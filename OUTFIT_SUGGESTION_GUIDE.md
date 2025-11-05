# Hướng Dẫn Sử Dụng Tính Năng Gợi Ý Outfit Bằng AI

## Tổng Quan

Tính năng **AI Outfit Suggestions** đã được thêm vào ứng dụng WeatherApp. Tính năng này sử dụng dữ liệu thời tiết hiện tại để đề xuất trang phục phù hợp.

## Tính Năng

✅ **Gợi ý trang phục thông minh** dựa trên:
- Nhiệt độ
- Điều kiện thời tiết (mưa, nắng, tuyết, v.v.)
- Tốc độ gió
- Độ ẩm

✅ **5 Danh Mục Gợi Ý:**
1. **Upper Body** - Áo khoác, áo thun, áo len
2. **Lower Body** - Quần dài, quần short
3. **Footwear** - Giày, dép, ủng
4. **Accessories** - Kính, ô, khăn choàng
5. **Extra Tips** - Lời khuyên bổ sung

✅ **Hai Chế Độ Hoạt Động:**
- **Chế độ mặc định**: Sử dụng thuật toán tích hợp (không cần API key) - **HOẠT ĐỘNG OFFLINE**
- **Chế độ AI nâng cao**: Sử dụng Google Gemini (cần API key) - **MIỄN PHÍ 100%**

## Cách Sử Dụng

### 1. Mở Tính Năng
Trong màn hình chính của app, nhấn vào nút:
```
👔 AI Outfit Suggestions
```

### 2. Xem Gợi Ý
App sẽ phân tích thời tiết hiện tại và hiển thị 5 gợi ý trang phục với:
- **Emoji** minh họa
- **Tên danh mục**
- **Gợi ý cụ thể**
- **Lý do** tại sao nên mặc

## 🆓 Nâng Cấp Lên AI Mode - MIỄN PHÍ HOÀN TOÀN!

Để sử dụng Google Gemini AI cho gợi ý thông minh hơn:

### Bước 1: Lấy Google Gemini API Key (MIỄN PHÍ)

1. Truy cập: **https://makersuite.google.com/app/apikey**
2. Đăng nhập bằng tài khoản Google
3. Nhấn **"Create API Key"**
4. Chọn project hoặc tạo project mới
5. Copy API key (bắt đầu bằng `AIza...`)

**Ưu điểm:**
- ✅ **Hoàn toàn MIỄN PHÍ**
- ✅ Không cần thẻ tín dụng
- ✅ 60 requests/phút
- ✅ Không giới hạn số request hàng tháng
- ✅ Chất lượng tốt, tương đương GPT-3.5

### Bước 2: Thêm API Key Vào App

Mở file: `app/src/main/java/com/example/weatherapp/OutfitSuggestionService.java`

Tìm dòng:
```java
private static final String GEMINI_API_KEY = "YOUR_GEMINI_API_KEY_HERE";
```

Thay thế bằng API key của bạn:
```java
private static final String GEMINI_API_KEY = "AIzaSyYour-Actual-API-Key-Here";
```

⚠️ **Lưu Ý Bảo Mật:**
- Không share API key
- Không commit API key lên GitHub
- Nên dùng environment variables cho production

### Bước 3: Build Lại App

```bash
# Trong Android Studio
Build > Rebuild Project
```

## So Sánh Các API AI Miễn Phí

| API | Miễn Phí | Giới Hạn | Chất Lượng | Yêu Cầu Thẻ |
|-----|----------|-----------|------------|-------------|
| **Google Gemini** | ✅ | 60 req/phút | ⭐⭐⭐⭐⭐ | ❌ Không |
| Hugging Face | ✅ | Giới hạn | ⭐⭐⭐⭐ | ❌ Không |
| Cohere | ✅ | 100 req/phút | ⭐⭐⭐⭐ | ❌ Không |
| OpenAI | ❌ Tính phí | $0.002/1K tokens | ⭐⭐⭐⭐⭐ | ✅ Cần |

**Khuyến nghị:** Dùng **Google Gemini** - miễn phí, mạnh, không cần thẻ!

## Cấu Trúc Code

### Files Đã Thêm

```
app/src/main/java/com/example/weatherapp/
├── OutfitSuggestion.java           # Model class
├── OutfitSuggestionActivity.java   # Main UI
├── OutfitSuggestionAdapter.java    # RecyclerView adapter
├── OutfitSuggestionService.java    # AI logic (Gemini API)

app/src/main/res/layout/
├── activity_outfit_suggestion.xml  # Main layout
└── item_outfit_suggestion.xml      # Item layout

app/src/main/res/drawable/
└── circle_background.xml           # Emoji background
```

### Cách Hoạt Động

```
1. User nhấn nút "AI Outfit Suggestions"
2. MainActivity truyền WeatherResponse data
3. OutfitSuggestionActivity nhận data
4. OutfitSuggestionService phân tích thời tiết
5. Nếu có Gemini key → Gọi Gemini API (MIỄN PHÍ)
6. Nếu không → Dùng thuật toán tích hợp (OFFLINE)
7. Hiển thị kết quả trong RecyclerView
```

## Ví Dụ Gợi Ý

### Thời tiết: 15°C, Có mây

**Upper Body:** 🧥
- Gợi ý: Light jacket or sweater
- Lý do: Mild temperature, a light layer is perfect

**Lower Body:** 👖
- Gợi ý: Jeans or casual pants
- Lý do: Comfortable for moderate temperatures

**Footwear:** 👟
- Gợi ý: Casual sneakers or shoes
- Lý do: Versatile footwear for pleasant weather

**Accessories:** 🕶️
- Gợi ý: Sunglasses
- Lý do: Optional but recommended for comfort

**Extra Tips:** ✨
- Gợi ý: Dress in layers for flexibility
- Lý do: Easy to adjust to temperature changes

## Tùy Chỉnh

### Thêm Danh Mục Mới

Trong `OutfitSuggestionService.java`, thêm vào method `getDefaultOutfitSuggestions()`:

```java
suggestions.add(new OutfitSuggestion(
    "Tên Danh Mục",
    "Gợi ý cụ thể",
    "Lý do",
    "🎒" // Emoji
));
```

### Thay Đổi Logic Gợi Ý

Chỉnh sửa các điều kiện trong `getDefaultOutfitSuggestions()`:

```java
if (temp < 10) {
    // Trời lạnh
} else if (temp < 20) {
    // Trời mát
} else if (temp < 28) {
    // Trời ấm
} else {
    // Trời nóng
}
```

## Xử Lý Sự Cố

### Lỗi: "No weather data available"
**Giải pháp:** Đợi app tải xong dữ liệu thời tiết trước khi mở tính năng

### Lỗi: OpenAI API không hoạt động
**Giải pháp:** 
- Kiểm tra API key có đúng không
- Kiểm tra còn credits trong tài khoản OpenAI
- App sẽ tự động chuyển sang chế độ mặc định

### UI không hiển thị đúng
**Giải pháp:**
- Clean project: Build > Clean Project
- Rebuild: Build > Rebuild Project
- Sync Gradle files

## Chi Phí API

### Google Gemini API (Khuyến nghị)
- **Miễn phí:** 60 requests/phút
- **Không giới hạn:** Số lượng requests hàng tháng
- **Không cần thẻ:** Không yêu cầu thông tin thanh toán
- **Chi phí:** $0 (MIỄN PHÍ VĨNH VIỄN)

### So sánh với OpenAI (Tham khảo)
- GPT-3.5 Turbo: ~$0.002 / 1000 tokens
- Mỗi gợi ý: ~500-800 tokens
- Chi phí ước tính: ~$0.001 - $0.002 / lần gợi ý

**→ Gemini tiết kiệm 100% chi phí!**

## Roadmap

### Phiên Bản Tương Lai

- [ ] Lưu lịch sử gợi ý
- [ ] Tùy chỉnh phong cách (casual, formal, sport)
- [ ] Gợi ý dựa trên giới tính
- [ ] Tích hợp với calendar (gợi ý cho sự kiện)
- [ ] Hỗ trợ nhiều ngôn ngữ
- [ ] Offline mode với ML model tích hợp

## Liên Hệ & Hỗ Trợ

Nếu có vấn đề hoặc câu hỏi, vui lòng:
1. Kiểm tra phần Xử Lý Sự Cố ở trên
2. Xem logs trong Android Studio Logcat
3. Tạo issue trên GitHub repository

---

**Chúc bạn có trải nghiệm tuyệt vời với tính năng AI Outfit Suggestions! 🎉**
