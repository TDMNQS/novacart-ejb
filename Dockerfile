FROM maven:3.9.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml ./
COPY src ./src
RUN mvn -B -ntp clean verify
RUN mvn -B -ntp org.apache.maven.plugins:maven-dependency-plugin:3.8.1:copy \
    -Dartifact=fish.payara.extras:payara-micro:6.2025.1 \
    -DoutputDirectory=/runtime && \
    echo '8e3ed1276234278034a7ac94efb0400eb0d1db733e20b0dc1f5b9178de2f82ae  /runtime/payara-micro-6.2025.1.jar' | sha256sum -c -

FROM eclipse-temurin:17-jre-jammy
WORKDIR /app
RUN groupadd --system novacart && useradd --system --gid novacart novacart && chown novacart:novacart /app
COPY --from=build --chown=novacart:novacart /build/target/novacart.war /app/novacart.war
COPY --from=build --chown=novacart:novacart /runtime/payara-micro-6.2025.1.jar /app/payara-micro.jar
COPY --chown=novacart:novacart cloud-start.sh /app/cloud-start.sh
USER novacart
ENV PORT=10000
EXPOSE 10000
CMD ["sh", "/app/cloud-start.sh"]
