package com.numan.novacart;
import jakarta.ejb.Stateful;
import jakarta.ejb.StatefulTimeout;
import jakarta.ejb.Remove;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;
/** One container-managed instance per customer conversation; never a static cart. */
@Stateful
@StatefulTimeout(value = 35, unit = TimeUnit.MINUTES)
public class ShoppingCartBean implements Serializable {
 private static final long serialVersionUID = 1L;
 private String customerName = "";
 private final String conversationId = UUID.randomUUID().toString();
 private final Map<String, Integer> quantities = new LinkedHashMap<>();
 private long revision;
 public void setCustomerName(String name) {
  if (name == null || name.strip().isEmpty() || name.strip().length() > 60 || name.chars().anyMatch(Character::isISOControl)) throw new CartValidationException("Enter a customer name between 1 and 60 characters.");
  customerName = name.strip(); revision++;
 }
 public void add(String productId) {
  requireCustomer(); Catalog.get(productId);
  int quantity = quantities.getOrDefault(productId, 0);
  if (quantity >= 10) throw new CartValidationException("Maximum 10 units per product.");
  quantities.put(productId, quantity + 1); revision++;
 }
 public void updateQuantity(String productId, int quantity) {
  requireCustomer(); Catalog.get(productId);
  if (!quantities.containsKey(productId)) throw new CartValidationException("Product is not in your cart.");
  if (quantity < 1 || quantity > 10) throw new CartValidationException("Quantity must be between 1 and 10.");
  quantities.put(productId, quantity); revision++;
 }
 public void remove(String productId) {
  requireCustomer(); Catalog.get(productId);
  if (quantities.remove(productId) == null) throw new CartValidationException("Product is not in your cart.");
  revision++;
 }
 public void clear() { requireCustomer(); quantities.clear(); revision++; }
 public CartSnapshot viewCart() {
  List<CartLine> lines = quantities.entrySet().stream().map(e -> new CartLine(Catalog.get(e.getKey()), e.getValue())).toList();
  return new CartSnapshot(customerName, conversationId, revision, List.copyOf(lines));
 }
 private void requireCustomer() { if (customerName.isEmpty()) throw new CartValidationException("Enter your name to start shopping."); }
 /** @Remove tells the container to destroy this conversation after the invocation. */
 @Remove public void close() { quantities.clear(); }
}
