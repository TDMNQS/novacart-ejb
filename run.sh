#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
command -v java >/dev/null || { echo "Install JDK 17 first."; exit 1; }
if [ ! -f target/novacart.war ]; then mvn clean verify; fi
mkdir -p .runtime
if [ ! -f .runtime/payara-micro.jar ]; then
  curl -fL https://repo.maven.apache.org/maven2/fish/payara/extras/payara-micro/6.2025.1/payara-micro-6.2025.1.jar -o .runtime/payara-micro.jar.part
  mv .runtime/payara-micro.jar.part .runtime/payara-micro.jar
fi
python3 - <<'CHECK'
import hashlib
from pathlib import Path
assert hashlib.sha256(Path('.runtime/payara-micro.jar').read_bytes()).hexdigest() == "8e3ed1276234278034a7ac94efb0400eb0d1db733e20b0dc1f5b9178de2f82ae", 'Runtime checksum mismatch; delete .runtime/payara-micro.jar and retry.'
CHECK
echo "After startup, open http://localhost:8080/novacart/"
exec java -jar .runtime/payara-micro.jar --noCluster --port 8080 --deploy target/novacart.war --contextroot novacart
