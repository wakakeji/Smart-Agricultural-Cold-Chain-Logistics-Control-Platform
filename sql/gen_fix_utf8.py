# -*- coding: utf-8 -*-
"""Generate UNHEX update SQL and import via mysql client."""
from __future__ import annotations

import subprocess
import tempfile
from pathlib import Path


def u(s: str) -> str:
    return "CONVERT(UNHEX('" + s.encode("utf-8").hex().upper() + "') USING utf8mb4)"


lines: list[str] = [
    "USE app_db;",
    "SET NAMES utf8mb4;",
]

users = {
    "farmer01": "张农户",
    "logistics01": "李调度",
    "driver01": "王司机",
    "wholesaler01": "赵仓管",
    "retailer01": "钱零售",
    "consumer01": "孙消费者",
    "admin": "管理员",
}
for k, v in users.items():
    lines.append(f"UPDATE sys_user SET real_name={u(v)} WHERE username='{k}';")

roles = {
    "farmer": "农户/生产者",
    "logistics": "物流企业",
    "driver": "司机",
    "wholesaler": "批发商/仓储方",
    "retailer": "零售商",
    "consumer": "消费者",
    "admin": "平台管理员",
}
for k, v in roles.items():
    lines.append(f"UPDATE sys_role SET role_name={u(v)} WHERE role_code='{k}';")

lines += [
    f"UPDATE product_batch SET product_name={u('百色芒果')}, origin={u('广西百色')} WHERE batch_no='20260716000001';",
    f"UPDATE product_batch SET product_name={u('武鸣沃柑')}, origin={u('广西南宁武鸣')} WHERE batch_no='20260716000002';",
    f"UPDATE product_batch SET unit={u('箱')} WHERE unit IS NULL OR unit LIKE '%?%';",
    f"UPDATE carbon_emission SET source_name={u('桂A·D1001')} WHERE source_type='VEHICLE';",
    f"UPDATE carbon_emission SET source_name={u('南宁东盟冷库A区')} WHERE source_type='COLD_STORAGE';",
    f"UPDATE carbon_emission SET source_name={u('包装环节')} WHERE source_type='PACKAGE';",
    f"UPDATE loss_record SET product_name={u('百色芒果')} WHERE batch_no='20260716000001';",
    f"UPDATE loss_record SET product_name={u('武鸣沃柑')} WHERE batch_no='20260716000002';",
    f"UPDATE alarm_record SET handler={u('赵仓管')}, handle_remark={u('已处理')} WHERE status='RESOLVED';",
]

sugs = [
    ("TEMP_CONTROL", "降低车厢设定温度", "车厢温度偏高，建议下调设定温度并检查冷机"),
    ("REROUTE", "绕开拥堵路段", "高速拥堵预计延误，建议改走备用路线"),
    ("PRIORITY_SALE", "优先销售临期批次", "货架期不足，建议优先配送商超渠道"),
    ("MAINTENANCE", "冷库设备巡检", "压缩机运行时长超阈值，建议安排巡检"),
    ("LOSS_REDUCE", "加强装卸规范", "运输损耗偏高，建议加强装卸培训"),
    ("CARBON", "优化发车班次", "空载率偏高，建议合并同向订单"),
]
for t, title, content in sugs:
    lines.append(
        f"UPDATE decision_suggestion SET title={u(title)}, content={u(content)} WHERE type='{t}';"
    )

dicts = [
    ("facility_status", "ONLINE", "在线"),
    ("facility_status", "OFFLINE", "离线"),
    ("facility_status", "ALARM", "告警"),
    ("sensor_type", "TEMP", "温度"),
    ("sensor_type", "HUMIDITY", "湿度"),
    ("sensor_type", "CO2", "二氧化碳"),
    ("alarm_level", "CRITICAL", "严重"),
    ("alarm_level", "WARNING", "警告"),
    ("alarm_level", "INFO", "提示"),
    ("alarm_type", "TEMP_OVER", "温度过高"),
    ("alarm_type", "HUMIDITY_OVER", "湿度过高"),
    ("alarm_type", "DEVICE_OFFLINE", "设备离线"),
    ("alarm_type", "VIBRATION", "震动异常"),
    ("alarm_type", "CO2_OVER", "二氧化碳过高"),
    ("alarm_status", "PENDING", "待处理"),
    ("alarm_status", "PROCESSING", "处理中"),
    ("alarm_status", "RESOLVED", "已解决"),
    ("alarm_status", "IGNORED", "已忽略"),
    ("transport_status", "PENDING", "待发车"),
    ("transport_status", "TRANSPORTING", "运输中"),
    ("transport_status", "COMPLETED", "已完成"),
    ("chain_status", "CONFIRMED", "已确认"),
    ("chain_status", "PENDING", "待确认"),
    ("network_status", "HEALTHY", "正常"),
    ("biz_type", "BATCH", "批次赋码"),
    ("biz_type", "BATCH_CREATE", "批次创建"),
    ("biz_type", "TRACE", "追溯节点"),
    ("biz_type", "SENSOR_DATA", "传感数据"),
    ("biz_type", "QUALITY_REPORT", "品质报告"),
    ("carbon_source", "VEHICLE", "运输车辆"),
    ("carbon_source", "COLD_STORAGE", "冷库仓储"),
    ("carbon_source", "PACKAGE", "包装环节"),
    ("facility_type", "COLD_STORAGE", "冷库"),
    ("facility_type", "REFRIGERATED_VEHICLE", "冷藏车"),
    ("suggestion_priority", "HIGH", "高"),
    ("suggestion_priority", "MEDIUM", "中"),
    ("suggestion_priority", "LOW", "低"),
    ("suggestion_type", "TEMP_CONTROL", "温控调整"),
    ("suggestion_type", "REROUTE", "路径改道"),
    ("suggestion_type", "PRIORITY_SALE", "优先销售"),
    ("suggestion_type", "MAINTENANCE", "设备巡检"),
    ("suggestion_type", "LOSS_REDUCE", "减损规范"),
    ("suggestion_type", "CARBON", "碳排优化"),
    ("data_quality_issue", "DELAY", "上报延迟"),
    ("data_quality_issue", "MISSING", "数据缺失"),
    ("data_quality_issue", "OUTLIER", "异常尖峰"),
    ("sensor_type", "VIBRATION", "振动"),
    ("sensor_type", "LIGHT", "光照"),
    ("sys_config", "blockchain.display_total", "28456"),
    ("sys_config", "blockchain.node_count", "7"),
]
for i, (dt, code, label) in enumerate(dicts):
    lines.append(
        "INSERT INTO sys_dict(dict_type, dict_code, dict_label, sort_order, status) "
        f"VALUES ('{dt}','{code}',{u(label)},{i},1) "
        "ON DUPLICATE KEY UPDATE dict_label=VALUES(dict_label);"
    )

# 设施名若已是 ???，按 id 批量重写常见几条（其余由 FacilitySeedService 重启时写入）
cities = ["南宁", "柳州", "桂林", "北海", "玉林", "百色", "钦州", "梧州"]
for i, city in enumerate(cities, start=1):
    name = f"{city}冷库{i:03d}区"
    addr = f"广西壮族自治区{city}市冷链园区"
    lines.append(
        f"UPDATE facility SET name={u(name)}, address={u(addr)} "
        f"WHERE type='COLD_STORAGE' AND name LIKE '%?%' LIMIT 1;"
    )

sql = "\n".join(lines) + "\n"
out = Path(__file__).with_name("05_fix_utf8.sql")
out.write_text(sql, encoding="utf-8")
print("wrote", out)

mysql = Path(r"D:\MySQL\mysql-8.0.46\bin\mysql.exe")
cmd = [
    str(mysql if mysql.exists() else "mysql"),
    "-h",
    "192.168.1.96",
    "-uroot",
    "-p123456",
    "--default-character-set=utf8mb4",
]
proc = subprocess.run(cmd, input=sql.encode("utf-8"), capture_output=True)
print(proc.stdout.decode("utf-8", errors="replace"))
print(proc.stderr.decode("utf-8", errors="replace"))
raise SystemExit(proc.returncode)
