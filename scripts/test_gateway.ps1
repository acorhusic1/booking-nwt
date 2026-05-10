# Testiranje API Gateway-a i Sigurnosti
# Port Gateway-a: 8080

Write-Host "--- Testiranje JAVNOG endpointa preko Gateway-a ---" -ForegroundColor Cyan
try {
    $respPublic = Invoke-WebRequest -Uri "http://localhost:8080/api/properties" -Method GET -UseBasicParsing
    Write-Host "Status: $($respPublic.StatusCode) (Očekivano: 200)" -ForegroundColor Green
} catch {
    Write-Host "Greška kod javnog endpointa: $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n--- Testiranje ZAŠTIĆENOG endpointa BEZ tokena ---" -ForegroundColor Cyan
try {
    $respProtected = Invoke-WebRequest -Uri "http://localhost:8080/api/properties" -Method POST -Body '{}' -ContentType "application/json" -UseBasicParsing
    Write-Host "Status: $($respProtected.StatusCode) (OČEKIVANA GREŠKA: Trebalo bi da bude 401/403)" -ForegroundColor Yellow
} catch {
    if ($_.Exception.Response.StatusCode -eq 401 -or $_.Exception.Response.StatusCode -eq 403) {
        Write-Host "Status: $($_.Exception.Response.StatusCode) (USPJEŠNO: Gateway i Servis blokiraju neautorizovan pristup)" -ForegroundColor Green
    } else {
        Write-Host "Neočekivana greška: $($_.Exception.Response.StatusCode)" -ForegroundColor Red
    }
}

Write-Host "`n--- Testiranje rute za User Service ---" -ForegroundColor Cyan
try {
    $respUser = Invoke-WebRequest -Uri "http://localhost:8080/api/users/paginated" -Method GET -UseBasicParsing
    Write-Host "Status: $($respUser.StatusCode) (Gateway uspješno rutira na User Service)" -ForegroundColor Green
} catch {
    Write-Host "Gateway nije mogao dohvatiti User Service: $($_.Exception.Message)" -ForegroundColor Red
}
