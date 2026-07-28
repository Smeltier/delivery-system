package br.com.delivery.application.dto.order;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import br.com.delivery.domain.restaurant.MenuItemCategory;
import br.com.delivery.domain.restaurant.MenuItemId;
import br.com.delivery.domain.shared.Money;
import br.com.delivery.domain.shared.Currency;

public class OrderItemOutputTest {

    @Test
    public void testValidOrderItemOutput() {
        MenuItemId menuItemId = MenuItemId.generate();
        String menuItemName = "Pizza Margherita";
        String description = "Pizza with tomato, mozzarella and basil";
        MenuItemCategory category = MenuItemCategory.DESSERT;
        int quantity = 2;
        Money unitPrice = Money.of(5.0, Currency.BRL);
        Money total = Money.of(10.0, Currency.BRL);

        OrderItemOutput orderItemOutput = new OrderItemOutput(
                menuItemId, menuItemName, description, category, quantity, unitPrice, total);

        assertEquals(menuItemId, orderItemOutput.menuItemId());
        assertEquals(menuItemName, orderItemOutput.menuItemName());
        assertEquals(description, orderItemOutput.description());
        assertEquals(category, orderItemOutput.category());
        assertEquals(quantity, orderItemOutput.quantity());
        assertEquals(unitPrice, orderItemOutput.unitPrice());
        assertEquals(total, orderItemOutput.total());
    }

    @Test
    public void testNullMenuItemIdThrows() {
        String menuItemName = "Pizza Margherita";
        String description = "Pizza with tomato, mozzarella and basil";
        MenuItemCategory category = MenuItemCategory.DESSERT;
        Money unitPrice = Money.of(5.0, Currency.BRL);
        Money total = Money.of(10.0, Currency.BRL);

        assertThrows(NullPointerException.class,
                () -> new OrderItemOutput(null, menuItemName, description, category, 2, unitPrice, total));
    }

    @Test
    public void testNullUnitPriceThrows() {
        MenuItemId menuItemId = MenuItemId.generate();
        String menuItemName = "Pizza Margherita";
        String description = "Pizza with tomato, mozzarella and basil";
        MenuItemCategory category = MenuItemCategory.DESSERT;
        Money total = Money.of(10.0, Currency.BRL);

        assertThrows(NullPointerException.class,
                () -> new OrderItemOutput(menuItemId, menuItemName, description, category, 2, null, total));
    }
}