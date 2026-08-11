package br.com.delivery.infrastructure.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import br.com.delivery.domain.repositories.IRestaurantOwnerRepository;
import br.com.delivery.domain.restaurantowner.RestaurantOwner;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.shared.Cnpj;

@Repository
public class RestaurantOwnerJdbcRepository implements IRestaurantOwnerRepository {
    private final JdbcTemplate jdbcTemplate;

    public RestaurantOwnerJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<RestaurantOwner> findById(AccountId id) {
        String sql = "SELECT id, corporate_name, cnpj FROM restaurant_owners WHERE id = ?";
        List<RestaurantOwner> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id.value());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.get(0));
    }

    @Override
    public boolean existsById(AccountId id) {
        String sql = "SELECT COUNT(*) FROM restaurant_owners WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.value());
        return count > 0;
    }

    @Override
    public void save(RestaurantOwner restaurantOwner) {
        String sql = """
                INSERT INTO restaurant_owners (id, corporate_name, cnpj)
                VALUES(?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET corporate_name = EXCLUDED.corporate_name, cnpj = EXCLUDED.cnpj
                """;

        jdbcTemplate.update(
                sql,
                restaurantOwner.getId().value(),
                restaurantOwner.getCorporateName(),
                restaurantOwner.getCnpj() != null ? restaurantOwner.getCnpj().value() : null);
    }

    private RestaurantOwner mapRow(ResultSet rs) throws SQLException {
        AccountId id = new AccountId(UUID.fromString(rs.getString("id")));
        String corporateName = rs.getString("corporate_name");

        String cnpjValue = rs.getString("cnpj");
        if (cnpjValue != null) {
            return RestaurantOwner.restore(id, new Cnpj(cnpjValue), corporateName);
        }

        return RestaurantOwner.restore(id, null, corporateName);
    }
}
