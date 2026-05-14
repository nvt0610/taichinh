# FINANCE_TRACKER_DB_DESIGN.md

# Finance Tracker MVP Database Design

## Overview

Database design cho dự án Personal Finance / Expense Tracker.

Mục tiêu:

* MVP nhưng production-oriented
* dễ mở rộng trong tương lai
* relation rõ ràng
* hỗ trợ JWT authentication
* hỗ trợ soft delete
* audit fields đầy đủ
* clean architecture

---

# Database Philosophy

Thiết kế hiện tại tập trung vào:

* đơn giản nhưng thực tế
* tránh over-engineering
* scale tốt trong tương lai
* giữ core domain rõ ràng

Core domain gồm:

```txt id="wttz2n"
User
Wallet
Category
Transaction
```

Đây là foundation chính của toàn bộ hệ thống.

---

# Common Audit Fields

Các table chính sẽ sử dụng audit fields chuẩn:

```sql id="9e1d2f"
created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL
deleted_at TIMESTAMP NULL
```

## Ý nghĩa

### created_at

Thời điểm tạo dữ liệu.

### updated_at

Thời điểm cập nhật cuối cùng.

### deleted_at

Soft delete timestamp.

Nếu:

```txt id="q0qnt0"
deleted_at IS NULL
```

→ record còn hoạt động.

Nếu:

```txt id="bq8qz8"
deleted_at IS NOT NULL
```

→ record đã bị soft delete.

---

# Soft Delete Strategy

Hệ thống sử dụng:

```txt id="7tbjlwm"
soft delete + partial unique index
```

Đây là hướng production-grade phù hợp với PostgreSQL.

## Ví dụ

```sql id="3ogp4w"
CREATE UNIQUE INDEX uq_users_email_active
ON users(email)
WHERE deleted_at IS NULL;
```

## Ý nghĩa

Unique chỉ áp dụng cho:

```txt id="w9xqcu"
record chưa bị delete
```

Điều này cho phép:

* reuse email sau khi soft delete
* vẫn giữ data integrity
* vẫn restore được nếu không conflict

---

# ENUM Design

Hệ thống sử dụng enum ở backend để:

* type-safe
* clean hơn
* tránh typo
* dễ maintain

---

# 1. users

## Purpose

Lưu thông tin tài khoản người dùng.

---

## Fields

```sql id="wjlwmx"
id UUID PRIMARY KEY

username VARCHAR(50) NOT NULL
email VARCHAR(255) NOT NULL

password_hash VARCHAR(255) NOT NULL

is_active BOOLEAN DEFAULT true

last_login_at TIMESTAMP NULL

created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL
deleted_at TIMESTAMP NULL
```

---

## Constraints

```txt id="o5j9tt"
username unique khi deleted_at IS NULL
email unique khi deleted_at IS NULL
```

---

## Notes

* password bắt buộc hash bằng BCrypt
* không lưu plain password
* authentication chính của hệ thống

---

# 2. roles

## Purpose

Lưu role hệ thống.

---

## Fields

```sql id="v6n2t8"
id UUID PRIMARY KEY

name VARCHAR(50) NOT NULL
description VARCHAR(255)

created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL
```

---

## Default Roles

```txt id="t1ix4h"
USER
ADMIN
```

---

# 3. user_roles

## Purpose

Mapping user và role.

---

## Fields

```sql id="87x7x9"
user_id UUID NOT NULL
role_id UUID NOT NULL

created_at TIMESTAMP NOT NULL

PRIMARY KEY(user_id, role_id)
```

---

## Relationships

```txt id="fr8j2z"
users 1-N user_roles
roles 1-N user_roles
```

---

# 4. wallets

## Purpose

Lưu các ví tài chính của người dùng.

---

## Fields

```sql id="q2m1jp"
id UUID PRIMARY KEY

user_id UUID NOT NULL

name VARCHAR(100) NOT NULL

type VARCHAR(50) NOT NULL

balance NUMERIC(15,2) DEFAULT 0

description TEXT NULL

created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL
deleted_at TIMESTAMP NULL
```

---

## WalletType Enum

```java id="0hs5iq"
public enum WalletType {
    CASH,
    BANK,
    EWALLET,
    SAVINGS
}
```

---

## Notes

Ví dụ:

* tiền mặt
* tài khoản ngân hàng
* ví điện tử
* ví tiết kiệm

Mỗi user có thể có nhiều wallet.

---

# 5. categories

## Purpose

Danh mục thu nhập / chi tiêu.

---

## Fields

```sql id="42g7yx"
id UUID PRIMARY KEY

user_id UUID NOT NULL

name VARCHAR(100) NOT NULL

type VARCHAR(20) NOT NULL

icon VARCHAR(100) NULL

color VARCHAR(20) NULL

is_default BOOLEAN DEFAULT false

created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL
deleted_at TIMESTAMP NULL
```

---

## CategoryType Enum

```java id="0wjlwm"
public enum CategoryType {
    INCOME,
    EXPENSE
}
```

---

## Notes

Ví dụ:

* Food
* Salary
* Transport
* Shopping

User có thể tự tạo category riêng.

---

# 6. transactions

## Purpose

Core table lưu toàn bộ giao dịch tài chính.

---

## Fields

```sql id="4q3pj1"
id UUID PRIMARY KEY

user_id UUID NOT NULL

wallet_id UUID NOT NULL

category_id UUID NULL

type VARCHAR(20) NOT NULL

amount NUMERIC(15,2) NOT NULL

title VARCHAR(255) NOT NULL

note TEXT NULL

transaction_date TIMESTAMP NOT NULL

reference_transaction_id UUID NULL

created_at TIMESTAMP NOT NULL
updated_at TIMESTAMP NOT NULL
deleted_at TIMESTAMP NULL
```

---

## TransactionType Enum

```java id="g8k5jh"
public enum TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}
```

---

## Notes

### INCOME

Tiền vào wallet.

### EXPENSE

Tiền ra khỏi wallet.

### TRANSFER

Chuyển tiền giữa 2 wallet.

Transfer sẽ tạo:

```txt id="g34rmw"
1 transaction trừ tiền
1 transaction cộng tiền
```

và liên kết bằng:

```txt id="vg3e0j"
reference_transaction_id
```

---

# 7. refresh_tokens

## Purpose

Lưu refresh token cho JWT authentication.

---

## Fields

```sql id="0gh2uy"
id UUID PRIMARY KEY

user_id UUID NOT NULL

token VARCHAR(500) NOT NULL

expired_at TIMESTAMP NOT NULL

revoked BOOLEAN DEFAULT false

created_at TIMESTAMP NOT NULL
```

---

## Notes

Dùng để:

* refresh access token
* logout
* revoke session
* tăng security

Không lưu access token trong database.

---

# Relationship Overview

```txt id="hmbw7o"
User
 ├── Wallets
 ├── Categories
 ├── Transactions
 ├── Refresh Tokens
 └── Roles

Wallet
 └── Transactions

Category
 └── Transactions
```

---

# Current MVP Scope

Hiện tại system đã hỗ trợ:

* authentication
* authorization
* JWT refresh flow
* role management
* wallet management
* income/expense tracking
* transfer money
* dashboard analytics
* transaction history
* soft delete
* audit tracking

---

# Future Expansion

Có thể mở rộng thêm:

* budgets
* recurring_transactions
* notifications
* attachments
* analytics_cache
* shared_wallets
* exchange_rates
* multi_currency

mà không cần redesign lại core architecture.

---

# Final Notes

Đây là structure phù hợp để:

* học Spring Boot thực chiến
* học PostgreSQL
* học JWT Security
* học REST API architecture
* học frontend dashboard integration
* học production thinking

Trong khi vẫn giữ:

* scope vừa phải
* relation clean
* business logic rõ ràng
* dễ maintain
* dễ scale
