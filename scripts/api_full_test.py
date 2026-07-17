# -*- coding: utf-8 -*-
"""API-level full functional test against running backend (port 8080)."""
from __future__ import annotations

import json
import re
import urllib.error
import urllib.parse
import urllib.request
from datetime import datetime
from pathlib import Path

BASE = "http://127.0.0.1:8080"
OUT = Path(__file__).resolve().parent / "e2e_results"
OUT.mkdir(parents=True, exist_ok=True)
issues: list[dict] = []


def add(page: str, severity: str, msg: str, detail: str = ""):
    issues.append({"page": page, "severity": severity, "message": msg, "detail": detail[:800]})
    print(f"[{severity}] {page}: {msg}")


def req(method: str, path: str, data=None, token: str | None = None, timeout=15):
    url = BASE + path
    headers = {"Accept": "application/json", "Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    body = None if data is None else json.dumps(data).encode("utf-8")
    r = urllib.request.Request(url, data=body, headers=headers, method=method)
    try:
        with urllib.request.urlopen(r, timeout=timeout) as resp:
            raw = resp.read()
            text = raw.decode("utf-8")
            try:
                return resp.status, json.loads(text), text
            except Exception:
                return resp.status, None, text
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", errors="replace")
        try:
            return e.code, json.loads(raw), raw
        except Exception:
            return e.code, None, raw
    except Exception as e:
        return 0, None, str(e)


def has_qmark(obj) -> bool:
    s = json.dumps(obj, ensure_ascii=False) if not isinstance(obj, str) else obj
    return bool(re.search(r"\?{2,}", s))


def main():
    print("== health ==")
    st, data, raw = req("GET", "/api/system/health")
    if st != 200:
        # try alternate
        st, data, raw = req("GET", "/actuator/health")
    print("health", st, (raw or "")[:200])

    print("== login ==")
    st, data, raw = req(
        "POST",
        "/api/auth/login",
        {"username": "admin", "password": "Abc@123456", "role": "admin"},
    )
    token = None
    if isinstance(data, dict):
        if data.get("code") not in (0, 200, None) and data.get("data") is None and "token" not in data:
            add("登录", "critical", f"登录业务失败: {data.get('message')}", raw[:300])
        payload = data.get("data") if data.get("data") is not None else data
        if isinstance(payload, dict):
            token = payload.get("token") or payload.get("accessToken")
            user = payload.get("user") or payload.get("userInfo") or {}
            real = user.get("realName") or payload.get("realName")
            if real and "?" in str(real):
                add("登录/用户", "major", f"realName 乱码: {real}")
            elif real:
                print("realName OK:", real)
            else:
                add("登录/用户", "info", "登录响应无 realName 字段", json.dumps(payload, ensure_ascii=False)[:300])
    if not token:
        add("登录", "critical", "无法获取 token", raw[:300])
        _dump()
        return 1
    print("token ok")

    checks = [
        ("地图设施", "GET", "/api/facilities"),
        ("指挥大屏", "GET", "/api/dashboard/overview"),
        ("告警列表", "GET", "/api/alarm/list?page=1&size=20"),
        ("实时传感器", "GET", "/api/monitor/sensors?page=1&size=20"),
        ("数据质量", "GET", "/api/data-quality/overview"),
        ("批次列表", "GET", "/api/batch/list?page=1&size=20"),
        ("区块链", "GET", "/api/blockchain/txs?page=1&size=20"),
        ("决策建议", "GET", "/api/suggestion/list"),
        ("运输监控", "GET", "/api/transport/ongoing"),
        ("损耗", "GET", "/api/loss/overview"),
        ("碳排放", "GET", "/api/carbon/overview"),
        ("碳排放明细", "GET", "/api/carbon/detail"),
        ("用户列表", "GET", "/api/system/users?page=1&size=20"),
        ("服务监控", "GET", "/api/system/health"),
        ("API管理", "GET", "/api/api-manage/list"),
        ("地图配置", "GET", "/api/system/map-config"),
        ("品质模型", "GET", "/api/predict/model-info"),
        ("路线规划", "GET", "/api/route/plans"),
    ]

    for name, method, path in checks:
        st, data, raw = req(method, path, token=token)
        print(f"{name}: {st}")
        if st != 200:
            add(name, "critical", f"HTTP {st}", raw[:300])
            continue
        payload = data.get("data") if isinstance(data, dict) and "data" in data else data
        if has_qmark(payload):
            add(name, "major", "响应含 ?? 乱码", json.dumps(payload, ensure_ascii=False)[:400])
        # empty
        if payload in ([], {}, None) or (isinstance(payload, dict) and payload.get("records") == []):
            add(name, "info", "数据为空或 records=[]")

    # transport detail
    st, data, raw = req("GET", "/api/transport/ongoing", token=token)
    payload = (data or {}).get("data") if isinstance(data, dict) else data
    if isinstance(payload, list):
        for row in payload[:5]:
            if str(row.get("route", "")).find("?") >= 0:
                add("运输监控", "major", f"route 乱码: {row.get('route')}")
                break
            if row.get("vehicleName") in (None, "", "-"):
                add("运输监控", "major", f"vehicleName 缺失 order={row.get('orderNo')}")
                break

    # alarm detail
    st, data, raw = req("GET", "/api/alarm/list?page=1&size=20", token=token)
    payload = (data or {}).get("data") if isinstance(data, dict) else data
    records = payload.get("records") if isinstance(payload, dict) else payload
    if isinstance(records, list) and records:
        for row in records[:5]:
            sn = str(row.get("sourceName", ""))
            ct = str(row.get("content", ""))
            if "?" in sn or "?" in ct:
                add("预警管理", "major", f"来源/内容乱码 source={sn} content={ct[:80]}")
                break

    # trace
    for batch in ("20260716000001", "20260716000002", "20260717000001"):
        st, data, raw = req("GET", f"/api/trace/query?batchNo={batch}", token=token)
        payload = (data or {}).get("data") if isinstance(data, dict) else data
        ok = isinstance(payload, dict) and (payload.get("product") or payload.get("batchNo") or payload.get("nodes"))
        print(f"trace {batch}: {st} ok={bool(ok)}")
        if not ok:
            add("追溯查询", "major" if batch.startswith("20260716") else "info", f"批次 {batch} 查询无有效数据", raw[:200])
        elif has_qmark(payload):
            add("追溯查询", "major", f"批次 {batch} 结果乱码")

    # h5
    st, data, raw = req("GET", "/api/h5/trace?batchNo=20260716000001")
    payload = (data or {}).get("data") if isinstance(data, dict) else data
    if st != 200 or not payload:
        add("H5溯源", "major", "H5 API 无数据", raw[:200])
    elif has_qmark(payload):
        add("H5溯源", "major", "H5 API 乱码")

    # QR
    st, data, raw = req("GET", "/api/batch/list?page=1&size=5", token=token)
    payload = (data or {}).get("data") if isinstance(data, dict) else data
    records = payload.get("records") if isinstance(payload, dict) else payload
    if isinstance(records, list) and records:
        bid = records[0].get("batchId") or records[0].get("id")
        st2, d2, r2 = req("GET", f"/api/batch/{bid}/qr-code", token=token)
        p2 = (d2 or {}).get("data") if isinstance(d2, dict) else d2
        print("qr", st2, p2)
        if not p2:
            add("赋码/二维码", "major", "二维码接口无数据")
        else:
            url = str(p2.get("qrContent") or p2.get("url") or p2.get("qrUrl") or "")
            if "localhost" in url or url.startswith("/h5"):
                add("赋码/二维码", "major", f"二维码内容为相对路径或 localhost，手机扫不开: {url}")
            if "trace.local" in url:
                add("赋码/二维码", "major", f"无效域名: {url}")
    else:
        add("赋码管理", "major", "无批次数据")

    # predict features
    st, data, raw = req("GET", "/api/predict/model-info", token=token)
    payload = (data or {}).get("data") if isinstance(data, dict) else data
    feats = (payload or {}).get("features") if isinstance(payload, dict) else None
    if isinstance(feats, list) and any(re.match(r"^[a-zA-Z]", str(f)) for f in feats):
        add("品质预测", "minor", f"特征为英文: {feats}")

    # map config
    st, data, raw = req("GET", "/api/system/map-config")
    payload = (data or {}).get("data") if isinstance(data, dict) else data
    if isinstance(payload, dict) and payload.get("baiduAk"):
        add("地图监控", "info", "已配置百度 AK，若弹窗“服务被禁用”需在百度控制台开通 JS API/认证")

    _dump()
    print(f"\nissues={len(issues)} -> {OUT / 'api_issues.json'}")
    return 0


def _dump():
    report = {
        "generated_at": datetime.now().isoformat(timespec="seconds"),
        "base": BASE,
        "issue_count": len(issues),
        "issues": issues,
    }
    (OUT / "api_issues.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    lines = ["# API 功能测试问题清单\n", f"时间: {report['generated_at']}\n", f"问题数: {len(issues)}\n"]
    for i, it in enumerate(issues, 1):
        lines.append(f"{i}. **[{it['severity']}]** {it['page']} — {it['message']}")
    (OUT / "api_issues.md").write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    raise SystemExit(main())
