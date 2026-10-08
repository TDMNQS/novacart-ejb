# Verification — NovaCart

Verified on 8 October 2026 using Java 17, Maven 3.9.9 and Payara Micro 6.2025.1 (Jakarta EE 10).

- `mvn clean verify`: BUILD SUCCESS; 13 tests, 0 failures, 0 errors.
- Real Payara HTTP/EJB deployment: 23 integration checks passed.
- Real Chromium browser: 12-product catalog; customer-name dialog; live header count; dedicated cart navigation; increase/decrease; cross-page and refresh retention; remove/clear; search; sorting; filters; product details; review; isolated mobile session; 390 px storefront/cart without horizontal overflow; zero JavaScript errors.
- Desktop (1440 px) and mobile (390 px) screenshots visually inspected.

The runtime checks call container-managed EJB methods through the servlet. Unit tests instantiate the bean only to check business rules and serialization; they do not prove container behavior. Integration checks prove the runtime conversation behavior.

Passivation serialization was tested by serializing/restoring the bean. Forced container passivation, clustered failover, load testing and server-restart durability were not tested. The launcher disables clustering for this single-user lab deployment.

## Business tests
1. Customer required before add.
2. Products and total survive multiple operations.
3. Repeat add increments quantity.
4. Remove deletes the entire selected product line.
5. Clear preserves customer name and conversation identity.
6. Separate bean instances isolate customers.
7. Invalid/excess quantities do not mutate state.
8. Name validation and rename retain products.
9. Returned snapshot cannot mutate bean state.
10. Bean fields survive Java serialization.
11. Expanded catalog has 12 unique IDs and the required categories.
12. New products support exact totals and quantity changes.
13. All catalog products can be added to one cart.

## Container integration checks
PASS distinct container-managed conversations
PASS expanded server catalog contains 12 products
PASS name required
PASS customer accepted
PASS CSRF rejected
PASS add laptop
PASS add mouse
PASS add keyboard
PASS exact server-owned total
PASS refresh retains EJB state
PASS independent customer has empty cart
PASS repeat add increments quantity
PASS reject zero quantity
PASS reject excessive quantity
PASS reject unlisted product
PASS reject noninteger quantity
PASS remove product recalculates total
PASS clear preserves customer conversation
PASS end destroys previous conversation
PASS new category products use server-owned prices
PASS increase new product quantity
PASS decrease new product quantity
PASS invalid quantity keeps same EJB usable
23 integration checks passed.
