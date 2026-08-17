package br.com.delivery.infrastructure.persistence.jdbc;

import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import br.com.delivery.domain.repositories.IAccountRepository;
import br.com.delivery.domain.account.Account;
import br.com.delivery.domain.account.AccountRole;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.shared.Email;

@Repository
public class AccountJdbcRepository implements IAccountRepository {
    private final JdbcTemplate jdbcTemplate;

    public AccountJdbcRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Account> findById(@NonNull AccountId id) {
        String sql = "SELECT id, name, email FROM accounts WHERE id = ?";
        List<Account> result = jdbcTemplate.query(sql, (rs, rowNum) -> mapRowWithoutRoles(rs), id.value());

        if (result.isEmpty()) {
            return Optional.empty();
        }

        Set<AccountRole> roles = findRoles(id);
        Account partial = result.get(0);

        return Optional.of(Account.restore(id, partial.getName(), partial.getEmail(), roles));
    }

    @Override
    @Transactional
    public void save(@NonNull Account account) {
        String upsertAccount = """
                INSERT INTO accounts (id, name, email, active)
                VALUES (?, ?, ?, ?)
                ON CONFLICT (id) DO UPDATE
                SET name = EXCLUDED.name, email = EXCLUDED.email, active = EXCLUDED.active
                """;

        jdbcTemplate.update(
                upsertAccount,
                account.getId().value(),
                account.getName(),
                account.getEmail().value(),
                account.isActive());

        jdbcTemplate.update("DELETE FROM accounts_roles WHERE account_id = ?", account.getId().value());

        if (!account.getRoles().isEmpty()) {
            List<AccountRole> roles = account.getRoles().stream().toList();
            jdbcTemplate.batchUpdate(
                    "INSERT INTO accounts_roles (account_id, role) VALUES (?, ?::account_role)",
                    roles, roles.size(),
                    (ps, role) -> {
                        ps.setObject(1, account.getId().value());
                        ps.setString(2, role.name());
                    });
        }
    }

    @NonNull
    private Account mapRowWithoutRoles(@NonNull ResultSet rs) throws SQLException {
        AccountId id = new AccountId(rs.getObject("id", UUID.class));
        String name =  rs.getString("name");
        Email email = new Email(rs.getString("email"));

        return Account.restore(id, name, email, Set.of());
    }

    @NonNull
    private Set<AccountRole> findRoles(@NonNull AccountId id) {
        String sql = "SELECT role FROM accounts_roles WHERE account_id = ?";
        List<String> roles = jdbcTemplate.queryForList(sql, String.class, id.value());

        Set<AccountRole> result = new HashSet<>();
        for (String role : roles) {
            result.add(AccountRole.valueOf(role));
        }

        return result;
    }
}
