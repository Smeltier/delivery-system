package br.com.delivery.domain.client;

import java.util.Objects;
import java.util.Optional;

import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.shared.Address;
import br.com.delivery.domain.shared.Cpf;

public final class Client {
    private final AccountId id;
    private Address address;
    private Cpf cpf;

    private Client(AccountId id) {
        this.id = Objects.requireNonNull(id);
    }

    public static Client create(AccountId id) {
        return new Client(id);
    }

    public static Client restore(AccountId id) {
        Client client = new Client(id);
        return client;
    }

    public void updateAddress(Address newAddress) {
        this.address = Objects.requireNonNull(newAddress);
    }

    public AccountId getId() {
        return id;
    }

    public Address getAddress() {
        return address;
    }

    public Optional<Cpf> getCpf() {
        return Optional.ofNullable(this.cpf);
    }

    public void setCpf(Cpf newCpf) {
        this.cpf = Objects.requireNonNull(newCpf);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Client)) {
            return false;
        }
        Client client = (Client) o;
        return this.id.equals(client.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
