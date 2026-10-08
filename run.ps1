$ErrorActionPreference = "Stop"
Set-Location $PSScriptRoot
# Prefer the JDK 17 installed during setup; do not accidentally launch with a newer JDK.
$jdk17 = Get-ChildItem "C:\Program Files\Microsoft" -Directory -Filter "jdk-17*" -ErrorAction SilentlyContinue | Sort-Object Name -Descending | Select-Object -First 1
if ($jdk17 -and (Test-Path (Join-Path $jdk17.FullName "bin/java.exe"))) {
    $env:JAVA_HOME = $jdk17.FullName
    $env:Path = "$env:JAVA_HOME\bin;$env:Path"
}
if (-not (Get-Command java -ErrorAction SilentlyContinue)) { throw "Install JDK 17 and add Java to PATH, then run again." }
if (-not (Test-Path "target/novacart.war")) { if (-not (Get-Command mvn -ErrorAction SilentlyContinue)) { throw "Install Maven 3.9+ to build source, then run mvn clean verify." }; & mvn clean verify; if ($LASTEXITCODE -ne 0) { throw "Maven build failed." } }
$javaVersion = cmd /c "java -version 2>&1" | Out-String
if ($javaVersion -notmatch 'version "17\.') { throw "This project requires Java 17. Install it with: winget install --id Microsoft.OpenJDK.17 --exact; then run again." }
Write-Host "Starting with Java 17..."
New-Item -ItemType Directory -Force ".runtime" | Out-Null
$runtime = ".runtime/payara-micro.jar"
$expected = "8e3ed1276234278034a7ac94efb0400eb0d1db733e20b0dc1f5b9178de2f82ae"
if (-not (Test-Path $runtime)) { Write-Host "Downloading Payara runtime (first run only)..."; Invoke-WebRequest -Uri "https://repo.maven.apache.org/maven2/fish/payara/extras/payara-micro/6.2025.1/payara-micro-6.2025.1.jar" -OutFile "$runtime.part"; Move-Item "$runtime.part" $runtime }
if ((Get-FileHash $runtime -Algorithm SHA256).Hash.ToLower() -ne $expected) { throw "Runtime checksum mismatch. Delete .runtime/payara-micro.jar and try again." }
Write-Host "After startup, open http://localhost:8080/novacart/"
& java -jar $runtime --noCluster --port 8080 --deploy target/novacart.war --contextroot novacart
