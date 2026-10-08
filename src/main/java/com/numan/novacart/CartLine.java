package com.numan.novacart;
import java.io.Serializable;
import java.math.BigDecimal;
public record CartLine(Product product, int quantity) implements Serializable {
 public BigDecimal subtotal() { return product.price().multiply(BigDecimal.valueOf(quantity)); }
}
