package com.swappy.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.swappy.entities.Inventory;

@Service
public class BasePriceStrategy implements Strategy {

    @Override
    public BigDecimal calculatePrice(Inventory inventory) {
        return inventory.getRoom().getBasePrice();
    }
}