# 将本机 Maven 加入当前用户 PATH（解决 mvn 无法识别）
$ErrorActionPreference = 'Stop'
$mavenBin = 'C:\tools\apache-maven-3.9.6\bin'
if (-not (Test-Path (Join-Path $mavenBin 'mvn.cmd'))) {
  Write-Error "Maven 不存在: $mavenBin"
}
$userPath = [Environment]::GetEnvironmentVariable('Path', 'User')
if ($userPath -notlike "*$mavenBin*") {
  [Environment]::SetEnvironmentVariable('Path', "$userPath;$mavenBin", 'User')
  Write-Host "已写入用户 PATH: $mavenBin"
  Write-Host '请重新打开终端后执行: mvn -version'
} else {
  Write-Host 'PATH 已包含 Maven'
}
$env:Path = "$mavenBin;$env:Path"
& (Join-Path $mavenBin 'mvn.cmd') -version
