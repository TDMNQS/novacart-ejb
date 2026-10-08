package com.numan.novacart;
import jakarta.servlet.annotation.WebListener;
import jakarta.servlet.http.*;
@WebListener
public class SessionCleanup implements HttpSessionListener {
 @Override public void sessionDestroyed(HttpSessionEvent event) {
  Object bean = event.getSession().getAttribute("cartBean");
  if (bean instanceof ShoppingCartBean cart) {
   try { cart.close(); } catch (jakarta.ejb.NoSuchEJBException ignored) { /* Already expired. */ }
  }
 }
}
