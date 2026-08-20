package br.com.delivery.infrastructure.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.lang.NonNull;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Repository;
import org.springframework.jdbc.core.JdbcTemplate;

import br.com.delivery.domain.restaurant.Restaurant;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.domain.restaurant.RestaurantStatus;
import br.com.delivery.domain.restaurant.MenuItem;
import br.com.delivery.domain.restaurant.MenuItemCategory;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.restaurant.OpeningHours;
import br.com.delivery.domain.shared.Address;
import br.com.delivery.domain.shared.Currency;
import br.com.delivery.domain.shared.Money;
import br.com.delivery.domain.shared.ZipCode;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.repositories.IRestaurantRepository;

@Repository
public class RestaurantJdbcRepository implements IRestaurantRepository {
    private final JdbcTemplate jdbcTemplate;

    public RestaurantJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Restaurant> findById(@NonNull RestaurantId id) {
        String sql = """
                SELECT r.id, r.owner_id, r.name, r.status, r.open_hour, r.close_hour, r.currency,
                       a.street, a.number, a.complement, a.city, a.country, a.zip_code
                FROM restaurants r
                JOIN addresses a ON r.address_id = a.id
                WHERE r.id = ?
                """;

        try {
            Restaurant restaurant = jdbcTemplate.queryForObject(sql, (rs, rowNum) -> mapRowWithoutItems(rs), id.value());
            List<MenuItem> items = findItems(id);
            return Optional.of(rebuild(restaurant, items));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Restaurant> findAllByOwnerId(@NonNull AccountId ownerId) {
        String sql = """
                SELECT r.id, r.owner_id, r.name, r.status, r.open_hour, r.close_hour, r.currency,
                       a.street, a.number, a.complement, a.city, a.country, a.zip_code
                FROM restaurants r
                JOIN addresses a ON r.address_id = a.id
                WHERE r.owner_id = ?
                """;

        List<Restaurant> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowWithoutItems(rs), ownerId.value());

        return result.stream()
                .map(restaurant -> rebuild(restaurant, findItems(restaurant.getId())))
                .toList();
    }

    @Override
    public boolean existsById(@NonNull RestaurantId id) {
        String sql = "SELECT COUNT(*) FROM restaurants WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.value());
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void save(@NonNull Restaurant restaurant) {
        UUID addressId = upsertAddress(restaurant.getId(), restaurant.getAddress());
        upsertRestaurant(restaurant, addressId);
        replaceMenuItems(restaurant.getId(), restaurant.getMenu());
    }

    private void upsertRestaurant(@NonNull Restaurant restaurant, UUID addressId) {
        String sql = """
                INSERT INTO restaurants (id, owner_id, name, status, open_hour, close_hour, currency, address_id)
                VALUES (?, ?, ?, ?::restaurant_status, ?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET owner_id = EXCLUDED.owner_id,
                    name = EXCLUDED.name,
                    status = EXCLUDED.status,
                    open_hour = EXCLUDED.open_hour,
                    close_hour = EXCLUDED.close_hour,
                    currency = EXCLUDED.currency,
                    address_id = EXCLUDED.address_id
                """;

        jdbcTemplate.update(
                sql,
                restaurant.getId().value(),
                restaurant.getOwnerId().value(),
                restaurant.getName(),
                restaurant.getStatus().name(),
                restaurant.getOpenHour(),
                restaurant.getCloseHour(),
                currencyCode(restaurant.getCurrency()),
                addressId);
    }

    @NonNull
    private UUID upsertAddress(@NonNull RestaurantId restaurantId, @NonNull Address address) {
        String selectSql = "SELECT address_id FROM restaurants WHERE id = ?";
        UUID existingAddressId = jdbcTemplate.query(selectSql, rs -> rs.next() ? rs.getObject("address_id", UUID.class) : null, restaurantId.value());

        if (existingAddressId != null) {
            String updateSql = """
                    UPDATE addresses
                    SET street = ?, number = ?, complement = ?, city = ?, country = ?, zip_code = ?
                    WHERE id = ?
                    """;

            jdbcTemplate.update(
                    updateSql,
                    address.street(),
                    address.number(),
                    address.complement(),
                    address.city(),
                    address.country(),
                    address.zipCode().toString(),
                    existingAddressId);

            return existingAddressId;
        }

        UUID newAddressId = UUID.randomUUID();
        String insertSql = """
                INSERT INTO addresses (id, street, number, complement, city, country, zip_code)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        jdbcTemplate.update(
                insertSql,
                newAddressId,
                address.street(),
                address.number(),
                address.complement(),
                address.city(),
                address.country(),
                address.zipCode().toString());

        return newAddressId;
    }

    private void replaceMenuItems(@NonNull RestaurantId restaurantId, @NonNull List<MenuItem> items) {
        upsertMenuItems(restaurantId, items);
        deleteRemovedMenuItems(restaurantId, items);
    }

    private void upsertMenuItems(@NonNull RestaurantId restaurantId, @NonNull List<MenuItem> items) {
        if (items.isEmpty()) {
            return;
        }

        String sql = """
                INSERT INTO menu_items (id, restaurant_id, name, description, category, unit_price_amount, unit_price_currency, active)
                VALUES (?, ?, ?, ?, ?::menu_item_category, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name,
                    description = EXCLUDED.description,
                    category = EXCLUDED.category,
                    unit_price_amount = EXCLUDED.unit_price_amount,
                    unit_price_currency = EXCLUDED.unit_price_currency,
                    active = EXCLUDED.active
                """;

        jdbcTemplate.batchUpdate(sql, items, items.size(), (ps, item) -> {
            ps.setObject(1, item.getId().value());
            ps.setObject(2, restaurantId.value());
            ps.setString(3, item.getName());
            ps.setString(4, item.getDescription());
            ps.setString(5, item.getCategory().name());
            ps.setBigDecimal(6, item.currentPrice().amount());
            ps.setString(7, item.currentPrice().currency().name());
            ps.setBoolean(8, item.isActive());
        });
    }

    private void deleteRemovedMenuItems(@NonNull RestaurantId restaurantId, @NonNull List<MenuItem> items) {
        Set<UUID> currentIds = items.stream().map(item -> item.getId().value()).collect(Collectors.toSet());
        List<UUID> existingIds = jdbcTemplate.queryForList(
                "SELECT id FROM menu_items WHERE restaurant_id = ?", UUID.class, restaurantId.value());

        List<UUID> toDelete = existingIds.stream().filter(id -> !currentIds.contains(id)).toList();

        for (UUID id : toDelete) {
            jdbcTemplate.update("DELETE FROM menu_items WHERE id = ?", id);
        }
    }

    @NonNull
    private Restaurant rebuild(@NonNull Restaurant base, @NonNull List<MenuItem> items) {
        return Restaurant.restore(
                base.getId(),
                base.getOwnerId(),
                base.getName(),
                base.getOpeningHours(),
                base.getAddress(),
                base.getCurrency(),
                base.getStatus(),
                items);
    }

    private List<MenuItem> findItems(@NonNull RestaurantId id) {
        String sql = """
                SELECT id, restaurant_id, name, description, category, unit_price_amount, unit_price_currency, active
                FROM menu_items
                WHERE restaurant_id = ?
                """;

        return jdbcTemplate.query(sql, (rs, rowNum) -> mapMenuItem(rs), id.value());
    }

    private String currencyCode(Currency currency)w {
        return currency == null ? null : currency.name();
    }

    private Currency currencyFromCode(String code) {
        return code == null ? null : Currency.valueOf(code);
    }

    @NonNull
    private Restaurant mapRowWithoutItems(@NonNull ResultSet rs) throws SQLException {
        RestaurantId id = new RestaurantId(rs.getObject("id", UUID.class));
        AccountId ownerId = new AccountId(rs.getObject("owner_id", UUID.class));
        String name = rs.getString("name");

        OpeningHours openingHours = new OpeningHours(
                rs.getTime("open_hour").toLocalTime(),
                rs.getTime("close_hour").toLocalTime());

        Address address = new Address(
                rs.getString("street"),
                rs.getString("number"),
                rs.getString("complement"),
                rs.getString("city"),
                rs.getString("country"),
                new ZipCode(rs.getString("zip_code")));

        Currency currency = currencyFromCode(rs.getString("currency"));
        RestaurantStatus status = RestaurantStatus.valueOf(rs.getString("status"));

        return Restaurant.restore(
                id,
                ownerId,
                name,
                openingHours,
                address,
                currency,
                status,
                List.of());
    }

    @NonNull
    private MenuItem mapMenuItem(@NonNull ResultSet rs) throws SQLException {
        MenuItemId id = new MenuItemId(rs.getObject("id", UUID.class));
        RestaurantId restaurantId = new RestaurantId(rs.getObject("restaurant_id", UUID.class));
        String name = rs.getString("name");
        String description = rs.getString("description");
        MenuItemCategory category = MenuItemCategory.valueOf(rs.getString("category"));
        Currency currency = Currency.valueOf(rs.getString("unit_price_currency"));
        Money price = Money.of(rs.getBigDecimal("unit_price_amount"), currency);

        MenuItem item = new MenuItem(id, restaurantId, name, description, category, price);

        if (!rs.getBoolean("active")) {
            item.deactivate();
        }

        return item;
    }
}
