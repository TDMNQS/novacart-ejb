package com.numan.novacart;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import jakarta.servlet.ServletException;
import jakarta.json.*;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.UUID;
/** HttpSession retains the EJB proxy, while the EJB owns customer/cart state. */
@WebServlet("/api/cart")
public class CartServlet extends HttpServlet {
 private static final long serialVersionUID = 1L;
 private ShoppingCartBean cart(HttpSession session) throws NamingException {
  ShoppingCartBean bean = (ShoppingCartBean) session.getAttribute("cartBean");
  if (bean == null) {
   InitialContext context = new InitialContext();
   try { bean = (ShoppingCartBean) context.lookup("java:module/ShoppingCartBean"); } finally { context.close(); }
   session.setAttribute("cartBean", bean);
   session.setAttribute("csrf", UUID.randomUUID().toString());
  }
  return bean;
 }
 @Override protected void doGet(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException { process(req, res, false); }
 @Override protected void doPost(HttpServletRequest req, HttpServletResponse res) throws IOException, ServletException { process(req, res, true); }
 private void process(HttpServletRequest req, HttpServletResponse res, boolean mutation) throws IOException, ServletException {
  req.setCharacterEncoding("UTF-8"); res.setCharacterEncoding("UTF-8"); res.setContentType("application/json");
  res.setHeader("Cache-Control", "no-store"); res.setHeader("X-Content-Type-Options", "nosniff");
  HttpSession session = req.getSession();
  // Serialize operations belonging to this browser session; other customers proceed independently.
  synchronized (session) {
   try {
    ShoppingCartBean bean = cart(session);
    if (mutation) {
     String token = req.getHeader("X-CSRF-Token");
     String expected = (String) session.getAttribute("csrf");
     if (token == null || !MessageDigest.isEqual(token.getBytes(StandardCharsets.UTF_8), expected.getBytes(StandardCharsets.UTF_8))) { error(res, 403, "Session changed. Reload the page and try again."); return; }
     String action = req.getParameter("action");
     if (action == null) throw new IllegalArgumentException("Choose an operation.");
     switch (action) {
      case "customer" -> bean.setCustomerName(req.getParameter("name"));
      case "add" -> bean.add(req.getParameter("id"));
      case "quantity" -> bean.updateQuantity(req.getParameter("id"), parseQuantity(req.getParameter("quantity")));
      case "remove" -> bean.remove(req.getParameter("id"));
      case "clear" -> bean.clear();
      case "end" -> { session.invalidate(); res.getWriter().write("{\"ended\":true}"); return; }
      default -> throw new IllegalArgumentException("Unknown operation.");
     }
    }
    write(res, bean.viewCart(), (String) session.getAttribute("csrf"));
   } catch (IllegalArgumentException e) { error(res, 400, e.getMessage()); }
   catch (jakarta.ejb.NoSuchEJBException e) { session.invalidate(); error(res, 409, "Your cart session expired. Reload to begin a new session."); }
   catch (jakarta.ejb.EJBException e) {
    Throwable cause = e.getCausedByException();
    if (cause instanceof IllegalArgumentException) error(res, 400, cause.getMessage());
    else { getServletContext().log("Cart operation failed", e); error(res, 500, "Unable to update the cart. Please try again."); }
   } catch (NamingException e) { throw new ServletException("EJB lookup failed. Deploy on a Jakarta EE 10 EJB-capable server.", e); }
  }
 }
 private int parseQuantity(String value) { try { return Integer.parseInt(value); } catch (NumberFormatException e) { throw new IllegalArgumentException("Enter a whole-number quantity."); } }
 private void error(HttpServletResponse res, int status, String message) throws IOException { res.setStatus(status); res.getWriter().write(Json.createObjectBuilder().add("error", message).build().toString()); }
 private void write(HttpServletResponse res, CartSnapshot s, String token) throws IOException {
  JsonArrayBuilder lines = Json.createArrayBuilder(), catalog = Json.createArrayBuilder();
  for (CartLine line : s.items()) lines.add(product(line.product()).add("quantity", line.quantity()).add("subtotal", line.subtotal()));
  for (Product p : Catalog.PRODUCTS) catalog.add(product(p));
  res.getWriter().write(Json.createObjectBuilder().add("customerName", s.customerName()).add("conversationId", s.conversationId()).add("revision", s.revision()).add("items", lines).add("itemCount", s.itemCount()).add("total", s.total()).add("catalog", catalog).add("csrfToken", token).build().toString());
 }
 private JsonObjectBuilder product(Product p) { return Json.createObjectBuilder().add("id", p.id()).add("name", p.name()).add("category", p.category()).add("description", p.description()).add("price", p.price()); }
}
