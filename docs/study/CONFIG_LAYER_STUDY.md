# Config Layer Study

## 1) Config layer là gì?

`config` chứa các bean và cấu hình hạ tầng cho app: security, password encoder, CORS, serialization, v.v.

## 2) Vai trò trong backend

- Định nghĩa bean dùng chung (`PasswordEncoder`, ...).
- Cấu hình security chain, endpoint policy.
- Quản lý tham số từ `application.yml/properties`.
- Tách phần setup framework khỏi business code.

## 3) Vị trí trong flow

Config chạy lúc app startup để dựng context; sau đó request runtime dùng các bean đã tạo.

## 4) Happy vs unhappy case

Happy:
- bean tạo thành công
- dependency injection đầy đủ
- request chạy ổn định

Unhappy:
- thiếu bean hoặc circular dependency -> app không start
- cấu hình sai property -> startup fail hoặc runtime lỗi
- security/cors config sai -> request bị chặn ngoài ý muốn

## 5) Dấu hiệu config layer hoàn chỉnh

- Mỗi config class có phạm vi rõ ràng.
- Secret/config runtime lấy từ env/property, không hardcode.
- Bean lifecycle ổn định, app start sạch lỗi.
- Cấu hình phản ánh đúng contract giữa frontend và backend.
