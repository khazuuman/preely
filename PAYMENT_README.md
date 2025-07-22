# Payment Registration System

## Tổng quan
Hệ thống đăng ký thanh toán trung gian được tích hợp vào ứng dụng Android Preely với giao diện đồng bộ với theme login hiện tại.

## Tính năng

### 1. Giao diện người dùng
- **Theme đồng bộ**: Sử dụng cùng font, màu sắc và style với trang login
- **Responsive design**: Tương thích với nhiều kích thước màn hình
- **Dynamic layout**: Hiển thị form phù hợp với phương thức thanh toán được chọn

### 2. Phương thức thanh toán
- **Credit/Debit Card**: Hỗ trợ thẻ tín dụng/ghi nợ
- **Bank Transfer**: Chuyển khoản ngân hàng
- **E-Wallet**: Ví điện tử (PayPal, Venmo, etc.)

### 3. Validation
- **Real-time validation**: Kiểm tra dữ liệu ngay khi nhập
- **Format tự động**: Tự động format số thẻ và ngày hết hạn
- **Comprehensive validation**: Kiểm tra đầy đủ các trường bắt buộc

### 4. Security
- **Input sanitization**: Làm sạch dữ liệu đầu vào
- **Pattern matching**: Kiểm tra định dạng dữ liệu
- **Terms acceptance**: Bắt buộc đồng ý điều khoản

## Cấu trúc file

```
app/src/main/
├── java/com/example/preely/
│   ├── model/
│   │   ├── dto/PaymentDto.java          # Data Transfer Object
│   │   └── response/PaymentResponse.java # Response model
│   ├── view/
│   │   └── PaymentActivity.java         # Main payment activity
│   ├── viewmodel/
│   │   └── PaymentService.java          # Payment processing logic
│   └── util/
│       └── PaymentHelper.java           # Helper utilities
├── res/
│   ├── layout/
│   │   └── activity_payment.xml         # Payment UI layout
│   └── drawable/
│       └── payment_icon.xml             # Payment icon
└── AndroidManifest.xml                  # Activity registration
```

## Cách sử dụng

### 1. Mở trang thanh toán cơ bản
```java
PaymentHelper.openPaymentActivity(this);
```

### 2. Mở trang thanh toán với dữ liệu có sẵn
```java
PaymentHelper.openPaymentActivity(this, 99.99, "Premium Subscription");
```

### 3. Xử lý kết quả thanh toán
```java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    
    if (PaymentHelper.isPaymentSuccessful(requestCode, resultCode)) {
        String transactionId = PaymentHelper.getTransactionId(data);
        double amount = PaymentHelper.getAmount(data);
        String paymentMethod = PaymentHelper.getPaymentMethod(data);
        
        // Xử lý thanh toán thành công
        handleSuccessfulPayment(transactionId, amount, paymentMethod);
    }
}
```

## Validation Rules

### Credit Card
- **Card Number**: Format XXXX XXXX XXXX XXXX
- **Expiry Date**: Format MM/YY
- **CVV**: 3-4 chữ số
- **Card Holder**: Tên chủ thẻ (bắt buộc)

### Bank Transfer
- **Bank Name**: Tên ngân hàng (bắt buộc)
- **Account Number**: Số tài khoản (tối thiểu 8 ký tự)

### E-Wallet
- **Wallet Type**: Loại ví (bắt buộc)
- **Wallet Email**: Email hợp lệ (bắt buộc)

### Common
- **Amount**: Số tiền > 0
- **Terms**: Phải đồng ý điều khoản

## Customization

### 1. Thay đổi theme
Chỉnh sửa file `activity_payment.xml` để thay đổi:
- Màu sắc
- Font chữ
- Layout spacing

### 2. Thêm phương thức thanh toán
1. Thêm RadioButton trong `activity_payment.xml`
2. Tạo layout cho form mới
3. Cập nhật `PaymentActivity.java` để xử lý layout mới
4. Thêm validation logic trong `PaymentService.java`

### 3. Tích hợp payment gateway thực
Thay thế logic simulation trong `PaymentService.java`:
```java
private void processPaymentWithGateway(PaymentDto paymentDto, PaymentCallback callback) {
    // Tích hợp với Stripe, PayPal, etc.
    // Gọi API thực tế
    // Xử lý response
}
```

## Error Handling

Hệ thống xử lý các lỗi sau:
- **Validation errors**: Hiển thị thông báo lỗi cụ thể
- **Network errors**: Thông báo lỗi kết nối
- **Payment failures**: Thông báo thanh toán thất bại
- **User cancellation**: Xử lý khi người dùng hủy

## Testing

### Test Cases
1. **Valid credit card payment**
2. **Invalid card number format**
3. **Expired card date**
4. **Bank transfer with valid data**
5. **E-wallet with invalid email**
6. **Payment without accepting terms**
7. **Zero or negative amount**

### Manual Testing
1. Mở ứng dụng
2. Click vào text trong HomeActivity
3. Chọn phương thức thanh toán
4. Nhập dữ liệu test
5. Click "Process Payment"
6. Kiểm tra kết quả

## Dependencies

Hệ thống sử dụng các thư viện có sẵn:
- **Material Design Components**: UI components
- **AndroidX**: Core Android libraries
- **Custom fonts**: Zain font family

## Security Considerations

1. **Data encryption**: Mã hóa dữ liệu nhạy cảm
2. **Input validation**: Kiểm tra dữ liệu đầu vào
3. **Secure storage**: Lưu trữ an toàn thông tin
4. **Network security**: Sử dụng HTTPS
5. **PCI compliance**: Tuân thủ tiêu chuẩn bảo mật

## Future Enhancements

1. **Biometric authentication**: Xác thực sinh trắc học
2. **Saved payment methods**: Lưu phương thức thanh toán
3. **Multiple currencies**: Hỗ trợ nhiều loại tiền tệ
4. **Payment history**: Lịch sử thanh toán
5. **Refund processing**: Xử lý hoàn tiền
6. **Analytics**: Phân tích dữ liệu thanh toán 