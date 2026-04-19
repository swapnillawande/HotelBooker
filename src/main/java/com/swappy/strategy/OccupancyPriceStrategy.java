package com.swappy.strategy;

import java.math.BigDecimal;

import com.swappy.entities.Inventory;

public class OccupancyPriceStrategy implements Strategy {

    private final Strategy wrapped;

    public OccupancyPriceStrategy(Strategy wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        BigDecimal price = wrapped.calculatePrice(inventory);

        if (inventory.getTotalCount() == 0) {
            return price;
        }

        double occupancyRate = (double) inventory.getBookedCount() / inventory.getTotalCount();

        if (occupancyRate > 0.8) {
            price = price.multiply(BigDecimal.valueOf(1.2));
        }

        return price;
    }
}