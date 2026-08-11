package br.com.delivery.domain.restaurantowner;

import java.util.Objects;

import br.com.delivery.domain.shared.Cnpj;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.exception.InvalidRestaurantOwnerException;

public class RestaurantOwner {
    private final AccountId id;
    private String corporateName;
    private Cnpj cnpj;

    private RestaurantOwner(AccountId id) {
        this.id = Objects.requireNonNull(id);
    }

    public static RestaurantOwner create(AccountId id) {
        return new RestaurantOwner(id);
    }

    public static RestaurantOwner restore(AccountId id, Cnpj cnpj, String corporateName) {
        RestaurantOwner owner = new RestaurantOwner(id);

        if (cnpj != null) {
            owner.updateCnpj(cnpj);
        }

        if (corporateName != null) {
            owner.updateCorporateName(corporateName);
        }

        return owner;
    }

    public void updateCnpj(Cnpj newCnpj) {
        this.cnpj = Objects.requireNonNull(newCnpj);
    }

    public void updateCorporateName(String newCorporateName) {
        if (newCorporateName == null || newCorporateName.isBlank()) {
            throw new InvalidRestaurantOwnerException("Novo nome corporativo inválido.");
        }
        this.corporateName = newCorporateName;
    }

    public AccountId getId() {
        return id;
    }

    public Cnpj getCnpj() {
        return cnpj;
    }

    public String getCorporateName() {
        return corporateName;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof RestaurantOwner)) {
            return false;
        }
        RestaurantOwner owner = (RestaurantOwner) obj;
        return this.id.equals(owner.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}