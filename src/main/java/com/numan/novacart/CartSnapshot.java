package com.numan.novacart;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;
public record CartSnapshot(String customerName, String conversationId, long revision, List<CartLine> items) implements Serializable {
 public int itemCount() { return items.stream().mapToInt(CartLine::quantity).sum(); }
 public BigDecimal total() { return items.stream().map(CartLine::subtotal).reduce(new BigDecimal("0.00"), BigDecimal::add); }
}
