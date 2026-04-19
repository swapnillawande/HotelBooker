package com.swappy.strategy;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.swappy.entities.Inventory;

public class UrgencyPriceStrategy implements Strategy {

    private final Strategy wrapped;

    public UrgencyPriceStrategy(Strategy wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);

        LocalDate today = LocalDate.now();

        if (!inventory.getDate().isBefore(today) &&
            inventory.getDate().isBefore(today.plusDays(7))) {
            price = price.multiply(BigDecimal.valueOf(1.15));
        }

        return price;
    }
}