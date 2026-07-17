# 全量接口冒烟（需后端已启动）
$ErrorActionPreference = 'Stop'
$base = 'http://localhost:8080/api'
$failed = @()

function Assert-Ok($name, $url, $headers = $null, $method = 'GET', $body = $null) {
  try {
    $params = @{ Uri = $url; Method = $method }
    if ($headers) { $params.Headers = $headers }
    if ($body) {
      $params.ContentType = 'application/json'
      $params.Body = $body
    }
    $r = Invoke-RestMethod @params
    if ($r.code -ne 200) { throw "code=$($r.code) msg=$($r.message)" }
    Write-Host "OK  $name"
  } catch {
    Write-Host "FAIL $name :: $($_.Exception.Message)"
    $script:failed += $name
  }
}

$login = Invoke-RestMethod "$base/auth/login" -Method Post -ContentType 'application/json' -Body '{"username":"admin","password":"Abc@123456","role":"admin"}'
$token = $login.data.token
$h = @{ Authorization = "Bearer $token" }

Assert-Ok 'blockchain/overview' "$base/blockchain/overview" $h
Assert-Ok 'blockchain/txs' "$base/blockchain/txs?page=1&size=5" $h
Assert-Ok 'transport/ongoing' "$base/transport/ongoing" $h
Assert-Ok 'predict/quality' "$base/predict/quality" $h
Assert-Ok 'predict/history' "$base/predict/history" $h
Assert-Ok 'predict/model-info' "$base/predict/model-info" $h
Assert-Ok 'suggestion/list' "$base/suggestion/list" $h
Assert-Ok 'route/plan' "$base/route/plan?origin=%E5%8D%97%E5%AE%81&destination=%E5%B9%BF%E5%B7%9E" $h
Assert-Ok 'loss/overview' "$base/loss/overview" $h
Assert-Ok 'carbon/overview' "$base/carbon/overview" $h
Assert-Ok 'data-quality/overview' "$base/data-quality/overview" $h
Assert-Ok 'api-manage/list' "$base/api-manage/list" $h
Assert-Ok 'api-manage/stats' "$base/api-manage/stats" $h
Assert-Ok 'trace/query(public)' "$base/trace/query?batchNo=20260717000001"
Assert-Ok 'h5/trace(public)' "$base/h5/trace?batchNo=20260717000001"

if ($failed.Count -gt 0) {
  Write-Host "`nFAILED: $($failed -join ', ')"
  exit 1
}
Write-Host "`nALL SMOKE TESTS PASSED"
exit 0
