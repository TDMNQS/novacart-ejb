package com.numan.novacart;
import java.io.Serializable;
import java.math.BigDecimal;
/** Immutable server-owned catalog entry. Prices never come from the browser. */
public record Product(String id, String name, String category, String description, BigDecimal price) implements Serializable {}
