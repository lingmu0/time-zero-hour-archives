param(
    [switch]$Client,
    [switch]$Offline,
    [string]$NodePath = 'node'
)
$ErrorActionPreference = 'Stop'
$timeProjectRoot = Split-Path -Parent $PSScriptRoot
$timeValidationStarted = [DateTime]::UtcNow.ToString('o')
Get-Command $NodePath -ErrorAction Stop | Out-Null
Push-Location -LiteralPath $timeProjectRoot
try {
    & $NodePath tools/verify-resources.mjs
    if ($LASTEXITCODE -ne 0) { throw 'Static resource verification failed.' }
    $timeGradleArguments = @('build', 'runValidationServer')
    if ($Client) { $timeGradleArguments += 'runValidationClient' }
    if ($Offline) { $timeGradleArguments += '--offline' }
    & .\gradlew.bat @timeGradleArguments
    if ($LASTEXITCODE -ne 0) { throw 'Build or Minecraft runtime failed.' }
    # A game can exit cleanly after disconnecting; exit code alone is insufficient.
    $timeReportArguments = @('tools/verify-runtime-reports.mjs', "--since=$timeValidationStarted")
    if ($Client) { $timeReportArguments += '--client' }
    & $NodePath @timeReportArguments
    if ($LASTEXITCODE -ne 0) { throw 'Fresh runtime evidence did not pass verification.' }
} finally {
    Pop-Location
}
