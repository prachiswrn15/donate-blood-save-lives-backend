# Recover the single existing administrator account for this local database.
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$backendListener = Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue | Select-Object -First 1
if ($backendListener) {
    Write-Error 'Stop the currently running backend (Ctrl+C in its terminal), then run this recovery script again.'
    exit 1
}

$confirmation = Read-Host 'This will replace the existing admin email and password. Type RESET to continue'
if ($confirmation -cne 'RESET') {
    Write-Host 'Admin recovery cancelled.'
    exit 1
}

$adminEmailInput = Read-Host 'New administrator email'
$adminPasswordInput = Read-Host 'New administrator password (at least 8 characters)' -AsSecureString
$adminPasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($adminPasswordInput)
try {
    $env:ADMIN_EMAIL = $adminEmailInput
    $env:ADMIN_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($adminPasswordPointer)
    $env:ADMIN_RESET = 'true'
    $env:SPRING_MAIN_WEB_APPLICATION_TYPE = 'none'
    Push-Location -LiteralPath $projectRoot
    try { & (Join-Path $projectRoot 'mvnw.cmd') spring-boot:run }
    finally { Pop-Location }
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($adminPasswordPointer)
    Remove-Item Env:ADMIN_EMAIL, Env:ADMIN_PASSWORD, Env:ADMIN_RESET, Env:SPRING_MAIN_WEB_APPLICATION_TYPE -ErrorAction SilentlyContinue
}
