"""End-to-end test. Start DB (docker compose up -d) and app (mvn spring-boot:run) first.

Usage: python e2e_test.py [base_url]
Note: the brute-force test blocks locktest@example.com for 5 minutes; it is skipped if already blocked.
"""
import re
import sys

import requests

BASE = sys.argv[1] if len(sys.argv) > 1 else "http://localhost:8080"
ADMIN = ("admin@example.com", "Admin#123")           # VIEW_INFO + VIEW_ADMIN
INFO = ("info@example.com", "Info#123")              # VIEW_INFO
ADMIN_ONLY = ("adminonly@example.com", "AdminOnly#123")  # VIEW_ADMIN
LOCK = ("locktest@example.com", "LockTest#123")

passed = failed = 0


def check(name, cond, detail=""):
    global passed, failed
    if cond:
        passed += 1
        print(f"[PASS] {name}")
    else:
        failed += 1
        print(f"[FAIL] {name} {detail}")


def login(creds):
    """Returns (session, response of the POST /login, without following redirects)."""
    s = requests.Session()
    page = s.get(f"{BASE}/login")
    token = re.search(r'name="_csrf"[^>]*value="([^"]+)"', page.text) \
        or re.search(r'value="([^"]+)"[^>]*name="_csrf"', page.text)
    r = s.post(f"{BASE}/login", allow_redirects=False,
               data={"email": creds[0], "password": creds[1], "_csrf": token.group(1)})
    return s, r


def ok_login(r):
    return r.status_code == 302 and "/login" not in r.headers["Location"]


def get(s, path):
    return s.get(f"{BASE}{path}", allow_redirects=False)


# --- public / anonymous
anon = requests.Session()
check("GET /about is public", get(anon, "/about").status_code == 200)
for p in ("/info", "/admin", "/blocked-users", "/"):
    r = get(anon, p)
    check(f"anonymous {p} redirected to login",
          r.status_code == 302 and "/login" in r.headers["Location"], r.status_code)
check("login page is public", get(anon, "/login").status_code == 200)

# --- permissions matrix
for name, creds, info, admin in (("admin", ADMIN, 200, 200),
                                 ("info", INFO, 200, 403),
                                 ("adminonly", ADMIN_ONLY, 403, 200)):
    s, r = login(creds)
    check(f"{name}: login succeeds", ok_login(r), r.headers.get("Location"))
    check(f"{name}: /about 200", get(s, "/about").status_code == 200)
    check(f"{name}: /info {info}", get(s, "/info").status_code == info)
    check(f"{name}: /admin {admin}", get(s, "/admin").status_code == admin)
    check(f"{name}: /blocked-users {admin}", get(s, "/blocked-users").status_code == admin)

s, _ = login(ADMIN)
body = get(s, "/info").json()
check("/info returns stats", "randomStat" in body and body["application"] == "MVC application", body)

# --- bad credentials
_, r = login((ADMIN[0], "wrong"))
check("wrong password -> /login?error", r.status_code == 302 and r.headers["Location"].endswith("/login?error"),
      r.headers.get("Location"))
_, r = login(("nobody@example.com", "x"))
check("unknown user -> /login?error", r.headers.get("Location", "").endswith("/login?error"))

# --- logout
s, _ = login(INFO)
page = s.get(f"{BASE}/")
token = re.search(r'name="_csrf"[^>]*value="([^"]+)"', page.text).group(1)
r = s.post(f"{BASE}/logout", data={"_csrf": token}, allow_redirects=False)
check("logout redirects to /login?logout", r.status_code == 302 and r.headers["Location"].endswith("/login?logout"),
      r.headers.get("Location"))
check("after logout /info needs login", get(s, "/info").status_code == 302)

# --- brute force
admin_s, _ = login(ADMIN)
already = any(u["email"] == LOCK[0] for u in get(admin_s, "/blocked-users").json())
if already:
    print("[SKIP] brute force: locktest user already blocked (wait 5 minutes)")
else:
    for i in range(1, 4):
        _, r = login((LOCK[0], "bad"))
        check(f"attempt {i} wrong password -> error", r.headers["Location"].endswith("/login?error"))
    _, r = login(LOCK)
    check("correct password rejected while blocked", r.headers["Location"].endswith("/login?blocked"),
          r.headers.get("Location"))
    blocked = get(admin_s, "/blocked-users").json()
    entry = next((u for u in blocked if u["email"] == LOCK[0]), None)
    check("/blocked-users lists blocked email", entry is not None, blocked)
    check("block lasts ~5 minutes", entry is not None and 290 <= entry["remainingSeconds"] <= 300, entry)
    check("non-admin cannot see blocked users", get(login(INFO)[0], "/blocked-users").status_code == 403)

print(f"\n{passed} passed, {failed} failed")
sys.exit(1 if failed else 0)
