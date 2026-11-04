"""
End-to-end tests for task1 (be-services-task1).

Prerequisites:
  - Application running (default http://localhost:8080); restart after code changes
  - pip install requests pytest

Run:
  python -m pytest e2e-tests.py -v --tb=short
"""

from __future__ import annotations

import os

import pytest
import requests

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080").rstrip("/")
REQUEST_TIMEOUT = 30
CONNECT_TIMEOUT = 3

USER_CREDENTIALS = {"name": "user", "password": "userpassword"}
ADMIN_CREDENTIALS = {"name": "admin", "password": "adminpassword"}


def _url(path: str) -> str:
    return f"{BASE_URL}{path}"


def _request(method: str, path: str, **kwargs) -> requests.Response:
    timeout = kwargs.pop("timeout", REQUEST_TIMEOUT)
    return requests.request(method, _url(path), timeout=timeout, **kwargs)


@pytest.fixture(scope="session", autouse=True)
def require_running_app() -> None:
    try:
        response = _request("GET", "/actuator/health", timeout=CONNECT_TIMEOUT)
    except requests.RequestException as exc:
        pytest.exit(
            f"Cannot reach {BASE_URL} ({exc}). "
            "Start the app: mvn spring-boot:run",
            returncode=1,
        )
    if response.status_code == 403:
        pytest.exit(
            "Actuator /actuator/health returned 403 without JWT. "
            "Stop any old process on port 8080 and restart the app.",
            returncode=1,
        )
    if response.status_code != 200:
        pytest.exit(
            f"Unexpected /actuator/health status {response.status_code}. "
            "Ensure the application started successfully.",
            returncode=1,
        )


def login(name: str, password: str) -> str:
    response = _request(
        "POST",
        "/api/auth/login",
        json={"name": name, "password": password},
        headers={"Content-Type": "application/json"},
    )
    response.raise_for_status()
    body = response.json()
    assert "jwt" in body and body["jwt"], "login response must include jwt"
    return body["jwt"]


def auth_headers(token: str) -> dict[str, str]:
    return {"Authorization": f"Bearer {token}"}


def json_headers(token: str) -> dict[str, str]:
    return {**auth_headers(token), "Content-Type": "application/json"}


@pytest.fixture(scope="module")
def user_token() -> str:
    return login(USER_CREDENTIALS["name"], USER_CREDENTIALS["password"])


@pytest.fixture(scope="module")
def admin_token() -> str:
    return login(ADMIN_CREDENTIALS["name"], ADMIN_CREDENTIALS["password"])


@pytest.fixture(scope="module")
def customer_id(user_token: str) -> int:
    response = _request(
        "POST",
        "/api/customer",
        json={"name": "Alex", "address": "Finland"},
        headers=json_headers(user_token),
    )
    response.raise_for_status()
    created_id = response.json()
    assert isinstance(created_id, int)
    return created_id


class TestAuthLogin:
    """Obtain JWT — README manual testing."""

    def test_user_login_returns_jwt(self) -> None:
        token = login(USER_CREDENTIALS["name"], USER_CREDENTIALS["password"])
        assert isinstance(token, str) and len(token) > 20

    def test_admin_login_returns_jwt(self) -> None:
        token = login(ADMIN_CREDENTIALS["name"], ADMIN_CREDENTIALS["password"])
        assert isinstance(token, str) and len(token) > 20

    def test_invalid_credentials_rejected(self) -> None:
        response = _request(
            "POST",
            "/api/auth/login",
            json={"name": "user", "password": "wrong"},
            headers={"Content-Type": "application/json"},
        )
        assert response.status_code in (401, 403)


class TestInvalidJwt:
    """Protected APIs must reject invalid Bearer tokens."""

    def test_malformed_jwt_rejected(self) -> None:
        response = _request(
            "GET",
            "/api/admin/cms",
            headers=auth_headers("not.a.valid.jwt"),
        )
        assert response.status_code in (401, 403)

    def test_tampered_jwt_rejected(self, user_token: str) -> None:
        tampered = user_token[:-1] + ("a" if user_token[-1] != "a" else "b")
        response = _request(
            "GET",
            "/api/admin/cms",
            headers=auth_headers(tampered),
        )
        assert response.status_code in (401, 403)


class TestCustomerCrud:
    """User access testing — customer API with USER role."""

    def test_customer_create(self, customer_id: int) -> None:
        assert customer_id > 0

    def test_customer_get(self, user_token: str, customer_id: int) -> None:
        response = _request(
            "GET",
            f"/api/customer/{customer_id}",
            headers=auth_headers(user_token),
        )
        response.raise_for_status()
        customer = response.json()
        assert customer["id"] == customer_id
        assert customer["name"] == "Alex"
        assert customer["address"] == "Finland"

    def test_customer_update(self, user_token: str, customer_id: int) -> None:
        response = _request(
            "PUT",
            "/api/customer",
            json={"id": customer_id, "name": "Alex", "address": "Poland"},
            headers=json_headers(user_token),
        )
        response.raise_for_status()
        updated = response.json()
        assert updated["id"] == customer_id
        assert updated["name"] == "Alex"
        assert updated["address"] == "Poland"

    def test_customer_delete(self, user_token: str, customer_id: int) -> None:
        response = _request(
            "DELETE",
            f"/api/customer/{customer_id}",
            headers=auth_headers(user_token),
        )
        assert response.status_code == 204

        missing = _request(
            "GET",
            f"/api/customer/{customer_id}",
            headers=auth_headers(user_token),
        )
        assert missing.status_code == 404
        assert str(customer_id) in missing.text

    def test_customer_endpoints_require_authentication(self) -> None:
        response = _request(
            "POST",
            "/api/customer",
            json={"name": "Alex", "address": "Finland"},
            headers={"Content-Type": "application/json"},
        )
        assert response.status_code in (401, 403)


class TestAdminAccess:
    """Admin access testing."""

    def test_admin_cms_with_admin_token(self, admin_token: str) -> None:
        response = _request(
            "GET",
            "/api/admin/cms",
            headers=auth_headers(admin_token),
        )
        response.raise_for_status()
        assert response.text == "Admin page stub"


class TestActuator:
    """Actuator health and Prometheus metrics (public, no JWT)."""

    def test_actuator_health(self) -> None:
        response = _request("GET", "/actuator/health")
        response.raise_for_status()
        payload = response.json()
        assert payload.get("status") == "UP"

    def test_actuator_prometheus_metrics(self) -> None:
        response = _request("GET", "/actuator/prometheus")
        response.raise_for_status()
        assert "text/plain" in response.headers.get("Content-Type", "")
        body = response.text
        assert "customer_metrics" in body or "customer.metrics" in body
