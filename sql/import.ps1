# UTF-8 import schema + mock + business seed
$ErrorActionPreference = "Stop"
$mysql = "D:\MySQL\mysql-8.0.46\bin\mysql.exe"
if (-not (Test-Path $mysql)) { $mysql = "mysql" }
$hostName = "192.168.1.96"
$user = "root"
$pass = "123456"
$dir = Split-Path -Parent $MyInvocation.MyCommand.Path

function Import-SqlFile([string]$file) {
  Write-Host "Import $file ..."
  Get-Content -Raw -Encoding UTF8 $file | & $mysql -h $hostName -u$user -p$pass --default-character-set=utf8mb4
}

Import-SqlFile (Join-Path $dir "01_schema.sql")
Import-SqlFile (Join-Path $dir "02_mock_data.sql")
Import-SqlFile (Join-Path $dir "03_alter_batch.sql")
Import-SqlFile (Join-Path $dir "04_business_seed.sql")
Write-Host "Done."
