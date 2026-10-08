package com.numan.novacart;
import java.math.BigDecimal;
import java.util.List;
/** Read-only catalog. The server is the single source of truth for product prices. */
public final class Catalog {
 private Catalog() {}
 public static final List<Product> PRODUCTS = List.of(
  new Product("laptop", "NovaBook Pro 14", "Laptop", "14-inch display · 16 GB RAM · 512 GB SSD", new BigDecimal("64990.00")),
  new Product("laptop-air", "NovaBook Air 13", "Laptop", "13-inch display · 8 GB RAM · 256 GB SSD", new BigDecimal("47990.00")),
  new Product("laptop-studio", "NovaBook Studio 16", "Laptop", "16-inch display · 32 GB RAM · 1 TB SSD", new BigDecimal("94990.00")),
  new Product("mouse", "Arc Wireless Mouse", "Mouse", "Precision tracking · Silent clicks · Wireless", new BigDecimal("1299.00")),
  new Product("mouse-pro", "Orbit Precision Mouse", "Mouse", "Adjustable DPI · Ergonomic grip · USB-C charging", new BigDecimal("2499.00")),
  new Product("keyboard", "Type Mechanical Keyboard", "Keyboard", "Tactile switches · Compact layout · USB-C", new BigDecimal("3499.00")),
  new Product("keyboard-mini", "Type Mini 65", "Keyboard", "65% layout · Hot-swap switches · Wireless", new BigDecimal("4499.00")),
  new Product("headphones", "Pulse Over-Ear", "Audio", "Active noise cancellation · 40-hour battery · Bluetooth", new BigDecimal("5999.00")),
  new Product("earbuds", "Pulse Buds", "Audio", "Pocket-size case · Touch controls · USB-C charging", new BigDecimal("2999.00")),
  new Product("monitor", "Canvas Display 27", "Display", "27-inch QHD · IPS panel · Adjustable stand", new BigDecimal("18990.00")),
  new Product("monitor-wide", "Canvas UltraWide 34", "Display", "34-inch curved display · WQHD · USB-C", new BigDecimal("32990.00")),
  new Product("hub", "Link 7-in-1 Hub", "Accessory", "HDMI · USB-A · USB-C power delivery · SD reader", new BigDecimal("2299.00")));
 public static Product get(String id) {
  return PRODUCTS.stream().filter(product -> product.id().equals(id)).findFirst()
   .orElseThrow(() -> new CartValidationException("Choose a valid product."));
 }
}
