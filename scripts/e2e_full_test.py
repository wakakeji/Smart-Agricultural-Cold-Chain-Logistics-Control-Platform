# -*- coding: utf-8 -*-
"""Full UI smoke test — record issues, do not fix here."""
from __future__ import annotations

import json
import re
import sys
from datetime import datetime
from pathlib import Path

from playwright.sync_api import sync_playwright

BASE = "http://localhost:5173"
OUT_DIR = Path(__file__).resolve().parent / "e2e_results"
OUT_DIR.mkdir(parents=True, exist_ok=True)

PAGES = [
    ("/map", "地图监控"),
    ("/dashboard", "指挥大屏"),
    ("/alarm", "预警管理"),
    ("/monitor", "实时数据"),
    ("/data-quality", "数据质量"),
    ("/code", "赋码管理"),
    ("/trace", "追溯查询"),
    ("/blockchain", "区块链存证"),
    ("/predict", "品质预测"),
    ("/suggestion", "决策建议"),
    ("/route", "路线规划"),
    ("/transport", "运输监控"),
    ("/loss", "损耗率"),
    ("/carbon", "碳排放"),
    ("/user", "用户权限"),
    ("/service-monitor", "系统服务监控"),
    ("/api-manage", "API管理"),
    ("/h5/trace?batchNo=20260716000001", "H5溯源"),
]

issues: list[dict] = []


def add_issue(page: str, severity: str, msg: str, detail: str = ""):
    issues.append(
        {
            "page": page,
            "severity": severity,
            "message": msg,
            "detail": detail[:500],
            "time": datetime.now().isoformat(timespec="seconds"),
        }
    )
    print(f"  [{severity}] {page}: {msg}")


def count_qmarks(text: str) -> int:
    return len(re.findall(r"\?{2,}", text or ""))


def login(page):
    page.goto(f"{BASE}/login", wait_until="networkidle")
    page.wait_for_timeout(500)
    # fill username/password
    inputs = page.locator("input")
    # Element Plus often has username then password
    user = page.get_by_placeholder(re.compile("用户名|手机"))
    pwd = page.get_by_placeholder(re.compile("密码|请输入"))
    if user.count():
        user.first.fill("admin")
    else:
        inputs.nth(0).fill("admin")
    if pwd.count():
        pwd.first.fill("Abc@123456")
    else:
        inputs.nth(1).fill("Abc@123456")
    page.get_by_role("button", name=re.compile("登录|登 录")).click()
    page.wait_for_timeout(2000)
    if "/login" in page.url:
        add_issue("login", "critical", "登录失败，仍停留在登录页", page.url)
        page.screenshot(path=str(OUT_DIR / "login_fail.png"))
        return False
    print("OK login ->", page.url)
    return True


def check_sidebar_scroll(page):
    page.goto(f"{BASE}/map", wait_until="networkidle")
    page.wait_for_timeout(800)
    info = page.evaluate(
        """() => {
      const aside = document.querySelector('.aside') || document.querySelector('.el-aside');
      if (!aside) return { found: false };
      const style = getComputedStyle(aside);
      return {
        found: true,
        overflow: style.overflow,
        overflowY: style.overflowY,
        scrollHeight: aside.scrollHeight,
        clientHeight: aside.clientHeight,
        canScroll: aside.scrollHeight > aside.clientHeight + 2,
      };
    }"""
    )
    if not info.get("found"):
        add_issue("sidebar", "major", "未找到侧边栏 .aside")
        return
    if info.get("canScroll") and info.get("overflowY") in ("hidden", "visible"):
        add_issue(
            "sidebar",
            "major",
            "菜单内容超出高度但 overflow 禁止滚动",
            json.dumps(info, ensure_ascii=False),
        )
    elif not info.get("canScroll"):
        # still note overflow setting
        if info.get("overflowY") == "hidden":
            add_issue(
                "sidebar",
                "minor",
                "侧边栏 overflow:hidden，菜单项多时将无法滚动",
                json.dumps(info, ensure_ascii=False),
            )


def visit_page(page, path: str, title: str):
    url = BASE + path
    print(f"\n== {title} {path}")
    try:
        page.goto(url, wait_until="networkidle", timeout=30000)
    except Exception as e:
        add_issue(title, "critical", f"页面加载失败: {e}")
        return
    page.wait_for_timeout(1200)

    # dismiss dialogs (Baidu map alert etc.)
    page.on("dialog", lambda d: d.dismiss())
    try:
        page.evaluate("() => { /* noop */ }")
    except Exception:
        pass

    # capture dialogs via handler already registered globally below
    body = page.inner_text("body")
    qm = count_qmarks(body)
    if qm >= 3:
        add_issue(title, "major", f"页面出现大量问号乱码({qm}处)", body[:300])

    # empty states
    if page.locator(".el-empty").count() > 0:
        empty_text = page.locator(".el-empty").inner_text()
        add_issue(title, "info", "存在空状态提示", empty_text[:200])

    # console errors collected globally
    shot = OUT_DIR / f"{title.replace('/', '_')}.png"
    page.screenshot(path=str(shot), full_page=True)

    # page-specific checks
    if path.startswith("/trace") and "h5" not in path:
        # try query with seeded batch
        batch_input = page.get_by_placeholder(re.compile("如|批次"))
        if batch_input.count():
            batch_input.first.fill("20260716000001")
            page.get_by_role("button", name="查询").click()
            page.wait_for_timeout(1500)
            body2 = page.inner_text("body")
            if "未找到" in body2 or page.locator(".el-empty").count():
                add_issue("追溯查询", "major", "使用种子批次 20260716000001 查询无结果或未找到")
            elif count_qmarks(body2) >= 3:
                add_issue("追溯查询", "major", "查询结果含乱码", body2[:300])
            else:
                print("  trace query OK")
        else:
            add_issue("追溯查询", "major", "未找到批次号输入框")

    if path.startswith("/code"):
        # QR dialog
        rows = page.locator(".el-table__row")
        if rows.count() == 0:
            add_issue("赋码管理", "major", "批次列表为空，无法测二维码")
        else:
            # click 二维码 if exists
            btn = page.get_by_role("button", name=re.compile("二维码|查看"))
            if btn.count():
                btn.first.click()
                page.wait_for_timeout(800)
                canvas = page.locator("canvas")
                img = page.locator("img[src*='data:image']")
                if canvas.count() == 0 and img.count() == 0:
                    add_issue("赋码管理", "major", "二维码弹窗未渲染 canvas/img")
                else:
                    # check QR URL text
                    dialog = page.locator(".el-dialog").inner_text() if page.locator(".el-dialog").count() else ""
                    if "localhost" in dialog:
                        add_issue(
                            "赋码管理",
                            "major",
                            "二维码指向 localhost，手机扫码无法访问",
                            dialog[:200],
                        )
                    if "trace.local" in dialog:
                        add_issue("赋码管理", "major", "二维码含无效域名 trace.local")
            else:
                add_issue("赋码管理", "minor", "未找到二维码按钮")

    if path.startswith("/transport"):
        if "??" in body or re.search(r"\?\?\s*->\s*\?\?", body):
            add_issue("运输监控", "major", "路线/车辆字段乱码或缺失", body[:400])
        if re.search(r"车辆[\s\S]{0,80}-\s", body) or "| - |" in body:
            pass
        # vehicle dash
        if page.locator("td", has_text=re.compile(r"^-$")).count() >= 2:
            add_issue("运输监控", "major", "车辆列多为 '-'，关联设施名可能缺失")

    if path.startswith("/alarm"):
        if count_qmarks(body) >= 2:
            add_issue("预警管理", "major", "来源/内容存在乱码", body[:400])

    if path.startswith("/predict"):
        for eng in ["avgTemp", "avgHumidity", "transportHours", "vibration", "storageDays"]:
            if eng in body:
                add_issue("品质预测", "minor", f"特征仍显示英文: {eng}")
                break
        if "ColdChain" in body or "stub" in body.lower():
            add_issue("品质预测", "info", "模型为 stub 演示，非真实训练模型")

    if path.startswith("/api-manage"):
        if "ONLINE" in body:
            add_issue("API管理", "minor", "状态仍显示英文 ONLINE")
        if count_qmarks(body) >= 2:
            add_issue("API管理", "major", "API名称等出现乱码")

    if "h5/trace" in path:
        if page.locator(".el-empty").count() or "暂无" in body or "未找到" in body:
            add_issue("H5溯源", "major", "H5 溯源无数据或不显示", body[:300])
        if count_qmarks(body) >= 2:
            add_issue("H5溯源", "major", "H5 页面乱码")


def main() -> int:
    console_errors: list[str] = []
    with sync_playwright() as p:
        browser = p.chromium.launch(headless=True)
        context = browser.new_context(locale="zh-CN", viewport={"width": 1440, "height": 900})
        page = context.new_page()

        def on_dialog(d):
            add_issue("dialog", "major", f"弹窗: {d.message}")
            d.dismiss()

        page.on("dialog", on_dialog)

        def on_console(msg):
            if msg.type == "error":
                console_errors.append(msg.text)

        page.on("console", on_console)

        if not login(page):
            browser.close()
            _write_report(console_errors)
            return 1

        # header user name
        header = page.locator(".user").inner_text() if page.locator(".user").count() else ""
        if "?" in header:
            add_issue("header", "major", f"顶部用户名乱码: {header}")

        check_sidebar_scroll(page)

        for path, title in PAGES:
            try:
                visit_page(page, path, title)
            except Exception as e:
                add_issue(title, "critical", f"测试异常: {e}")

        browser.close()

    _write_report(console_errors)
    print(f"\nTotal issues: {len(issues)}")
    print(f"Report: {OUT_DIR / 'issues.json'}")
    return 0 if not any(i["severity"] == "critical" for i in issues) else 2


def _write_report(console_errors: list[str]):
    report = {
        "generated_at": datetime.now().isoformat(timespec="seconds"),
        "base": BASE,
        "issue_count": len(issues),
        "issues": issues,
        "console_errors_sample": console_errors[:40],
    }
    (OUT_DIR / "issues.json").write_text(json.dumps(report, ensure_ascii=False, indent=2), encoding="utf-8")
    # markdown summary
    lines = ["# E2E 测试问题清单\n", f"生成时间: {report['generated_at']}\n", f"问题数: {len(issues)}\n"]
    for i, it in enumerate(issues, 1):
        lines.append(f"{i}. **[{it['severity']}]** `{it['page']}` — {it['message']}")
        if it.get("detail"):
            lines.append(f"   - detail: {it['detail'][:200]}")
    (OUT_DIR / "issues.md").write_text("\n".join(lines), encoding="utf-8")


if __name__ == "__main__":
    sys.exit(main())
