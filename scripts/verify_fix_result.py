# -*- coding: utf-8 -*-
import json
import urllib.request
from pathlib import Path

BASE = "http://127.0.0.1:8080"


def call(method, path, data=None, token=None):
    headers = {"Accept": "application/json", "Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    body = None if data is None else json.dumps(data).encode("utf-8")
    req = urllib.request.Request(BASE + path, data=body, headers=headers, method=method)
    with urllib.request.urlopen(req) as resp:
        return json.loads(resp.read().decode("utf-8"))


login = call("POST", "/api/auth/login", {"username": "admin", "password": "Abc@123456", "role": "admin"})
tok = login["data"]["token"]
lines = []
for x in call("GET", "/api/transport/ongoing", token=tok)["data"][:3]:
    lines.append(f"TRANSPORT {x.get('orderNo')} vehicle={x.get('vehicleName')} route={x.get('route')}")
records = call("GET", "/api/alarm/list?page=1&size=3", token=tok)["data"]["records"]
for x in records:
    lines.append(f"ALARM {x.get('sourceName')} | {x.get('content')}")
feat = call("GET", "/api/predict/model-info", token=tok)["data"].get("features")
lines.append(f"FEATURES {feat}")
out = Path(__file__).resolve().parent / "e2e_results" / "verify_cn.txt"
out.write_text("\n".join(lines), encoding="utf-8")
print(out)
