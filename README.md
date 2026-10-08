# NovaCart 2 — Stateful EJB Shopping Cart

AWP CA3 • B.Tech IT • Batch 2, Group 1 • Numan Qureshi

## Start on Windows (PowerShell)

1. Install a **JDK 17** (not only a JRE). Check `java -version`.
2. Extract this ZIP. Open PowerShell inside `novacart`.
3. Run `powershell -ExecutionPolicy Bypass -File .\run.ps1`.
4. Open **http://localhost:8080/novacart/**. Keep the server terminal running.

The Windows launcher automatically selects Microsoft JDK 17 when installed and checks the Java version before server startup.

The package includes a built WAR. The launcher downloads the pinned Payara Micro 6.2025.1 runtime on first use, verifies its SHA-256, and deploys the WAR. Internet is needed only for this first runtime download. Subsequent runs use the downloaded runtime.

## Upgrade from Version 1

1. Stop the running server with Ctrl+C.
2. Extract the updated ZIP. Copy the files inside its `novacart` folder into your existing `novacart` folder and choose Replace for existing files.
3. The existing `.runtime` folder will be reused, avoiding a runtime download.
4. Run `powershell -ExecutionPolicy Bypass -File .\run.ps1` again.
5. Open /novacart/ and use Ctrl+F5 once to load the new frontend assets. This server restart begins a new cart session.

## Build or edit source

Install Apache Maven 3.9+ and JDK 17, then run:

```shell
mvn clean verify
```

Then launch again. Linux/macOS: install Python 3 and curl, then use `bash run.sh`. If port 8080 is occupied, stop the other service or use `java -jar .runtime/payara-micro.jar --noCluster --port 8081 --deploy target/novacart.war --contextroot novacart` and visit port 8081.

**Use an EJB-capable Jakarta EE 10 server. Plain Tomcat does not provide the EJB container required by this assignment.** Payara Micro is the supplied route; a Jakarta EE 10 Payara Server can also deploy this WAR. Do not change only imports to `javax.*`: Java EE 8 needs a compatible API and server.

## Features

- 12 server-owned products across Laptop, Mouse, Keyboard, Audio, Display and Accessory categories.
- Sticky header with a live item count; dedicated `cart.html` page.
- Search, category filters, price/name sorting and product quick-view dialogs.
- Customer-name dialog, cart recommendations and accessible empty states.
- Add, view, remove whole product, clear cart; quantity controls (1–10).
- Container-managed `@Stateful` bean, customer isolation, refresh retention.
- Server-owned catalog and exact `BigDecimal` currency calculations.
- CSRF token; HttpOnly session cookie; server-side validation; safe text rendering.
- Redesigned responsive storefront and cart; review dialog and expandable session diagnostics.
- Cart-page quantity controls with per-line subtotals and instant total updates.
- Explicit session end calls `@Remove` via listener; HTTP idle timeout 30 minutes.

## Architecture

Browser → CartServlet → session-specific EJB proxy → ShoppingCartBean.

The servlet is shared, so it **does not inject one shared Stateful EJB field**. On the first request for a browser session it performs `java:module/ShoppingCartBean` lookup, obtaining a fresh container-managed bean and retaining that proxy in HttpSession. The bean owns `customerName`, `quantities`, `conversationId`, and `revision`. HttpSession owns only the proxy and CSRF token. Requests for the same session are serialized; different sessions remain isolated. Clear empties product state while retaining the customer conversation. End destroys that conversation. Regular page refresh invokes viewCart on the same proxy. Snapshot objects are immutable and cannot mutate the live bean.

## Five-minute demonstration

1. Add **NovaBook Pro 14**. Enter **Numan** in the customer-name dialog. Add **Arc Wireless Mouse** and **Type Mechanical Keyboard**. Click the header **Cart** link to open the dedicated cart page. Total: **₹69,788.00**.
2. Refresh. Same items, customer, conversation ID and revision remain.
3. Add Mouse again. Quantity becomes 2. Total: **₹71,087.00**.
4. Remove Keyboard. Total: **₹67,588.00**.
5. Open a private/incognito window. Enter **Furqan**. His cart is empty and has a different conversation ID. Numan's cart stays intact.
6. Clear Numan's cart. Items disappear, while customer and conversation ID remain.
7. End session in the Your shopping session panel on the cart page. The next session has a new conversation ID.

## Tests and report

`mvn verify` runs 13 business-logic tests. `python tests/integration.py` runs a real HTTP/EJB server test when the server is running (Python standard library only). See `docs/VERIFICATION.md` for the recorded results. The accompanying PDF follows all **17 required report sections**, includes test cases, viva notes and complete authored source code.

## Scope and practical limits

This is an academic shopping-cart application, not a payment platform. Review is not checkout; there are no orders, real stock reservations, authentication or payment processing. Prices are sample INR catalog prices. Session state is conversational: do not promise retention after timeout, ending the session, or server restart. A production commerce service would need persistent orders, stock handling, authentication, TLS/secure-cookie configuration and payment integration. No database is needed for this lab's state demonstration. The customer name is a display name, not authenticated identity. Avoid using real personal or payment information in the demonstration.
