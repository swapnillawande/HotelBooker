package com.swappy.strategy;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;

import com.swappy.entities.Inventory;

@Service
public class PricingService {

    public BigDecimal calculateDynamicPricing(Inventory inventory) {
        Strategy pricingStrategy = new BasePriceStrategy();

        pricingStrategy = new SurgePriceStrategy(pricingStrategy);
        pricingStrategy = new OccupancyPriceStrategy(pricingStrategy);
        pricingStrategy = new UrgencyPriceStrategy(pricingStrategy);

        return pricingStrategy.calculatePrice(inventory);
    }
}