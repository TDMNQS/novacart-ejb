# Cloud deployment

The Docker image builds the WAR from source, runs unit tests, and uses Java 17 with checksum-verified Payara Micro 6.2025.1. It starts as an unprivileged user on Render's PORT.

Create one Render Docker web service from this repository's main branch, using Dockerfile and the repository root as the build context. Use Singapore and the free plan for an academic demonstration. The application URL is https://SERVICE.onrender.com/novacart/. A suitable health check is /novacart/index.html.

After the service is live, Vercel can proxy the complete application to that Render URL. Proxy all /novacart/ routes, including the API, so the session cookie and the browser share the same origin. Redirect the Vercel root to /novacart/. Preserve the /novacart/ cookie path, forward Set-Cookie, and disable API caching. Do not publish only the static frontend without its API route.

Keep one Payara instance: the Stateful EJB and HttpSession live in that process. A restart, redeployment, or free-service idle suspension ends existing carts. This is a demonstration deployment, not durable shopping-cart storage. No database or payment processor is configured.

Deployment verification must include catalog loading, customer name, adding products, header item count, opening the cart page, increasing/decreasing quantities, removing and clearing items, refresh persistence, and isolation between two browsers. Local tests do not establish that a cloud deployment is live.
