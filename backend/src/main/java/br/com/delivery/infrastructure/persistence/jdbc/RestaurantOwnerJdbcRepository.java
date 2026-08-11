package br.com.delivery.infrastructure.persistence.jdbc;

import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.repositories.IRestaurantOwnerRepository;
import br.com.delivery.domain.restaurantowner.RestaurantOwner;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

public class RestaurantOwnerJdbcRepository implements IRestaurantOwnerRepository {
    private final JdbcTemplate jdbcTemplate;

    public RestaurantOwnerJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<RestaurantOwner> findById(AccountId id) {
        return Optional.empty();
    }

    @Override
    public boolean existsById(AccountId id) {
        String sql = "SELECT COUNT(*) FROM restaurant_owners WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.value());
        return count > 0;
    }

    @Override
    public void save(RestaurantOwner restaurantOwner) {
    }
}