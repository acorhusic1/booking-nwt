# Task 4 — Load balancing demo for reservation-service
# -----------------------------------------------------
# Spawns 100 GET /api/reservations requests through the API Gateway
# (which uses lb://reservation-service via Eureka), records which instance
# answered each request and reports per-instance counts + total wall time.
#
# Prereqs:
#   1) docker compose -f docker-compose.yml -f docker-compose.loadbalancing.yml up -d --build
#   2) Wait until both reservation-service AND reservation-service-2 show UP in
#      Eureka (http://localhost:8761).
#   3) Obtain a JWT for an ADMIN user (the endpoint is @PreAuthorize hasRole('ADMIN')).
#      Pass it via -Token "<jwt>".
#
# Usage:
#   .\scripts\loadbalance-test.ps1                       # without LB (single instance)
#   .\scripts\loadbalance-test.ps1 -Token "<jwt>"        # with LB (compare timing)
#   .\scripts\loadbalance-test.ps1 -BaseUrl http://localhost:8080 -Requests 100 -Token "<jwt>"

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Path    = "/api/reservations",
    [int]   $Requests = 100,
    [string]$Token   = ""
)

$headers = @{}
if ($Token -ne "") { $headers["Authorization"] = "Bearer $Token" }

Write-Host "Sending $Requests requests to $BaseUrl$Path ..." -ForegroundColor Cyan

$instanceCounts = @{}
$failures = 0
$sw = [System.Diagnostics.Stopwatch]::StartNew()

for ($i = 1; $i -le $Requests; $i++) {
    try {
        $resp = Invoke-WebRequest -Uri "$BaseUrl$Path" -Headers $headers -UseBasicParsing -TimeoutSec 10
        # Spring Boot includes the responding instance in the X-Application-Context
        # or we can use a custom 'X-Instance-Id' header if added. Fallback to status.
        $instance = $resp.Headers["X-Instance-Id"]
        if (-not $instance) { $instance = $resp.Headers["X-Application-Context"] }
        if (-not $instance) { $instance = "unknown-instance" }
        if (-not $instanceCounts.ContainsKey($instance)) { $instanceCounts[$instance] = 0 }
        $instanceCounts[$instance]++
    } catch {
        $failures++
    }
}

$sw.Stop()
$totalMs = $sw.ElapsedMilliseconds
$avgMs   = [math]::Round($totalMs / $Requests, 2)

Write-Host ""
Write-Host "=== Load-balancing report ===" -ForegroundColor Green
Write-Host "Requests sent      : $Requests"
Write-Host "Failures           : $failures"
Write-Host "Total wall time    : ${totalMs} ms"
Write-Host "Avg per request    : ${avgMs} ms"
Write-Host "Distribution per instance:"
$instanceCounts.GetEnumerator() | Sort-Object Value -Descending | ForEach-Object {
    Write-Host ("  {0,-40} {1,4}" -f $_.Key, $_.Value)
}
Write-Host ""
Write-Host "Tip: run once with a single instance, once with both instances UP," -ForegroundColor Yellow
Write-Host "     and compare the wall time + distribution to satisfy Task 4." -ForegroundColor Yellow
