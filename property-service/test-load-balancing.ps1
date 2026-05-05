# Poboljšana Load Balancing Test Skripta
# Prikazuje rezultat svakog zahtjeva radi verifikacije load balancera

$url = "http://localhost:8080/api/properties/test"
$requests = 100
$successCount = 0
$failCount = 0

Write-Host "Pokrećem $requests zahtjeva na $url..." -ForegroundColor Cyan

for ($i = 1; $i -le $requests; $i++) {
    $response = curl.exe -s $url
    
    if ($LASTEXITCODE -eq 0 -and $response -like "*Property Service Instance*") {
        $successCount++
        Write-Host "Zahtjev $($i): OK - $response"
    } else {
        $failCount++
        Write-Host "Zahtjev $($i): GREŠKA" -ForegroundColor Red
    }
}

Write-Host "`nRezultati testiranja:" -ForegroundColor Cyan
Write-Host "Broj uspješnih zahtjeva: $successCount"
Write-Host "Broj neuspješnih zahtjeva: $failCount"

if ($successCount -eq $requests) {
    Write-Host "Load balancing radi ispravno!" -ForegroundColor Green
} else {
    Write-Host "Bilo je grešaka tokom testa." -ForegroundColor Yellow
}
