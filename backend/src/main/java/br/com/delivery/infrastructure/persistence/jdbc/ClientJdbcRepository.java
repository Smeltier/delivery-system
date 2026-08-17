package br.com.delivery.infrastructure.persistence.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.delivery.domain.repositories.IClientRepository;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.client.Client;
import br.com.delivery.domain.shared.Address;
import br.com.delivery.domain.shared.Cpf;
import br.com.delivery.domain.shared.ZipCode;

@Repository
public class ClientJdbcRepository implements IClientRepository {
    private final JdbcTemplate jdbcTemplate;

    public ClientJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Client> findById(@NonNull AccountId id) {
        String sql = """
                SELECT c.id, c.cpf, c.address_id,
                a.street, a.number, a.complement, a.city, a.country, a.zip_code
                FROM clients c
                LEFT JOIN addresses a ON a.id = c.address_id
                WHERE c.id = ?
                """;
        List<Client> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRow(rs), id.value());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(result.get(0));
    }

    @Override
    public boolean existsById(@NonNull AccountId id) {
        String sql = "SELECT COUNT(*) FROM clients WHERE id = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, id.value());
        return count > 0;
    }

    @Override
    @Transactional
    public void save(@NonNull Client client) {
        UUID addressId = null;

        if (client.getAddress() != null) {
            addressId = UUID.randomUUID();
            Address address = client.getAddress();

            String addressQuery = "INSERT INTO addresses (id, street, number, complement, city, country, zip_code) VALUES (?, ?, ?, ?, ?, ?, ?)";
            jdbcTemplate.update(
                    addressQuery,
                    addressId,
                    address.street(),
                    address.number(),
                    address.complement(),
                    address.city(),
                    address.country(),
                    address.zipCode().value());
        }

        String cpfValue = client.getCpf().map(Cpf::value).orElse(null);

        String sql = """
                INSERT INTO clients (id, cpf, address_id)
                VALUES (?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET cpf = EXCLUDED.cpf, address_id = EXCLUDED.address_id
                """;

        jdbcTemplate.update(sql, client.getId().value(), cpfValue, addressId);
    }

    @NonNull
    private Client mapRow(@NonNull ResultSet rs) throws SQLException {
        AccountId id = new AccountId(rs.getObject("id", UUID.class));
        Client client = Client.restore(id);

        String cpfValue = rs.getString("cpf");
        if (cpfValue != null) {
            client.setCpf(new Cpf(cpfValue));
        }

        Address address = null;
        if (rs.getObject("address_id") != null) {
            address = new Address(
                    rs.getString("street"),
                    rs.getString("number"),
                    rs.getString("complement"),
                    rs.getString("city"),
                    rs.getString("country"),
                    new ZipCode(rs.getString("zip_code")));

            client.updateAddress(address);
        }

        return client;
    }
}
