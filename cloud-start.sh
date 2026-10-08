#!/bin/sh
set -eu
exec java -Xms64m -Xmx256m -XX:MaxMetaspaceSize=160m \
  -jar /app/payara-micro.jar --noCluster --port "${PORT:-10000}" \
  --rootDir /app/runtime --deploy /app/novacart.war --contextroot novacart
