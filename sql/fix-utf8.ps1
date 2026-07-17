# 用 UNHEX 写回中文，避免管道乱码把汉字变成 ???
$ErrorActionPreference = 'Stop'
$mysql = 'D:\MySQL\mysql-8.0.46\bin\mysql.exe'
if (-not (Test-Path $mysql)) { $mysql = 'mysql' }

function Hex([string]$s) {
  ([System.BitConverter]::ToString([System.Text.Encoding]::UTF8.GetBytes($s))).Replace('-', '')
}
function U([string]$s) { "CONVERT(UNHEX('$(Hex $s)') USING utf8mb4)" }

$sb = New-Object System.Text.StringBuilder
[void]$sb.AppendLine('USE app_db;')
[void]$sb.AppendLine('SET NAMES utf8mb4;')

# 用户姓名
$map = @{
  farmer01 = '张农户'; logistics01 = '李调度'; driver01 = '王司机'
  wholesaler01 = '赵仓管'; retailer01 = '钱零售'; consumer01 = '孙消费者'; admin = '管理员'
}
foreach ($k in $map.Keys) {
  [void]$sb.AppendLine("UPDATE sys_user SET real_name=$(U $map[$k]) WHERE username='$k';")
}

# 角色名
$roles = @{
  farmer = '农户/生产者'; logistics = '物流企业'; driver = '司机'
  wholesaler = '批发商/仓储方'; retailer = '零售商'; consumer = '消费者'; admin = '平台管理员'
}
foreach ($k in $roles.Keys) {
  [void]$sb.AppendLine("UPDATE sys_role SET role_name=$(U $roles[$k]) WHERE role_code='$k';")
}

# 批次
[void]$sb.AppendLine("UPDATE product_batch SET product_name=$(U '百色芒果'), origin=$(U '广西百色') WHERE batch_no='20260716000001';")
[void]$sb.AppendLine("UPDATE product_batch SET product_name=$(U '武鸣沃柑'), origin=$(U '广西南宁武鸣') WHERE batch_no='20260716000002';")
[void]$sb.AppendLine("UPDATE product_batch SET unit=$(U '箱') WHERE unit IS NULL OR unit='?' OR unit LIKE '%?%';")

# 碳排放来源名
[void]$sb.AppendLine("UPDATE carbon_emission SET source_name=$(U '桂A·D1001') WHERE source_type='VEHICLE';")
[void]$sb.AppendLine("UPDATE carbon_emission SET source_name=$(U '南宁东盟冷库A区') WHERE source_type='COLD_STORAGE';")
[void]$sb.AppendLine("UPDATE carbon_emission SET source_name=$(U '包装环节') WHERE source_type='PACKAGE';")

# 决策建议
$sugs = @(
  @('TEMP_CONTROL', '降低车厢设定温度', '桂A·D1001 车厢温度偏高，建议将设定温度下调 1℃ 并检查冷机'),
  @('REROUTE', '绕开拥堵路段', '南宁→柳州高速拥堵预计延误 40 分钟，建议改走 G7211'),
  @('PRIORITY_SALE', '优先销售临期批次', '批次货架期剩余不足 48 小时，建议优先配送商超渠道'),
  @('MAINTENANCE', '冷库设备巡检', '南宁冷库压缩机运行时长超阈值，建议安排巡检'),
  @('LOSS_REDUCE', '加强装卸规范', '近7日运输损耗偏高，建议加强装卸培训与缓冲包装'),
  @('CARBON', '优化发车班次', '空载率偏高，建议合并同向订单降低碳排放')
)
foreach ($s in $sugs) {
  [void]$sb.AppendLine("UPDATE decision_suggestion SET title=$(U $s[1]), content=$(U $s[2]) WHERE type='$($s[0])';")
}

# 损耗
[void]$sb.AppendLine("UPDATE loss_record SET product_name=$(U '百色芒果') WHERE batch_no='20260716000001';")
[void]$sb.AppendLine("UPDATE loss_record SET product_name=$(U '武鸣沃柑') WHERE batch_no='20260716000002';")
[void]$sb.AppendLine("UPDATE loss_record SET loss_reason=$(U '运输颠簸破损') WHERE loss_type='TRANSPORT' AND loss_reason LIKE '%?%';")
[void]$sb.AppendLine("UPDATE loss_record SET loss_reason=$(U '冷库温控波动') WHERE loss_type='STORAGE' AND loss_reason LIKE '%?%';")
[void]$sb.AppendLine("UPDATE loss_record SET loss_reason=$(U '包装破损') WHERE loss_type='PACKAGE' AND loss_reason LIKE '%?%';")
[void]$sb.AppendLine("UPDATE loss_record SET loss_reason=$(U '货架损耗') WHERE loss_type='RETAIL' AND loss_reason LIKE '%?%';")

# 字典标签（若已是 ??? 则重写）
$dicts = @(
  @('facility_status','ONLINE','在线'), @('facility_status','OFFLINE','离线'), @('facility_status','ALARM','告警'),
  @('sensor_type','TEMP','温度'), @('sensor_type','HUMIDITY','湿度'), @('sensor_type','CO2','二氧化碳'), @('sensor_type','VIBRATION','振动'),
  @('alarm_level','CRITICAL','严重'), @('alarm_level','WARNING','警告'), @('alarm_level','INFO','提示'),
  @('alarm_type','TEMP_OVER','温度过高'), @('alarm_type','HUMIDITY_OVER','湿度过高'), @('alarm_type','DEVICE_OFFLINE','设备离线'),
  @('alarm_type','VIBRATION','震动异常'), @('alarm_type','CO2_OVER','二氧化碳过高'),
  @('alarm_status','PENDING','待处理'), @('alarm_status','PROCESSING','处理中'), @('alarm_status','RESOLVED','已解决'), @('alarm_status','IGNORED','已忽略'),
  @('transport_status','PENDING','待发车'), @('transport_status','TRANSPORTING','运输中'), @('transport_status','COMPLETED','已完成'),
  @('chain_status','CONFIRMED','已确认'), @('chain_status','PENDING','待确认'), @('chain_status','CHAINED','已上链'),
  @('network_status','HEALTHY','正常'), @('network_status','DEGRADED','降级'),
  @('biz_type','BATCH','批次赋码'), @('biz_type','BATCH_CREATE','批次创建'), @('biz_type','TRACE','追溯节点'),
  @('biz_type','SENSOR_DATA','传感数据'), @('biz_type','QUALITY_REPORT','品质报告'),
  @('carbon_source','VEHICLE','运输车辆'), @('carbon_source','COLD_STORAGE','冷库仓储'), @('carbon_source','PACKAGE','包装环节')
)
foreach ($d in $dicts) {
  [void]$sb.AppendLine(@"
INSERT INTO sys_dict(dict_type, dict_code, dict_label, sort_order, status)
VALUES ('$($d[0])','$($d[1])',$(U $d[2]),0,1)
ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);
"@)
}

# 修正告警来源中文
[void]$sb.AppendLine("UPDATE alarm_record SET source_name=$(U '桂林保鲜冷库C区') WHERE source_name LIKE '%?%' OR source_name='???';")
[void]$sb.AppendLine("UPDATE alarm_record SET content=$(U '温度超标，请及时处理') WHERE content LIKE '%?%';")
[void]$sb.AppendLine("UPDATE alarm_record SET handler=$(U '赵仓管'), handle_remark=$(U '已处理') WHERE status='RESOLVED' AND (handler IS NULL OR handler LIKE '%?%' OR handler='');")

$tmp = Join-Path $env:TEMP 'cc_fix_utf8.sql'
[System.IO.File]::WriteAllText($tmp, $sb.ToString(), [System.Text.UTF8Encoding]::new($false))
Write-Host "Import $tmp"
Get-Content -Raw -Encoding UTF8 $tmp | & $mysql -h 192.168.1.96 -uroot -p123456 --default-character-set=utf8mb4
Write-Host 'UTF-8 fix done.'
& $mysql -h 192.168.1.96 -uroot -p123456 --default-character-set=utf8mb4 -N -e "USE app_db; SELECT username, real_name FROM sys_user WHERE username='admin'; SELECT product_name, origin FROM product_batch LIMIT 1; SELECT title FROM decision_suggestion LIMIT 1;"
