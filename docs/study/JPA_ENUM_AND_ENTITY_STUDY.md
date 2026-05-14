# JPA Enum And Entity Study

Tài liệu này giải thích 2 loại file JPA vừa thêm vào backend:

- `enum`: định nghĩa tập giá trị cố định trong domain.
- `entity`: class Java đại diện cho một table trong database.

Các ví dụ trong tài liệu này dựa trên code hiện tại:

- `backend/src/main/java/com/taichinh/app/enums/WalletType.java`
- `backend/src/main/java/com/taichinh/app/enums/CategoryType.java`
- `backend/src/main/java/com/taichinh/app/enums/TransactionType.java`
- `backend/src/main/java/com/taichinh/app/entity/Wallet.java`

---

## 1. JPA Enum Là Gì?

Trong Java, `enum` là một kiểu dữ liệu chỉ cho phép một số giá trị cố định.

Ví dụ:

```java
public enum WalletType {
    CASH,
    BANK,
    EWALLET,
    SAVINGS
}
```

`WalletType` chỉ có thể nhận một trong các giá trị:

- `CASH`
- `BANK`
- `EWALLET`
- `SAVINGS`

Điều này giúp code an toàn hơn so với dùng `String` tự do.

Nếu dùng `String`, code có thể bị typo:

```java
wallet.setType("E-WALLET");
wallet.setType("ewallet");
wallet.setType("Ewallet");
```

Các giá trị trên đều là text, Java không biết cái nào đúng cái nào sai.

Nếu dùng `enum`, Java bắt buộc phải dùng đúng giá trị đã khai báo:

```java
wallet.setType(WalletType.EWALLET);
```

Nếu viết sai, code sẽ lỗi ngay lúc compile.

---

## 2. Các Enum Hiện Có Trong Project

### WalletType

File:

```text
backend/src/main/java/com/taichinh/app/enums/WalletType.java
```

Code:

```java
public enum WalletType {
    CASH,
    BANK,
    EWALLET,
    SAVINGS
}
```

Ý nghĩa:

- `CASH`: ví tiền mặt.
- `BANK`: tài khoản ngân hàng.
- `EWALLET`: ví điện tử.
- `SAVINGS`: ví tiết kiệm.

Trong database, table `wallets` có cột:

```sql
type VARCHAR(50) NOT NULL
```

Cột này sẽ lưu text như:

```text
CASH
BANK
EWALLET
SAVINGS
```

### CategoryType

File:

```text
backend/src/main/java/com/taichinh/app/enums/CategoryType.java
```

Code:

```java
public enum CategoryType {
    INCOME,
    EXPENSE
}
```

Ý nghĩa:

- `INCOME`: danh mục thu nhập.
- `EXPENSE`: danh mục chi tiêu.

Ví dụ:

- Salary thuộc `INCOME`.
- Food thuộc `EXPENSE`.

### TransactionType

File:

```text
backend/src/main/java/com/taichinh/app/enums/TransactionType.java
```

Code:

```java
public enum TransactionType {
    INCOME,
    EXPENSE,
    TRANSFER
}
```

Ý nghĩa:

- `INCOME`: giao dịch tiền vào.
- `EXPENSE`: giao dịch tiền ra.
- `TRANSFER`: chuyển tiền giữa các ví.

---

## 3. Enum Được Lưu Xuống Database Như Thế Nào?

Trong entity, enum được map bằng:

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 50)
private WalletType type;
```

Dòng quan trọng nhất là:

```java
@Enumerated(EnumType.STRING)
```

Nó nói với JPA/Hibernate:

> Khi lưu enum xuống database, hãy lưu bằng tên enum dạng text.

Ví dụ Java object:

```java
WalletType.BANK
```

Khi lưu xuống DB sẽ thành:

```text
BANK
```

Nên dùng `EnumType.STRING` thay vì `EnumType.ORDINAL`.

Nếu dùng `ORDINAL`, database sẽ lưu số thứ tự:

```text
CASH    -> 0
BANK    -> 1
EWALLET -> 2
SAVINGS -> 3
```

Vấn đề là nếu sau này đổi thứ tự enum, dữ liệu cũ có thể bị hiểu sai. Vì vậy project này dùng `STRING` để rõ ràng và an toàn hơn.

---

## 4. JPA Entity Là Gì?

`entity` là một class Java được JPA dùng để đại diện cho một table trong database.

Trong project này, file:

```text
backend/src/main/java/com/taichinh/app/entity/Wallet.java
```

đại diện cho table:

```sql
wallets
```

Nói đơn giản:

```text
1 row trong table wallets <-> 1 object Wallet trong Java
```

Ví dụ row trong database:

```text
id       = 6fa...
user_id  = 2bd...
name     = Main Bank
type     = BANK
balance  = 1000000.00
```

Khi JPA đọc lên, nó biến row đó thành object:

```java
Wallet wallet = ...
wallet.getName();    // "Main Bank"
wallet.getType();    // WalletType.BANK
wallet.getBalance(); // 1000000.00
```

---

## 5. Wallet Entity Đang Làm Gì?

Code hiện tại:

```java
@Entity
@Table(name = "wallets")
public class Wallet {
    ...
}
```

### `@Entity`

Annotation này nói với JPA:

> Class này là một entity, hãy quản lý nó như một object có thể lưu xuống database.

Nếu không có `@Entity`, Spring Data JPA sẽ xem `Wallet` chỉ là class Java bình thường.

### `@Table(name = "wallets")`

Annotation này nói:

> Entity `Wallet` map với table `wallets`.

Tên class là `Wallet`, còn tên table trong DB là `wallets`, nên ta khai báo rõ bằng `@Table`.

---

## 6. Mapping Field Java Sang Column Database

Trong table `wallets`, schema hiện tại là:

```sql
CREATE TABLE IF NOT EXISTS wallets (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  user_id UUID NOT NULL,
  name VARCHAR(100) NOT NULL,
  type VARCHAR(50) NOT NULL,
  balance NUMERIC(15,2) NOT NULL DEFAULT 0,
  description TEXT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
  deleted_at TIMESTAMP NULL
);
```

Entity `Wallet` map tương ứng:

```java
@Id
@GeneratedValue(strategy = GenerationType.UUID)
private UUID id;
```

Ý nghĩa:

- `@Id`: field này là primary key.
- `@GeneratedValue(strategy = GenerationType.UUID)`: JPA tự tạo UUID khi persist object mới.
- Kiểu Java là `UUID`, map với kiểu PostgreSQL `UUID`.

```java
@Column(name = "user_id", nullable = false)
private UUID userId;
```

Ý nghĩa:

- Java field tên `userId`.
- DB column tên `user_id`.
- `nullable = false` tương ứng với `NOT NULL`.

Hiện tại field này chỉ là `UUID`, chưa map quan hệ `User`.

Lý do: project mới tạo 1 entity đầu tiên là `Wallet`, chưa tạo `User` entity. Sau này khi có `User`, ta có thể đổi sang quan hệ JPA:

```java
@ManyToOne
@JoinColumn(name = "user_id")
private User user;
```

Nhưng ở bước hiện tại, dùng `UUID userId` giúp entity compile được, map đúng DB, và chưa kéo thêm entity khác.

```java
@Column(nullable = false, length = 100)
private String name;
```

Map với:

```sql
name VARCHAR(100) NOT NULL
```

```java
@Enumerated(EnumType.STRING)
@Column(nullable = false, length = 50)
private WalletType type;
```

Map với:

```sql
type VARCHAR(50) NOT NULL
```

Java dùng enum `WalletType`, DB lưu text.

```java
@Column(nullable = false, precision = 15, scale = 2)
private BigDecimal balance = BigDecimal.ZERO;
```

Map với:

```sql
balance NUMERIC(15,2) NOT NULL DEFAULT 0
```

Ý nghĩa:

- `BigDecimal`: kiểu nên dùng cho tiền trong Java.
- Không dùng `double` hoặc `float` cho tiền vì có thể sai số.
- `precision = 15`: tổng số chữ số tối đa.
- `scale = 2`: số chữ số sau dấu thập phân.

```java
@Column(columnDefinition = "TEXT")
private String description;
```

Map với:

```sql
description TEXT NULL
```

Field này có thể null.

---

## 7. Audit Fields Và Soft Delete

Entity có 3 field thời gian:

```java
@Column(name = "created_at", nullable = false, updatable = false)
private LocalDateTime createdAt;

@Column(name = "updated_at", nullable = false)
private LocalDateTime updatedAt;

@Column(name = "deleted_at")
private LocalDateTime deletedAt;
```

### createdAt

Thời điểm record được tạo.

```java
updatable = false
```

nghĩa là sau khi tạo xong, JPA không nên update field này nữa.

### updatedAt

Thời điểm record được update gần nhất.

Project hiện có cả:

- Java callback `@PreUpdate`.
- DB trigger `trg_wallets_set_updated_at`.

Callback giúp object Java có giá trị đúng trước khi lưu. DB trigger là lớp bảo vệ ở database, kể cả khi update không đi qua JPA.

### deletedAt

Field phục vụ soft delete.

Nếu:

```text
deleted_at IS NULL
```

record còn active.

Nếu:

```text
deleted_at IS NOT NULL
```

record đã bị xóa mềm.

Xóa mềm nghĩa là không xóa row khỏi database, chỉ đánh dấu thời điểm xóa. Cách này giữ được lịch sử dữ liệu và tránh mất thông tin tài chính.

---

## 8. `@PrePersist` Và `@PreUpdate`

Trong entity hiện có:

```java
@PrePersist
void prePersist() {
    LocalDateTime now = LocalDateTime.now();
    createdAt = now;
    updatedAt = now;
    if (balance == null) {
        balance = BigDecimal.ZERO;
    }
}
```

`@PrePersist` chạy ngay trước khi JPA insert object mới vào database.

Nó đang làm 3 việc:

- Set `createdAt`.
- Set `updatedAt`.
- Nếu `balance` bị null thì đưa về `BigDecimal.ZERO`.

Ví dụ khi tạo ví mới:

```java
Wallet wallet = new Wallet(userId, "Cash", WalletType.CASH);
walletRepository.save(wallet);
```

Trước khi insert, JPA gọi `prePersist()`. Sau đó object có đủ `createdAt`, `updatedAt`, và `balance`.

Tiếp theo:

```java
@PreUpdate
void preUpdate() {
    updatedAt = LocalDateTime.now();
}
```

`@PreUpdate` chạy ngay trước khi JPA update một entity đã tồn tại.

Ví dụ:

```java
wallet.setName("Main Cash");
walletRepository.save(wallet);
```

Trước khi update, JPA gọi `preUpdate()` để cập nhật `updatedAt`.

---

## 9. Constructor Trong Entity

Entity hiện có:

```java
protected Wallet() {
}
```

JPA cần một constructor không tham số để tạo object khi đọc data từ database.

Constructor này để `protected` vì:

- JPA vẫn dùng được.
- Code bên ngoài không nên tạo `Wallet` rỗng tùy tiện.

Entity cũng có constructor tiện dụng:

```java
public Wallet(UUID userId, String name, WalletType type) {
    this.userId = userId;
    this.name = name;
    this.type = type;
}
```

Dùng khi app tạo ví mới:

```java
Wallet wallet = new Wallet(userId, "Cash", WalletType.CASH);
```

---

## 10. Vai Trò Của Enum Và Entity Trong Luồng Spring Boot

Sau khi dự án hoàn thiện, luồng hoạt động thường sẽ như này:

```text
Frontend
  -> Controller
  -> Service
  -> Repository
  -> JPA Entity
  -> Hibernate
  -> PostgreSQL
```

Ví dụ chức năng tạo wallet:

```text
1. User nhập form tạo ví ở frontend.
2. Frontend gọi API POST /api/wallets.
3. Controller nhận request.
4. Service kiểm tra business rules.
5. Service tạo object Wallet.
6. Repository gọi save(wallet).
7. Hibernate đọc annotation trong Wallet entity.
8. Hibernate generate SQL INSERT vào table wallets.
9. PostgreSQL lưu row mới.
10. Backend trả response về frontend.
```

Trong luồng đó:

- `WalletType` giúp loại ví luôn đúng giá trị hợp lệ.
- `Wallet` entity giúp Java object map đúng table `wallets`.
- JPA/Hibernate dùng annotation để biết insert/update/select như thế nào.

---

## 11. Entity Không Phải DTO

Một điểm quan trọng khi học Spring Boot:

```text
Entity không phải DTO.
```

Entity đại diện cho dữ liệu trong database.

DTO đại diện cho dữ liệu đi vào/đi ra API.

Ví dụ sau này ta có thể có:

```java
public class CreateWalletRequest {
    private String name;
    private WalletType type;
    private String description;
}
```

Request từ frontend không nên gửi trực tiếp vào entity. Controller nhận DTO, service chuyển DTO thành entity.

Luồng tốt hơn:

```text
CreateWalletRequest DTO
  -> Service validate
  -> new Wallet(...)
  -> WalletRepository.save(wallet)
```

Lý do:

- Tránh frontend set nhầm field nhạy cảm như `id`, `userId`, `balance`, `createdAt`.
- API contract rõ ràng hơn.
- Entity giữ đúng vai trò persistence model.

---

## 12. Sau Này Repository Sẽ Dùng Entity Như Thế Nào?

Bước tiếp theo trong checklist là Spring Data repositories.

Khi có repository, code có thể như sau:

```java
public interface WalletRepository extends JpaRepository<Wallet, UUID> {
    List<Wallet> findByUserIdAndDeletedAtIsNull(UUID userId);
}
```

Ý nghĩa:

- `JpaRepository<Wallet, UUID>` nói rằng repository này quản lý `Wallet`.
- `UUID` là kiểu của primary key.
- `findByUserIdAndDeletedAtIsNull` là query method Spring Data tự hiểu.

Khi gọi:

```java
walletRepository.findByUserIdAndDeletedAtIsNull(userId);
```

Spring Data JPA có thể tự generate SQL tương đương:

```sql
SELECT *
FROM wallets
WHERE user_id = ?
  AND deleted_at IS NULL;
```

Đây là lý do entity cần map field rõ ràng. Repository dựa vào entity để biết table, column, kiểu dữ liệu và cách query.

---

## 13. Sau Khi Project Hoàn Thành, Vai Trò Của Chúng Là Gì?

### Enum

Enum là bộ từ vựng chính thức của domain.

Nó trả lời câu hỏi:

```text
Trong hệ thống này, field này được phép có những trạng thái/loại nào?
```

Ví dụ:

- Wallet chỉ có các loại trong `WalletType`.
- Category chỉ có `INCOME` hoặc `EXPENSE`.
- Transaction chỉ có `INCOME`, `EXPENSE`, hoặc `TRANSFER`.

Enum giúp:

- Code dễ đọc.
- Tránh typo.
- Dễ validate.
- Dễ switch logic theo từng loại.
- API và DB nhất quán hơn.

### Entity

Entity là model persistence của backend.

Nó trả lời câu hỏi:

```text
Một table trong database được biểu diễn như thế nào trong Java?
```

Entity giúp:

- Service làm việc với object Java thay vì tự viết SQL mọi nơi.
- Repository biết cách đọc/ghi dữ liệu.
- Hibernate biết cách map object sang table.
- App giữ dữ liệu đúng shape với schema.

Trong project tài chính cá nhân này, entity sẽ là nền tảng cho:

- Authentication: `User`, `Role`, `RefreshToken`.
- Wallet CRUD: `Wallet`.
- Category CRUD: `Category`.
- Income/expense/transfer: `Transaction`.
- Dashboard: query các entity để tính tổng tiền, thống kê, lịch sử.

---

## 14. Ghi Nhớ Nhanh

```text
Enum = danh sách giá trị hợp lệ.
Entity = Java class map với database table.
Repository = class/interface dùng entity để query database.
Service = nơi xử lý business logic.
Controller = nơi nhận/trả HTTP request/response.
```

Với `Wallet`:

```text
WalletType.CASH
  -> giá trị enum trong Java
  -> lưu thành "CASH" ở cột wallets.type

Wallet entity
  -> object Java
  -> map với row trong table wallets
```

Nếu đọc lại sau này, chỉ cần nhớ một câu:

> Enum giúp domain có giá trị đúng; entity giúp JPA biến object Java thành dữ liệu trong database và ngược lại.
