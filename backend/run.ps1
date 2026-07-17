# 后端启动（无需全局 mvn）
$ErrorActionPreference = 'Stop'
$mvn = 'C:\tools\apache-maven-3.9.6\bin\mvn.cmd'
if (-not (Test-Path $mvn)) {
  $mvn = Join-Path $PSScriptRoot '..\tools\apache-maven-3.9.6\bin\mvn.cmd'
}
if (-not (Test-Path $mvn)) {
  Write-Error '未找到 Maven，请安装到 C:\tools\apache-maven-3.9.6 或先执行 scripts\setup-env.ps1'
}
Set-Location $PSScriptRoot
& $mvn spring-boot:run @args
