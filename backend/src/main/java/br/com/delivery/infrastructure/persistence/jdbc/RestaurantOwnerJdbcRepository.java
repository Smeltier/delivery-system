package br.com.delivery.infrastructure.persistence.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

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
    public Optional<RestaurantOwner> findById(@NonNull AccountId id) {
        String sql = "SELECT id, corporate_name, cnpj FROM restaurant_owners WHERE id = ?";
        List<RestaurantOwner> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id.value());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.get(0));
    }

    @Override
    public boolean existsById(@NonNull AccountId id) {
        String sql = "SELECT COUNT(*) FROM restaurant_owners WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.value());
        return count != null && count > 0;
    }

    @Override
    @Transactional
    public void save(@NonNull RestaurantOwner restaurantOwner) {
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

    @NonNull
    private RestaurantOwner mapRow(@NonNull ResultSet rs) throws SQLException {
        AccountId id = new AccountId(rs.getObject("id", UUID.class));
        String corporateName = rs.getString("corporate_name");

        String cnpjValue = rs.getString("cnpj");
        if (cnpjValue != null) {
            return RestaurantOwner.restore(id, new Cnpj(cnpjValue), corporateName);
        }

        return RestaurantOwner.restore(id, null, corporateName);
    }
}
