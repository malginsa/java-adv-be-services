"""
Read Spring Boot Actuator health indicators from task1.

Uses admin JWT (actuator endpoints require authentication).

Usage:
  pip install requests
  python read_indicators.py
  python read_indicators.py --base-url http://localhost:8080
  python read_indicators.py --json
"""

from __future__ import annotations

import argparse
import json
import os
import sys

import requests

DEFAULT_BASE_URL = "http://localhost:8080"
ADMIN_CREDENTIALS = {"name": "admin", "password": "adminpassword"}


def login(base_url: str) -> str:
    response = requests.post(
        f"{base_url.rstrip('/')}/api/auth/login",
        json=ADMIN_CREDENTIALS,
        headers={"Content-Type": "application/json"},
        timeout=30,
    )
    response.raise_for_status()
    body = response.json()
    token = body.get("jwt")
    if not token:
        raise RuntimeError("Login response did not include jwt")
    return token


def fetch_health(base_url: str, token: str) -> dict:
    response = requests.get(
        f"{base_url.rstrip('/')}/actuator/health",
        headers={"Authorization": f"Bearer {token}"},
        timeout=30,
    )
    response.raise_for_status()
    return response.json()


def extract_indicators(health: dict) -> dict[str, dict]:
    """Return named health components (custom HealthIndicators and built-ins)."""
    components = health.get("components")
    if isinstance(components, dict):
        return components
    # Single-component or legacy shape: treat root as one indicator
    return {"application": health}


def format_indicators(indicators: dict[str, dict]) -> str:
    lines: list[str] = []
    for name in sorted(indicators.keys()):
        component = indicators[name]
        status = component.get("status", "UNKNOWN")
        lines.append(f"{name}: {status}")
        details = component.get("details")
        if isinstance(details, dict):
            for key, value in sorted(details.items()):
                lines.append(f"  {key}: {value}")
    return "\n".join(lines)


def main(argv: list[str] | None = None) -> int:
    parser = argparse.ArgumentParser(description="Read actuator health indicators.")
    parser.add_argument(
        "--base-url",
        default=os.environ.get("BASE_URL", DEFAULT_BASE_URL),
        help="Application base URL (default: BASE_URL env or http://localhost:8080)",
    )
    parser.add_argument(
        "--json",
        action="store_true",
        help="Print indicators as JSON",
    )
    args = parser.parse_args(argv)
    base_url = args.base_url.rstrip("/")

    try:
        token = login(base_url)
        health = fetch_health(base_url, token)
    except requests.RequestException as exc:
        print(f"Failed to read indicators: {exc}", file=sys.stderr)
        return 1

    overall = health.get("status", "UNKNOWN")
    indicators = extract_indicators(health)

    if args.json:
        payload = {"status": overall, "indicators": indicators}
        print(json.dumps(payload, indent=2))
    else:
        print(f"Overall status: {overall}")
        print()
        print(format_indicators(indicators))

    return 0 if overall == "UP" else 1


if __name__ == "__main__":
    raise SystemExit(main())
