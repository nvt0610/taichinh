# Dashboard Study

## Dashboard backend trong project này là gì?

Phase 4 của backend trong project này là một nhóm API chỉ đọc dữ liệu, dùng để frontend render dashboard MVP từ dữ liệu thật của user đang đăng nhập.

Hiện tại dashboard có 4 phần:

- `totalBalance`
- `totalIncome`
- `totalExpense`
- `netCashFlow`
- `periodStart`
- `periodEnd`
- `recent transactions`
- `top spending categories`
- `monthly statistics`

Trong project này, API hiện tại là:

- `GET /api/dashboard/summary`
- `GET /api/dashboard/recent-transactions`
- `GET /api/dashboard/top-spending-categories`
- `GET /api/dashboard/monthly-statistics`

Code chính nằm ở:

- `controller/DashboardController`
- `service/DashboardService`
- `dto/dashboard/*`

## Các file đang làm gì?

### `DashboardSummaryQueryParams`

File này nhận query param từ request:

- `startDate`
- `endDate`

Nó dùng `@ModelAttribute`, nên request dạng này sẽ được bind tự động:

```text
/api/dashboard/summary?startDate=2026-05-01T00:00:00&endDate=2026-05-31T23:59:59
```

Nếu không truyền gì, service sẽ tự dùng tháng hiện tại làm kỳ mặc định.

File này hiện được dùng lại cho:

- `summary`
- `top-spending-categories`

### `DashboardSummaryResponse`

File này là DTO đầu ra cho dashboard summary. Controller trả nó qua `ApiResponse.success(...)`.

### `DashboardRecentTransactionsQueryParams`

File này nhận `limit` cho block recent transactions.

Mặc định:

- `limit = 5`

Giới hạn:

- tối thiểu `1`
- tối đa `20`

### `DashboardRecentTransactionResponse`

DTO này là item cho recent transactions. Nó trả dữ liệu vừa đủ để frontend render nhanh:

- transaction id
- wallet id + wallet name
- category id + category name
- type
- amount
- title
- transaction date

### `DashboardTopSpendingCategoryResponse`

DTO này dùng cho block top spending categories. Mỗi item gồm:

- category id
- category name
- icon
- color
- tổng số tiền đã chi trong kỳ
- số giao dịch thuộc category đó

### `DashboardMonthlyStatisticsQueryParams`

File này nhận `months` cho monthly statistics.

Mặc định:

- `months = 6`

Giới hạn:

- tối thiểu `1`
- tối đa `12`

### `DashboardMonthlyStatisticResponse`

DTO này trả thống kê theo từng tháng:

- `month`
- `totalIncome`
- `totalExpense`
- `netCashFlow`

### `DashboardController`

Controller nhận JWT authentication, lấy `userId` hiện tại qua `AuthenticatedUserProvider`, rồi gọi `DashboardService`.

Nghĩa là tất cả dashboard endpoint luôn chỉ đọc dữ liệu của user đang đăng nhập, không tin `userId` từ client.

### `DashboardService`

Đây là nơi chứa toàn bộ logic tổng hợp cho dashboard:

1. Xác định khoảng thời gian.
2. Đọc toàn bộ wallet của user để tính `totalBalance`.
3. Đọc transaction trong kỳ để tính `totalIncome`, `totalExpense`, `netCashFlow`.
4. Lấy recent transactions theo `transactionDate desc`.
5. Group expense transaction theo `categoryId` để tìm top spending categories.
6. Group transaction theo `YearMonth` để tạo monthly statistics.

## Luồng request đến database

Luồng thực tế chung là:

1. Client gọi một trong các endpoint `/api/dashboard/...`
2. `JwtAuthenticationFilter` xác thực access token
3. `DashboardController` lấy `userId`
4. `DashboardService` resolve query cần thiết
5. `WalletRepository` và/hoặc `TransactionRepository` đọc dữ liệu của user
6. `CategoryRepository` được dùng khi cần enrich tên/icon/màu category
7. Service tổng hợp số liệu
8. Controller trả `ApiResponse<...>`

## Quy tắc quan trọng của dashboard này

### 1. `totalBalance` là số dư hiện tại, không bị filter theo kỳ

Đây là điểm dễ nhầm nhất.

`totalBalance` được tính từ `wallet.balance` hiện tại của user. Vì vậy nó phản ánh số tiền đang có ở thời điểm gọi API, không phải số dư riêng trong khoảng `startDate/endDate`.

Ví dụ:

- ví đang có `1479`
- trong tháng 5 chỉ có `income = 500` và `expense = 120`

thì:

- `totalBalance = 1479`
- `totalIncome = 500`
- `totalExpense = 120`
- `netCashFlow = 380`

### 2. `income/expense` trong summary là số liệu theo kỳ

`totalIncome`, `totalExpense`, và `netCashFlow` chỉ tính trên transaction nằm trong `periodStart` đến `periodEnd`.

### 3. Transfer chưa được cộng vào income/expense summary và monthly statistics

Trong service hiện tại:

- `INCOME` được cộng vào `totalIncome`
- `EXPENSE` được cộng vào `totalExpense`
- `TRANSFER` bị bỏ qua

Điều này đúng cho dashboard MVP, vì transfer chỉ là di chuyển tiền giữa các ví của cùng user, không phải thu hay chi thực sự.

### 4. Recent transactions đang đọc theo `transactionDate` giảm dần

Block recent transactions hiện tại trả transaction mới nhất trước.

### 5. Top spending categories chỉ tính `EXPENSE`

Block này:

- chỉ lấy transaction loại `EXPENSE`
- chỉ lấy transaction có `categoryId`
- group theo category
- sort giảm dần theo tổng tiền đã chi
- giới hạn top `5`

### 6. Monthly statistics trả cả tháng không có dữ liệu

Nếu request `months=6`, response luôn cố gắng trả đủ 6 tháng liên tiếp tính từ hiện tại trở lùi về trước, kể cả tháng nào chưa có giao dịch thì vẫn có item với số liệu `0`.

## Cách resolve khoảng thời gian

### Nếu không truyền query param

Service tự lấy:

- `periodStart`: ngày đầu tháng hiện tại lúc `00:00:00`
- `periodEnd`: ngày cuối tháng hiện tại lúc `23:59:59.999999999`

### Nếu có truyền query param

Phải truyền đủ cả `startDate` và `endDate`.

Nếu chỉ truyền một bên, service sẽ throw `BusinessException` với `BAD_REQUEST`.

Nếu `startDate > endDate`, service cũng throw `BAD_REQUEST`.

## Cách hoạt động của 3 block mới

### Recent transactions

Endpoint:

```text
GET /api/dashboard/recent-transactions?limit=5
```

Mục tiêu:

- cho dashboard hiển thị hoạt động gần đây
- không cần pagination đầy đủ ở block nhỏ này

### Top spending categories

Endpoint:

```text
GET /api/dashboard/top-spending-categories?startDate=...&endDate=...
```

Nếu không truyền date range, nó dùng tháng hiện tại giống summary.

### Monthly statistics

Endpoint:

```text
GET /api/dashboard/monthly-statistics?months=6
```

Block này phù hợp để frontend render chart cột hoặc chart đường đơn giản cho MVP.

## Dashboard Phase 4 hoàn thành khi nào?

Có thể xem Phase 4 backend là hoàn thành khi:

- API có JWT protection
- chỉ đọc dữ liệu của user hiện tại
- có `totalBalance`
- có `income/expense/netCashFlow`
- có default current month
- có hỗ trợ custom date range
- có recent transactions
- có top spending categories
- có monthly statistics
- validation date range trả lỗi đúng format chung
- có integration test đi qua controller, security, service, và PostgreSQL thật

## Giới hạn hiện tại

Dashboard Phase 4 hiện tại vẫn là MVP, nên còn một vài giới hạn có chủ ý:

- top spending categories đang cố định `top 5`
- monthly statistics đang dùng cửa sổ tháng gần đây, chưa có custom date range riêng
- service đang tổng hợp bằng cách đọc dữ liệu user rồi group trong application layer, phù hợp MVP nhưng chưa tối ưu cho dữ liệu rất lớn
