package com.poultry.broiler_farming_system.dto.marketplace;

import java.math.BigDecimal;

public record CreateSalesPostRequest(String title, String description, BigDecimal price) {
}
