#!/usr/bin/env bash
set -euo pipefail

API_BASE_URL="${API_BASE_URL:-http://localhost:8080/api}"
USERNAME="${USERNAME:-demo_user_01}"
EMAIL="${EMAIL:-demo.user.01@example.com}"
PASSWORD="${PASSWORD:-DemoPass123}"

JSON_HEADER="Content-Type: application/json"

if ! command -v curl >/dev/null 2>&1; then
  echo "curl is required."
  exit 1
fi

if ! command -v jq >/dev/null 2>&1; then
  echo "jq is required."
  exit 1
fi

echo "==> API: ${API_BASE_URL}"
echo "==> Seeding user: ${USERNAME} / ${EMAIL}"

health_url="${API_BASE_URL%/api}/api/health"
if ! curl -fsS "${health_url}" >/dev/null 2>&1; then
  host_ip="$(awk '/nameserver/ {print $2; exit}' /etc/resolv.conf 2>/dev/null || true)"

  if [ -n "${host_ip}" ]; then
    fallback_api_base_url="http://${host_ip}:8080/api"
    fallback_health_url="${fallback_api_base_url%/api}/api/health"

    if curl -fsS "${fallback_health_url}" >/dev/null 2>&1; then
      API_BASE_URL="${fallback_api_base_url}"
      echo "==> localhost khong reachable trong WSL. Fallback sang host Windows: ${API_BASE_URL}"
    fi
  fi
fi

health_url="${API_BASE_URL%/api}/api/health"
if ! curl -fsS "${health_url}" >/dev/null 2>&1; then
  echo "Khong ket noi duoc backend qua: ${API_BASE_URL}"
  echo "Vui long dam bao backend dang chay va mo cong 8080."
  exit 1
fi

register_payload="$(jq -n \
  --arg username "${USERNAME}" \
  --arg email "${EMAIL}" \
  --arg password "${PASSWORD}" \
  '{username:$username,email:$email,password:$password}')"

# Register may fail if user already exists. Continue to login in that case.
set +e
register_response="$(curl -sS -X POST "${API_BASE_URL}/auth/register" -H "${JSON_HEADER}" -d "${register_payload}")"
register_status=$?
set -e

if [ "${register_status}" -ne 0 ]; then
  echo "Register call failed to execute."
  exit 1
fi

login_payload="$(jq -n \
  --arg usernameOrEmail "${EMAIL}" \
  --arg password "${PASSWORD}" \
  '{usernameOrEmail:$usernameOrEmail,password:$password}')"

login_response="$(curl -sS -X POST "${API_BASE_URL}/auth/login" -H "${JSON_HEADER}" -d "${login_payload}")"
success="$(echo "${login_response}" | jq -r '.success // false')"

if [ "${success}" != "true" ]; then
  echo "Login failed:"
  echo "${login_response}" | jq .
  exit 1
fi

access_token="$(echo "${login_response}" | jq -r '.data.accessToken')"
refresh_token="$(echo "${login_response}" | jq -r '.data.refreshToken')"
auth_header="Authorization: Bearer ${access_token}"

create_wallet() {
  local name="$1"
  local type="$2"
  local balance="$3"
  local description="$4"

  local payload
  payload="$(jq -n \
    --arg name "${name}" \
    --arg type "${type}" \
    --arg balance "${balance}" \
    --arg description "${description}" \
    '{name:$name,type:$type,balance:($balance|tonumber),description:$description}')"

  curl -sS -X POST "${API_BASE_URL}/wallets" \
    -H "${JSON_HEADER}" \
    -H "${auth_header}" \
    -d "${payload}" | jq -r '.data.id'
}

create_category() {
  local name="$1"
  local type="$2"
  local icon="$3"
  local color="$4"

  local payload
  payload="$(jq -n \
    --arg name "${name}" \
    --arg type "${type}" \
    --arg icon "${icon}" \
    --arg color "${color}" \
    '{name:$name,type:$type,icon:$icon,color:$color}')"

  curl -sS -X POST "${API_BASE_URL}/categories" \
    -H "${JSON_HEADER}" \
    -H "${auth_header}" \
    -d "${payload}" | jq -r '.data.id'
}

create_income() {
  local wallet_id="$1"
  local category_id="$2"
  local amount="$3"
  local title="$4"
  local note="$5"
  local transaction_date="$6"

  local payload
  payload="$(jq -n \
    --arg walletId "${wallet_id}" \
    --arg categoryId "${category_id}" \
    --arg amount "${amount}" \
    --arg title "${title}" \
    --arg note "${note}" \
    --arg transactionDate "${transaction_date}" \
    '{walletId:$walletId,categoryId:$categoryId,amount:($amount|tonumber),title:$title,note:$note,transactionDate:$transactionDate}')"

  curl -sS -X POST "${API_BASE_URL}/transactions/income" \
    -H "${JSON_HEADER}" \
    -H "${auth_header}" \
    -d "${payload}" >/dev/null
}

create_expense() {
  local wallet_id="$1"
  local category_id="$2"
  local amount="$3"
  local title="$4"
  local note="$5"
  local transaction_date="$6"

  local payload
  payload="$(jq -n \
    --arg walletId "${wallet_id}" \
    --arg categoryId "${category_id}" \
    --arg amount "${amount}" \
    --arg title "${title}" \
    --arg note "${note}" \
    --arg transactionDate "${transaction_date}" \
    '{walletId:$walletId,categoryId:$categoryId,amount:($amount|tonumber),title:$title,note:$note,transactionDate:$transactionDate}')"

  curl -sS -X POST "${API_BASE_URL}/transactions/expense" \
    -H "${JSON_HEADER}" \
    -H "${auth_header}" \
    -d "${payload}" >/dev/null
}

create_transfer() {
  local source_wallet_id="$1"
  local destination_wallet_id="$2"
  local amount="$3"
  local title="$4"
  local note="$5"
  local transaction_date="$6"

  local payload
  payload="$(jq -n \
    --arg sourceWalletId "${source_wallet_id}" \
    --arg destinationWalletId "${destination_wallet_id}" \
    --arg amount "${amount}" \
    --arg title "${title}" \
    --arg note "${note}" \
    --arg transactionDate "${transaction_date}" \
    '{sourceWalletId:$sourceWalletId,destinationWalletId:$destinationWalletId,amount:($amount|tonumber),title:$title,note:$note,transactionDate:$transactionDate}')"

  curl -sS -X POST "${API_BASE_URL}/transactions/transfer" \
    -H "${JSON_HEADER}" \
    -H "${auth_header}" \
    -d "${payload}" >/dev/null
}

echo "==> Creating wallets..."
wallet_cash_id="$(create_wallet "Tien mat" "CASH" "2500000" "Vi tien mat test")"
wallet_bank_id="$(create_wallet "Vietcombank" "BANK" "18000000" "Tai khoan ngan hang test")"
wallet_saving_id="$(create_wallet "Tiet kiem" "SAVINGS" "12000000" "Vi tiet kiem test")"

echo "==> Creating categories..."
cat_salary_id="$(create_category "Luong" "INCOME" "wallet" "#1f9d7a")"
cat_freelance_id="$(create_category "Freelance" "INCOME" "briefcase" "#18a574")"
cat_food_id="$(create_category "An uong" "EXPENSE" "utensils" "#df5c50")"
cat_transport_id="$(create_category "Di chuyen" "EXPENSE" "bus" "#e06a5d")"
cat_entertain_id="$(create_category "Giai tri" "EXPENSE" "film" "#d2554b")"

echo "==> Creating transactions..."
create_income "${wallet_bank_id}" "${cat_salary_id}" "22000000" "Luong thang" "Luong co ban thang nay" "2026-05-03T09:00:00"
create_income "${wallet_bank_id}" "${cat_freelance_id}" "4500000" "Du an freelance" "Thanh toan milestone A" "2026-05-09T20:30:00"
create_expense "${wallet_cash_id}" "${cat_food_id}" "180000" "Bua trua" "Com van phong" "2026-05-10T12:10:00"
create_expense "${wallet_cash_id}" "${cat_transport_id}" "250000" "Do xang" "Do xang xe may" "2026-05-11T08:00:00"
create_expense "${wallet_bank_id}" "${cat_entertain_id}" "790000" "Xem phim + an toi" "Cuoi tuan" "2026-05-12T19:45:00"
create_expense "${wallet_bank_id}" "${cat_food_id}" "420000" "An toi voi ban" "Nha hang" "2026-05-13T19:00:00"
create_transfer "${wallet_bank_id}" "${wallet_cash_id}" "2000000" "Rut tien mat" "Rut tu tai khoan ve vi tien mat" "2026-05-14T10:30:00"
create_transfer "${wallet_bank_id}" "${wallet_saving_id}" "3500000" "Nap tiet kiem" "Chuyen vao vi tiet kiem" "2026-05-14T21:15:00"

echo
echo "=============================="
echo "Seed completed."
echo "API base URL: ${API_BASE_URL}"
echo "Login email: ${EMAIL}"
echo "Login password: ${PASSWORD}"
echo "Refresh token (for logout test): ${refresh_token}"
echo "Wallet IDs:"
echo "  CASH:    ${wallet_cash_id}"
echo "  BANK:    ${wallet_bank_id}"
echo "  SAVINGS: ${wallet_saving_id}"
echo "=============================="
