package br.com.delivery.infrastructure.config;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import br.com.delivery.domain.account.Account;
import br.com.delivery.domain.account.AccountId;
import br.com.delivery.domain.account.AccountRole;
import br.com.delivery.domain.client.Client;
import br.com.delivery.domain.repositories.IAccountRepository;
import br.com.delivery.domain.repositories.IClientRepository;
import br.com.delivery.domain.repositories.IRestaurantOwnerRepository;
import br.com.delivery.domain.repositories.IRestaurantRepository;
import br.com.delivery.domain.restaurant.MenuItem;
import br.com.delivery.domain.restaurant.MenuItemCategory;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.restaurant.OpeningHours;
import br.com.delivery.domain.restaurant.Restaurant;
import br.com.delivery.domain.restaurant.RestaurantId;
import br.com.delivery.domain.restaurant.RestaurantStatus;
import br.com.delivery.domain.restaurantowner.RestaurantOwner;
import br.com.delivery.domain.shared.Address;
import br.com.delivery.domain.shared.Currency;
import br.com.delivery.domain.shared.Email;
import br.com.delivery.domain.shared.Money;
import br.com.delivery.domain.shared.ZipCode;

@Configuration
public class DevelopmentSeedConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger(DevelopmentSeedConfig.class);

    private static final AccountId CLIENT_ACCOUNT_ID = new AccountId(UUID.fromString("11111111-1111-1111-1111-111111111111"));
    private static final AccountId OWNER_ACCOUNT_ID = new AccountId(UUID.fromString("22222222-2222-2222-2222-222222222222"));
    private static final RestaurantId RESTAURANT_ID = new RestaurantId(UUID.fromString("33333333-3333-3333-3333-333333333333"));
    private static final MenuItemId MENU_ITEM_ID = new MenuItemId(UUID.fromString("44444444-4444-4444-4444-444444444444"));

    @Bean
    public CommandLineRunner seedData(
            IAccountRepository accountRepository,
            IClientRepository clientRepository,
            IRestaurantOwnerRepository restaurantOwnerRepository,
            IRestaurantRepository restaurantRepository,
            @Value("${app.seed.enabled:true}") boolean seedEnabled
    ) {
        return args -> {
            if (!seedEnabled) {
                LOGGER.info("Seed inicial desativado via app.seed.enabled=false");
                return;
            }

            Account clientAccount = Account.restore(
                    CLIENT_ACCOUNT_ID,
                    "Cliente Demo",
                    new Email("cliente.demo@delivery.com"),
                    Set.of(AccountRole.BASE_CLIENT));

            Account ownerAccount = Account.restore(
                    OWNER_ACCOUNT_ID,
                    "Dona do Restaurante",
                    new Email("dona.restaurante@delivery.com"),
                    Set.of(AccountRole.RESTAURANT_OWNER));

            accountRepository.save(clientAccount);
            accountRepository.save(ownerAccount);

            clientRepository.save(Client.create(CLIENT_ACCOUNT_ID));
            restaurantOwnerRepository.save(RestaurantOwner.create(OWNER_ACCOUNT_ID));

            Address address = new Address(
                    "Rua das Flores",
                    "100",
                    "Loja 1",
                    "Belo Horizonte",
                    "Brasil",
                    new ZipCode("30110-000"));

            OpeningHours openingHours = new OpeningHours(LocalTime.of(9, 0), LocalTime.of(22, 0));

            MenuItem brownie = new MenuItem(
                    MENU_ITEM_ID,
                    RESTAURANT_ID,
                    "Brownie de Chocolate",
                    "Brownie com calda quente",
                    MenuItemCategory.DESSERT,
                    Money.of(BigDecimal.valueOf(18.90), Currency.BRL));

            Restaurant restaurant = Restaurant.restore(
                    RESTAURANT_ID,
                    OWNER_ACCOUNT_ID,
                    "Doce Ponto",
                    openingHours,
                    address,
                    Currency.BRL,
                    RestaurantStatus.CLOSED,
                    List.of(brownie));

            restaurantRepository.save(restaurant);

            LOGGER.info("Seed inicial carregado.");
            LOGGER.info("accountId (cliente): {}", CLIENT_ACCOUNT_ID.value());
            LOGGER.info("restaurantId: {}", RESTAURANT_ID.value());
            LOGGER.info("menuItemId: {}", MENU_ITEM_ID.value());
        };
        }
}
