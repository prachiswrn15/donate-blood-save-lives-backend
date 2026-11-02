# Start the backend with credentials for its first administrator account.
# Credentials stay in this PowerShell process and are not saved in a file.
$projectRoot = [System.IO.Path]::GetFullPath((Join-Path $PSScriptRoot '../..'))
$adminEmailInput = Read-Host 'Email for the first administrator'
$adminPasswordInput = Read-Host 'Password (at least 8 characters)' -AsSecureString
$adminPasswordPointer = [Runtime.InteropServices.Marshal]::SecureStringToBSTR($adminPasswordInput)
try {
    $env:ADMIN_EMAIL = $adminEmailInput
    $env:ADMIN_PASSWORD = [Runtime.InteropServices.Marshal]::PtrToStringBSTR($adminPasswordPointer)
    Push-Location -LiteralPath $projectRoot
    try { & (Join-Path $projectRoot 'mvnw.cmd') spring-boot:run }
    finally { Pop-Location }
} finally {
    [Runtime.InteropServices.Marshal]::ZeroFreeBSTR($adminPasswordPointer)
    Remove-Item Env:ADMIN_EMAIL,Env:ADMIN_PASSWORD -ErrorAction SilentlyContinue
}
