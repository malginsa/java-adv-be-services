"""
End-to-end tests for task1 (be-services-task1).

Prerequisites:
  - Application running (default http://localhost:8080)
  - pip install requests pytest

Run:
  python -m pytest e2e-tests.py -v --tb=short
"""

from __future__ import annotations

import os

import pytest
import requests

BASE_URL = os.environ.get("BASE_URL", "http://localhost:8080").rstrip("/")

USER_CREDENTIALS = {"name": "user", "password": "userpassword"}
ADMIN_CREDENTIALS = {"name": "admin", "password": "adminpassword"}


def _url(path: str) -> str:
    return f"{BASE_URL}{path}"


def login(name: str, password: str) -> str:
    response = requests.post(
        _url("/api/auth/login"),
        json={"name": name, "password": password},
        headers={"Content-Type": "application/json"},
        timeout=30,
    )
    response.raise_for_status()
    body = response.json()
    assert "jwt" in body and body["jwt"], "login response must include jwt"
    return body["jwt"]


def auth_headers(token: str) -> dict[str, str]:
    return {"Authorization": f"Bearer {token}"}


@pytest.fixture(scope="module")
def user_token() -> str:
    return login(USER_CREDENTIALS["name"], USER_CREDENTIALS["password"])


@pytest.fixture(scope="module")
def admin_token() -> str:
    return login(ADMIN_CREDENTIALS["name"], ADMIN_CREDENTIALS["password"])


class TestAuthLogin:
    """Obtain JWT — README manual testing."""

    def test_user_login_returns_jwt(self) -> None:
        token = login(USER_CREDENTIALS["name"], USER_CREDENTIALS["password"])
        assert isinstance(token, str) and len(token) > 20

    def test_admin_login_returns_jwt(self) -> None:
        token = login(ADMIN_CREDENTIALS["name"], ADMIN_CREDENTIALS["password"])
        assert isinstance(token, str) and len(token) > 20

    def test_invalid_credentials_rejected(self) -> None:
        response = requests.post(
            _url("/api/auth/login"),
            json={"name": "user", "password": "wrong"},
            headers={"Content-Type": "application/json"},
            timeout=30,
        )
        assert response.status_code in (401, 403)


class TestCustomerCrud:
    """User access testing — customer API with USER role."""

    customer_id: int

    def test_customer_create(self, user_token: str) -> None:
        headers = {**auth_headers(user_token), "Content-Type": "application/json"}
        create_response = requests.post(
            _url("/api/customer"),
            json={"name": "Alex", "address": "Finland"},
            headers=headers,
            timeout=30,
        )
        create_response.raise_for_status()
        customer_id = create_response.json()
        assert isinstance(customer_id, int)
        TestCustomerCrud.customer_id = customer_id

    def test_customer_get(self, user_token: str) -> None:
        customer_id = TestCustomerCrud.customer_id
        get_response = requests.get(
            _url(f"/api/customer/{customer_id}"),
            headers=auth_headers(user_token),
            timeout=30,
        )
        get_response.raise_for_status()
        customer = get_response.json()
        assert customer["id"] == customer_id
        assert customer["name"] == "Alex"
        assert customer["address"] == "Finland"

    def test_customer_update(self, user_token: str) -> None:
        customer_id = TestCustomerCrud.customer_id
        headers = {**auth_headers(user_token), "Content-Type": "application/json"}
        update_response = requests.put(
            _url("/api/customer"),
            json={"id": customer_id, "name": "Alex", "address": "Poland"},
            headers=headers,
            timeout=30,
        )
        update_response.raise_for_status()
        assert update_response.json() == customer_id

        get_after_update = requests.get(
            _url(f"/api/customer/{customer_id}"),
            headers=auth_headers(user_token),
            timeout=30,
        )
        get_after_update.raise_for_status()
        updated = get_after_update.json()
        assert updated["address"] == "Poland"

    def test_customer_delete(self, user_token: str) -> None:
        customer_id = TestCustomerCrud.customer_id
        delete_response = requests.delete(
            _url(f"/api/customer/{customer_id}"),
            headers=auth_headers(user_token),
            timeout=30,
        )
        assert delete_response.status_code == 204

        missing_response = requests.get(
            _url(f"/api/customer/{customer_id}"),
            headers=auth_headers(user_token),
            timeout=30,
        )
        assert missing_response.status_code == 400

    def test_customer_endpoints_require_authentication(self) -> None:
        response = requests.post(
            _url("/api/customer"),
            json={"name": "Alex", "address": "Finland"},
            headers={"Content-Type": "application/json"},
            timeout=30,
        )
        assert response.status_code in (401, 403)


class TestAdminAccess:
    """Admin access testing."""

    def test_admin_cms_with_admin_token(self, admin_token: str) -> None:
        response = requests.get(
            _url("/api/admin/cms"),
            headers=auth_headers(admin_token),
            timeout=30,
        )
        response.raise_for_status()
        assert response.text == "Admin page stub"


class TestActuator:
    """Actuator health and Prometheus metrics (admin JWT)."""

    def test_actuator_health(self, admin_token: str) -> None:
        response = requests.get(
            _url("/actuator/health"),
            headers=auth_headers(admin_token),
            timeout=30,
        )
        response.raise_for_status()
        payload = response.json()
        assert payload.get("status") == "UP"

    def test_actuator_prometheus_metrics(self, admin_token: str) -> None:
        response = requests.get(
            _url("/actuator/prometheus"),
            headers=auth_headers(admin_token),
            timeout=30,
        )
        response.raise_for_status()
        assert "text/plain" in response.headers.get("Content-Type", "")
        body = response.text
        assert "customer_metrics" in body or "customer.metrics" in body or len(body) > 0
