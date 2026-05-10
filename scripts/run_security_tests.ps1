# $ErrorActionPreference = "SilentlyContinue"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "🚀 POKRETANJE SIGURNOSNIH TESTOVA (Zadatak 1)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 1. Login kao GUEST
Write-Host "`n1. Pokušavam Login kao GUEST (benjamin.h@email.com)..."
$loginBodyGuest = @{
    email = "benjamin.h@email.com"
    password = "password123"
} | ConvertTo-Json

$responseGuest = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginBodyGuest -ContentType "application/json"
if ($responseGuest.StatusCode -eq 200) {
    Write-Host "✅ USPJEH (200 OK) - Dobijen GUEST token" -ForegroundColor Green
    $guestData = $responseGuest.Content | ConvertFrom-Json
    $guestToken = $guestData.accessToken
} else {
    Write-Host "❌ GRESKA: Nije uspio login za GUEST" -ForegroundColor Red
}

# 2. Login kao ADMIN
Write-Host "`n2. Pokušavam Login kao ADMIN (admin@bookingnwt.com)..."
$loginBodyAdmin = @{
    email = "admin@bookingnwt.com"
    password = "password123"
} | ConvertTo-Json

$responseAdmin = Invoke-WebRequest -Uri "http://localhost:8080/api/auth/login" -Method POST -Body $loginBodyAdmin -ContentType "application/json"
if ($responseAdmin.StatusCode -eq 200) {
    Write-Host "✅ USPJEH (200 OK) - Dobijen ADMIN token" -ForegroundColor Green
    $adminData = $responseAdmin.Content | ConvertFrom-Json
    $adminToken = $adminData.accessToken
} else {
    Write-Host "❌ GRESKA: Nije uspio login za ADMIN" -ForegroundColor Red
}

# 3. Pristup bez tokena (Pogrešna Autentifikacija)
Write-Host "`n3. Pokušavam pristupiti ZAŠTIĆENOM resursu (Korisnici) BEZ TOKENA..."
$responseNoToken = Invoke-WebRequest -Uri "http://localhost:8080/api/users" -Method GET
if ($responseNoToken.StatusCode -eq 401) {
    Write-Host "✅ USPJEH (401 Unauthorized) - Pristup odbijen kako se i očekivalo!" -ForegroundColor Green
} else {
    Write-Host "⚠️ NEOČEKIVANO: Status code je $($responseNoToken.StatusCode)" -ForegroundColor Yellow
}

# 4. Pristup sa pogrešnom rolom (Pogrešna Autorizacija)
Write-Host "`n4. Pokušavam pristupiti ADMIN resursu (Korisnici) sa GUEST TOKENOM..."
$headersGuest = @{ "Authorization" = "Bearer $guestToken" }
$responseWrongRole = Invoke-WebRequest -Uri "http://localhost:8080/api/users" -Method GET -Headers $headersGuest
if ($responseWrongRole.StatusCode -eq 403) {
    Write-Host "✅ USPJEH (403 Forbidden) - Pristup odbijen (Guest nema pristup Admin resursima)!" -ForegroundColor Green
} else {
    Write-Host "⚠️ NEOČEKIVANO: Status code je $($responseWrongRole.StatusCode)" -ForegroundColor Yellow
}

# 5. Uspješan pristup (Happy Path)
Write-Host "`n5. Pokušavam pristupiti ADMIN resursu (Korisnici) sa ADMIN TOKENOM..."
$headersAdmin = @{ "Authorization" = "Bearer $adminToken" }
$responseHappy = Invoke-WebRequest -Uri "http://localhost:8080/api/users" -Method GET -Headers $headersAdmin
if ($responseHappy.StatusCode -eq 200) {
    Write-Host "✅ USPJEH (200 OK) - Admin je uspješno pristupio podacima!" -ForegroundColor Green
} else {
    Write-Host "⚠️ NEOČEKIVANO: Status code je $($responseHappy.StatusCode)" -ForegroundColor Yellow
}

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "🎉 SVI TESTOVI ZAVRŠENI" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan
