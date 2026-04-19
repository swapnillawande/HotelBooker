package com.swappy.strategy;

import java.math.BigDecimal;
import com.swappy.entities.Inventory;

public interface Strategy {
    BigDecimal calculatePrice(Inventory inventory);
}