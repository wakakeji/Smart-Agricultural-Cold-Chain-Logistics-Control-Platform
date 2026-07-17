# -*- coding: utf-8 -*-
"""Repair remaining ??? business data: alarms, transport routes, vehicle links."""
from __future__ import annotations

import subprocess
from pathlib import Path


def u(s: str) -> str:
    return "CONVERT(UNHEX('" + s.encode("utf-8").hex().upper() + "') USING utf8mb4)"


lines = [
    "USE app_db;",
    "SET NAMES utf8mb4;",
    f"UPDATE transport_order SET route={u('南宁->柳州')} WHERE order_no='TO20260716001';",
    f"UPDATE transport_order SET route={u('南宁->桂林')} WHERE order_no='TO20260716002';",
    f"UPDATE transport_order SET route={u('柳州->北海')} WHERE order_no='TO20260716003';",
    f"UPDATE transport_order SET route={u('百色->南宁')} WHERE order_no='TO20260716004';",
    f"UPDATE transport_order SET route={u('南宁->玉林')} WHERE order_no='TO20260716005';",
    f"UPDATE transport_order SET route={u('南宁->柳州')} WHERE route LIKE '%?%';",
    # remap vehicles
    "SET @rn := 0;",
    """UPDATE transport_order o
JOIN (
  SELECT facility_id, (@rn := @rn + 1) AS rn
  FROM facility
  WHERE type='REFRIGERATED_VEHICLE'
  ORDER BY facility_id
) v ON ((o.order_id - 1) % (SELECT COUNT(*) FROM facility WHERE type='REFRIGERATED_VEHICLE')) + 1 = v.rn
SET o.vehicle_id = v.facility_id
WHERE (SELECT COUNT(*) FROM facility WHERE type='REFRIGERATED_VEHICLE') > 0;""",
    # alarm rewrite by type
    f"UPDATE alarm_record SET source_name={u('桂林保鲜冷库C区')}, content={u('温度超标：当前 6.8℃，阈值 4.0℃')} WHERE type='TEMP_OVER' AND level='CRITICAL';",
    f"UPDATE alarm_record SET source_name={u('冷藏车桂C·F3003')}, content={u('车辆设备离线超过 10 分钟')} WHERE type='DEVICE_OFFLINE';",
    f"UPDATE alarm_record SET source_name={u('南宁东盟冷库A区')}, content={u('湿度偏高：当前 92%，阈值 90%')} WHERE type='HUMIDITY_OVER';",
    f"UPDATE alarm_record SET source_name={u('冷藏车桂B·E2002')}, content={u('运输振动异常')} WHERE type='VIBRATION';",
    f"UPDATE alarm_record SET source_name={u('南宁东盟冷库A区')}, content={u('CO2 浓度偏高')} WHERE type='CO2_OVER';",
    f"UPDATE alarm_record SET source_name={u('冷链设施')}, content={u('环境参数异常，请及时处理')} WHERE source_name LIKE '%?%' OR content LIKE '%?%';",
    f"UPDATE alarm_record SET handler={u('赵仓管')}, handle_remark={u('已处理')} WHERE status='RESOLVED';",
    # users again
    f"UPDATE sys_user SET real_name={u('管理员')} WHERE username='admin';",
]

sql = "\n".join(lines) + "\n"
out = Path(__file__).with_name("06_repair_business.sql")
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
err = proc.stderr.decode("utf-8", errors="replace")
print(err)
raise SystemExit(proc.returncode)
