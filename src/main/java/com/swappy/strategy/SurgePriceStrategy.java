package com.swappy.strategy;

import java.math.BigDecimal;

import com.swappy.entities.Inventory;

public class SurgePriceStrategy implements Strategy {

    private final Strategy wrapped;

    public SurgePriceStrategy(Strategy wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);
        return price.multiply(inventory.getSurgeFactor());
    }
}