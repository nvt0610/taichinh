# Frontend UX/UI Direction

Tài liệu này ghi lại gu UI đã chốt trong quá trình redesign trang đăng nhập, để các màn hình tiếp theo giữ cùng một tinh thần.

## Tinh thần chung

- Giao diện nên cảm giác sạch, có chiều sâu vừa đủ, và thực dụng cho một app tài chính cá nhân.
- Ưu tiên cảm giác workspace thật hơn là landing page quảng cáo.
- Dùng dữ liệu có ngữ cảnh để làm UI sống hơn, ví dụ tỷ giá, giá vàng tham chiếu, số dư, dòng tiền, giao dịch gần đây.
- Tránh trang trí quá nhiều lớp nếu nó không giúp người dùng hiểu nhanh hơn.
- Nếu một element làm nền hoặc watermark khiến UI khó kiểm soát, bỏ nó để giữ giao diện clean.

## Layout Và Composition

- Auth layout hiện tại dùng split composition: hero dữ liệu bên trái, form đăng nhập bên phải.
- Form nên có chiều cao và chiều sâu rõ, không đặt giữa màn hình quá trống.
- Hero không nên chỉ có chữ. Nên có một vài element dữ liệu hoặc visual tài chính để tạo bối cảnh.
- Với các trang app sau đăng nhập, ưu tiên layout phục vụ quét thông tin nhanh: sidebar, topbar, grid dữ liệu, bảng, filter, summary metric.
- Không dùng hero marketing lớn cho dashboard, ví, giao dịch. Các trang đó nên giống công cụ làm việc.

## Visual Style

- Tone hiện tại: minimal finance, mint/graphite, có glass nhẹ và shadow mềm.
- Dùng border mảnh, nền sáng, layer nhẹ. Không làm quá nhiều hiệu ứng mờ chồng nhau.
- Card ngân hàng trong login chỉ là visual phụ, không cần quá thật. Nó nên mờ nhẹ và lùi về sau.
- Chip dữ liệu nhỏ như giá vàng hoặc trạng thái live nên gọn, sắc, không chiếm spotlight.
- Tránh watermark nền nếu nó làm rối code hoặc khó thấy trên nền sáng.

## Data Elements

- Các element dữ liệu nên có nhãn rõ, giá trị dễ scan, ngày cập nhật cùng format ISO `YYYY-MM-DD`.
- Tỷ giá hiện dùng format `USD / VND`, `USD / EUR`, v.v.
- Giá vàng hiện là tham chiếu tĩnh do nguồn live ở frontend dễ gặp CORS. Nếu cần live thật, nên proxy qua backend.
- Không dùng các câu mô tả kiểu quảng cáo quá mức. Copy nên đời hơn, ví dụ “nhìn nhanh dòng tiền, tỷ giá và các con số mình cần”.

## Icon Và Branding

- Dùng logo thật trong `frontend/public` cho brand mark và favicon.
- Dùng `lucide-react` qua wrapper `AppIcon` để icon thống nhất, dễ thay thế.
- Icon nên hỗ trợ scan và action, không dùng như trang trí rải rác.

## Khi Làm Trang Tiếp Theo

- Bắt đầu từ nhu cầu người dùng trên trang đó: cần xem gì, lọc gì, thao tác gì nhanh nhất.
- Ưu tiên dữ liệu thật hoặc dữ liệu domain-related hơn hình trang trí.
- Giữ UI compact vừa đủ, đừng biến dashboard thành landing page.
- Nếu thêm visual flourish, kiểm tra nó có giúp cân bố cục hoặc hiểu dữ liệu không. Nếu không, bỏ.
- Sau mỗi thay đổi UI, chạy build và nếu có thể kiểm tra responsive ở desktop/mobile.
