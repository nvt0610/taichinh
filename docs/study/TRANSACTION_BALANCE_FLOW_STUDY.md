# Transaction Và Balance Flow Study

File này giải thích phần Phase 3 xử lý `income`, `expense`, và `transfer`.

## 1) Mục tiêu của slice này

Cho user đã đăng nhập:

- tạo giao dịch thu nhập
- tạo giao dịch chi tiêu
- tạo giao dịch chuyển tiền giữa 2 ví
- cập nhật số dư wallet đúng theo từng loại giao dịch

Điểm cốt lõi của slice này không phải chỉ là lưu row vào bảng `transactions`, mà là:

- kiểm tra ownership theo `userId`
- validate category đúng loại
- không cho chi hoặc chuyển vượt số dư
- transfer phải tạo 2 transaction liên kết nhau

---

## 2) Các file chính

- `controller/TransactionController.java`
- `service/TransactionService.java`
- `dto/transaction/CreateIncomeTransactionRequest.java`
- `dto/transaction/CreateExpenseTransactionRequest.java`
- `dto/transaction/CreateTransferTransactionRequest.java`
- `dto/transaction/TransactionResponse.java`
- `dto/transaction/TransferTransactionResponse.java`

File liên quan:

- `entity/Transaction.java`
- `repository/TransactionRepository.java`
- `repository/WalletRepository.java`
- `repository/CategoryRepository.java`
- `exception/ErrorCode.java`
- `security/AuthenticatedUserProvider.java`

---

## 3) Endpoint đang có

- `POST /api/transactions/income`
- `POST /api/transactions/expense`
- `POST /api/transactions/transfer`
- `GET /api/transactions`
- `GET /api/transactions/{transactionId}`
- `PUT /api/transactions/{transactionId}/income`
- `PUT /api/transactions/{transactionId}/expense`
- `PUT /api/transactions/{transactionId}/transfer`
- `DELETE /api/transactions/{transactionId}`

Slice này hiện đã bao phủ create, history, update, và soft delete.

---

## 4) Luồng income

1. Controller lấy `userId` từ JWT qua `AuthenticatedUserProvider`.
2. Service tìm wallet active theo `walletId + userId`.
3. Nếu có `categoryId`, service tìm category active theo `categoryId + userId`.
4. Category phải có `type = INCOME`.
5. Tăng `wallet.balance` thêm `amount`.
6. Tạo `Transaction` với:
   - `type = INCOME`
   - `walletId`
   - `categoryId`
   - `amount`
   - `title`
   - `note`
   - `transactionDate`
7. Lưu transaction và trả `TransactionResponse`.

---

## 5) Luồng expense

Flow gần giống income, nhưng có thêm rule số dư:

1. Tìm wallet active theo owner.
2. Nếu có `categoryId`, category phải là `EXPENSE`.
3. Kiểm tra:

```txt
wallet.balance >= amount
```

4. Nếu không đủ tiền, throw `BusinessException(ErrorCode.INSUFFICIENT_BALANCE, ...)`.
5. Trừ `wallet.balance`.
6. Tạo transaction `type = EXPENSE`.

---

## 6) Luồng transfer

Transfer là case đặc biệt nhất.

### Input chính

- `sourceWalletId`
- `destinationWalletId`
- `amount`
- `title`
- `note`
- `transactionDate`

### Rule

- source và destination không được trùng nhau
- cả 2 wallet đều phải thuộc cùng user hiện tại
- source wallet phải đủ số dư

### Cách lưu

Service tạo:

- 1 transaction cho ví nguồn
- 1 transaction cho ví đích

Cả hai đều có:

- `type = TRANSFER`
- cùng `amount`
- cùng `title`
- cùng `transactionDate`

Sau khi có ID của 2 row:

- transaction nguồn trỏ `reference_transaction_id` sang transaction đích
- transaction đích trỏ ngược lại transaction nguồn

Như vậy transfer trong DB là một cặp linked rows, không phải một row duy nhất.

---

## 7) Vì sao balance được update trong service

`WalletController` không trực tiếp sửa số dư.

`TransactionService` mới là nơi:

- validate rule domain
- update wallet balance
- lưu transaction

Mọi bước được đặt trong `@Transactional`, nên nếu có lỗi giữa chừng:

- balance không bị lệch nửa chừng
- transaction không bị lưu dang dở

Đây là lý do phase trước mình không cho `Wallet update` sửa `balance` tự do.

---

## 8) Transaction history đang chạy như nào

`GET /api/transactions` đang hỗ trợ:

- `page`
- `size`
- `q`
- `sort`
- `walletId`
- `type`
- `startDate`
- `endDate`

History chỉ trả transaction còn active:

```txt
deleted_at IS NULL
```

Filter được áp ở service sau khi lấy list theo `userId`, rồi mới sort và paginate.

Sort whitelist hiện có:

- `transactionDate`
- `amount`
- `title`
- `type`
- `createdAt`
- `updatedAt`

---

## 9) Update transaction đang làm gì

### Income update

- rollback ảnh hưởng cũ của income
- validate rollback không làm ví âm
- apply giá trị income mới

### Expense update

- refund expense cũ về wallet cũ
- validate expense mới với số dư hiện tại
- apply expense mới

### Transfer update

- tìm cả cặp transaction liên kết
- rollback cặp transfer cũ
- validate ví nguồn mới đủ tiền
- apply cặp transfer mới

Điểm quan trọng:

- transfer source đang được lưu với `amount` âm
- transfer destination đang được lưu với `amount` dương

Nhờ vậy service nhận ra chiều của cặp transfer để rollback/update an toàn hơn.

---

## 10) Soft delete transaction đang làm gì

`DELETE /api/transactions/{id}` không hard delete row.

### Với income

- trừ lại số tiền khỏi wallet
- set `deleted_at`

### Với expense

- cộng lại số tiền vào wallet
- set `deleted_at`

### Với transfer

- rollback cả cặp transfer
- set `deleted_at` cho cả 2 row linked

Nếu rollback làm ví âm, service sẽ chặn bằng `INSUFFICIENT_BALANCE`.

---

## 11) Error code và validation liên quan

### Validation annotation

Ở request DTO:

- `amount > 0`
- `title` bắt buộc
- `walletId` bắt buộc
- `transactionDate` bắt buộc

### Business validation

Trong service:

- wallet không tồn tại -> `NOT_FOUND`
- category không tồn tại -> `NOT_FOUND`
- category sai loại -> `BAD_REQUEST`
- transfer cùng 1 ví -> `BAD_REQUEST`
- không đủ số dư -> `INSUFFICIENT_BALANCE`

---

## 12) Khi nào biết slice này hoàn thành

Slice này được xem là hoàn thành khi:

- income tạo được transaction và cộng tiền vào wallet
- expense tạo được transaction và trừ tiền khỏi wallet
- transfer tạo được 2 transaction linked bằng `reference_transaction_id`
- source và destination wallet update balance đúng
- transaction history chỉ trả record active theo user
- update transaction không làm lệch balance
- soft delete transaction rollback balance đúng
- không cho expense/transfer vượt số dư
- category chỉ được dùng đúng loại
- tất cả đều bị khóa theo `userId`
- integration test với PostgreSQL thật pass

---

## 13) Giới hạn hiện tại

Chưa làm trong lượt này:

- dashboard summary
- default category seed

Đây là các lát tiếp theo của Phase 3 và Phase 4.
