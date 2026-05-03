# Load Balancing Test Script
# Šalje 100 zahtjeva na property-service i mjeri vrijeme

$url = "http://localhost:8080/api/properties" # Pretpostavljamo da Gateway radi na 8080
$requests = 100
$times = @()

Write-Host "Pokrećem $requests zahtjeva na $url..."

for ($i = 1; $i -le $requests; $i++) {
    $elapsed = Measure-Command {
        try {
            Invoke-WebRequest -Uri $url -Method Get -UseBasicParsing | Out-Null
        } catch {
            Write-Host "Greška u zahtjevu $i" -ForegroundColor Red
        }
    }
    $times += $elapsed.TotalMilliseconds
}

$avg = ($times | Measure-Object -Average).Average
$max = ($times | Measure-Object -Maximum).Maximum
$min = ($times | Measure-Object -Minimum).Minimum

Write-Host "`nRezultati testiranja:" -ForegroundColor Cyan
Write-Host "Prosječno vrijeme: $avg ms"
Write-Host "Maksimalno vrijeme: $max ms"
Write-Host "Minimalno vrijeme: $min ms"

# Dokumentacija za Emir-a:
# Pokrenuti dvije instance property-service:
# 1. mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8082"
# 2. mvn spring-boot:run -Dspring-boot.run.arguments="--server.port=8083"
# Pratiti logove oba servisa da se vidi raspodjela zahtjeva.
