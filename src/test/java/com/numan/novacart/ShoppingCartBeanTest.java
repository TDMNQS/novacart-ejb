package com.numan.novacart;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;
import java.io.*;
class ShoppingCartBeanTest {
 private ShoppingCartBean started() { ShoppingCartBean b=new ShoppingCartBean();b.setCustomerName("Numan");return b; }
 @Test void requiresCustomer() { assertThrows(IllegalArgumentException.class, () -> new ShoppingCartBean().add("laptop")); }
 @Test void preservesAcrossOperations() { var b=started();String id=b.viewCart().conversationId();b.add("laptop");b.add("mouse");assertEquals(2,b.viewCart().itemCount());assertEquals(new BigDecimal("66289.00"),b.viewCart().total());assertEquals(id,b.viewCart().conversationId()); }
 @Test void incrementsExistingProduct() { var b=started();b.add("mouse");b.add("mouse");assertEquals(1,b.viewCart().items().size());assertEquals(2,b.viewCart().itemCount()); }
 @Test void removesWholeProduct() { var b=started();b.add("mouse");b.add("mouse");b.add("keyboard");b.remove("mouse");assertEquals(new BigDecimal("3499.00"),b.viewCart().total()); }
 @Test void clearsItemsButKeepsCustomerAndIdentity() { var b=started();String id=b.viewCart().conversationId();b.add("laptop");b.clear();assertEquals("Numan",b.viewCart().customerName());assertEquals(id,b.viewCart().conversationId());assertEquals(0,b.viewCart().itemCount()); }
 @Test void isolatesCustomers() { var a=started();var b=started();a.add("laptop");assertEquals(0,b.viewCart().itemCount());assertNotEquals(a.viewCart().conversationId(),b.viewCart().conversationId()); }
 @Test void rejectsInvalidAndExcessQuantityWithoutMutation() { var b=started();b.add("mouse");long before=b.viewCart().revision();assertThrows(IllegalArgumentException.class,()->b.updateQuantity("mouse",0));assertThrows(IllegalArgumentException.class,()->b.add("unknown"));assertEquals(before,b.viewCart().revision());for(int i=1;i<10;i++)b.add("mouse");assertThrows(IllegalArgumentException.class,()->b.add("mouse"));assertEquals(10,b.viewCart().itemCount()); }
 @Test void validatesNameAndPreservesCartOnRename() { var b=started();b.add("laptop");assertThrows(IllegalArgumentException.class,()->b.setCustomerName(" "));b.setCustomerName("  Furqan  ");assertEquals("Furqan",b.viewCart().customerName());assertEquals(1,b.viewCart().itemCount()); }
 @Test void snapshotIsImmutable() { var b=started();b.add("mouse");var snapshot=b.viewCart();assertThrows(UnsupportedOperationException.class,()->snapshot.items().clear());b.clear();assertEquals(1,snapshot.itemCount()); }
 @Test void serializesPassivationState() throws Exception { var b=started();b.add("keyboard");var bytes=new ByteArrayOutputStream();try(var out=new ObjectOutputStream(bytes)){out.writeObject(b);}try(var in=new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))){var restored=(ShoppingCartBean)in.readObject();assertEquals(b.viewCart(),restored.viewCart());} }
 @Test void expandedCatalogHasUniqueIdsAndRequiredCategories() {
  assertEquals(12, Catalog.PRODUCTS.size());
  assertEquals(12, Catalog.PRODUCTS.stream().map(Product::id).distinct().count());
  assertTrue(Catalog.PRODUCTS.stream().allMatch(p -> p.price().signum() > 0));
  assertEquals("Laptop", Catalog.get("laptop").category());
  assertEquals("Mouse", Catalog.get("mouse").category());
  assertEquals("Keyboard", Catalog.get("keyboard").category());
 }
 @Test void expandedProductsParticipateInExactTotals() {
  var b=started();b.add("headphones");b.add("monitor");b.add("hub");
  assertEquals(new BigDecimal("27288.00"),b.viewCart().total());
  b.updateQuantity("headphones",2);
  assertEquals(new BigDecimal("33287.00"),b.viewCart().total());
  b.updateQuantity("headphones",1);
  assertEquals(new BigDecimal("27288.00"),b.viewCart().total());
 }
 @Test void allCatalogProductsCanBeAddedToOneCart() {
  var b=started();for(Product product : Catalog.PRODUCTS)b.add(product.id());
  assertEquals(12,b.viewCart().items().size());
  assertEquals(Catalog.PRODUCTS.stream().map(Product::price).reduce(new BigDecimal("0.00"),BigDecimal::add),b.viewCart().total());
 }
}
