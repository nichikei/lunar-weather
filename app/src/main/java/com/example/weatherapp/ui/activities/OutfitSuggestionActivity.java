package com.example.weatherapp.ui.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.data.models.OutfitSuggestion;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.ui.adapters.OutfitSuggestionAdapter;
import com.example.weatherapp.utils.OutfitSuggestionService;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * ACTIVITY GỢI Ý TRANG PHỤC DỰA VÀO THỜI TIẾT (Outfit Suggestion)
 *
 * Activity này sử dụng AI (Gemini API) để gợi ý trang phục phù hợp
 * dựa trên điều kiện thời tiết hiện tại:
 * - Nhiệt độ (nóng/lạnh/ấm)
 * - Điều kiện thời tiết (mưa/nắng/tuyết)
 * - Độ ẩm
 *
 * Quy trình:
 * 1. Nhận dữ liệu thời tiết từ MainActivity
 * 2. Gửi request đến Gemini AI với thông tin thời tiết
 * 3. AI phân tích và trả về danh sách gợi ý trang phục
 * 4. Hiển thị danh sách gợi ý trong RecyclerView
 */
public class OutfitSuggestionActivity extends AppCompatActivity {

    // Progress bar hiển thị khi đang load gợi ý từ AI
    private ProgressBar progressBar;

    // TextView hiển thị thông tin thời tiết (nhiệt độ, tình trạng)
    private TextView tvWeatherInfo;

    // Icon hiển thị điều kiện thời tiết (mưa/nắng/tuyết...)
    private ImageView ivWeatherIcon;

    // Layout chứa nội dung chính (ẩn khi đang load)
    private View layoutContent;

    // Dữ liệu thời tiết nhận từ MainActivity
    private WeatherResponse weatherData;

    // Service xử lý logic gọi AI và phân tích kết quả
    private OutfitSuggestionService outfitService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outfit_suggestion);

        // === KHỞI TẠO CÁC VIEW ===
        RecyclerView rvOutfitSuggestions = findViewById(R.id.rvOutfitSuggestions);
        progressBar = findViewById(R.id.progressBar);
        tvWeatherInfo = findViewById(R.id.tvWeatherInfo);
        ImageView btnBack = findViewById(R.id.btnBack);
        layoutContent = findViewById(R.id.layoutContent);
        ivWeatherIcon = findViewById(R.id.ivWeatherIcon);

        // === SETUP RECYCLERVIEW ===
        // RecyclerView hiển thị danh sách các gợi ý trang phục
        // Mỗi item bao gồm: icon, tên trang phục, mô tả
        rvOutfitSuggestions.setLayoutManager(new LinearLayoutManager(this));
        OutfitSuggestionAdapter adapter = new OutfitSuggestionAdapter(new ArrayList<>());
        rvOutfitSuggestions.setAdapter(adapter);

        // === NHẬN DỮ LIỆU THỜI TIẾT TỪ INTENT ===
        // MainActivity gửi dữ liệu thời tiết qua Intent khi user bấm nút "Outfit Suggestion"
        weatherData = (WeatherResponse) getIntent().getSerializableExtra("weather_data");

        // Kiểm tra dữ liệu có tồn tại không
        if (weatherData == null) {
            Toast.makeText(this, "No weather data available", Toast.LENGTH_SHORT).show();
            finish();  // Đóng Activity nếu không có dữ liệu
            return;
        }

        // Setup nút Back để quay lại màn hình trước
        btnBack.setOnClickListener(v -> finish());

        // Hiển thị thông tin thời tiết ở phần header
        displayWeatherInfo();

        // === KHỞI TẠO SERVICE VÀ GỌI AI ===
        // OutfitSuggestionService chứa logic:
        // - Tạo prompt (câu hỏi) gửi cho AI
        // - Gọi Gemini API
        // - Parse (phân tích) kết quả JSON trả về
        outfitService = new OutfitSuggestionService(this);
        fetchOutfitSuggestions(adapter);
    }

    /**
     * HIỂN THỊ THÔNG TIN THỜI TIẾT Ở PHẦN HEADER
     *
     * Hiển thị:
     * - Tên thành phố
     * - Nhiệt độ hiện tại
     * - Mô tả thời tiết (Partly cloudy, Rainy...)
     * - Icon thời tiết tương ứng
     */
    private void displayWeatherInfo() {
        // Lấy thông tin từ weatherData
        double temp = weatherData.getMain().getTemp();
        String condition = weatherData.getWeather().get(0).getDescription();
        String cityName = weatherData.getName();

        // Hiển thị text: "Hanoi - 25°C, partly cloudy"
        tvWeatherInfo.setText(String.format(Locale.getDefault(),
            "%s - %.0f°C, %s", cityName, temp, condition));

        // Lấy điều kiện thời tiết chính (Clear/Clouds/Rain...)
        String weatherCondition = weatherData.getWeather().get(0).getMain().toLowerCase();

        // Set icon tương ứng
        ivWeatherIcon.setImageResource(getWeatherIconResource(weatherCondition));
    }

    /**
     * GỌI AI ĐỂ LẤY GỢI Ý TRANG PHỤC
     *
     * Flow:
     * 1. Hiển thị ProgressBar, ẩn nội dung
     * 2. Service tạo prompt và gọi Gemini AI
     * 3. AI phân tích thời tiết và trả về JSON gợi ý
     * 4. Parse JSON thành List<OutfitSuggestion>
     * 5. Cập nhật RecyclerView với danh sách gợi ý
     *
     * Ví dụ prompt gửi cho AI:
     * "Nhiệt độ: 25°C, Thời tiết: Partly cloudy, Độ ẩm: 70%
     *  Gợi ý 5-7 món đồ phù hợp để mặc. Format JSON:
     *  [{name: 'T-shirt', description: 'Light cotton t-shirt', icon: 'shirt'}]"
     */
    private void fetchOutfitSuggestions(OutfitSuggestionAdapter adapter) {
        // Hiển thị loading state
        progressBar.setVisibility(View.VISIBLE);
        layoutContent.setVisibility(View.GONE);

        // Gọi service để lấy gợi ý
        // Service sẽ:
        // 1. Build prompt dựa vào weatherData
        // 2. Gọi Gemini API (HTTP request)
        // 3. Nhận response JSON
        // 4. Parse JSON thành List<OutfitSuggestion>
        outfitService.getOutfitSuggestions(weatherData, new OutfitSuggestionService.OutfitSuggestionCallback() {

            /**
             * CALLBACK KHI AI TRẢ VỀ KẾT QUẢ THÀNH CÔNG
             *
             * @param suggestions Danh sách gợi ý trang phục từ AI
             *                    Mỗi item có: name, description, icon
             */
            @Override
            public void onSuccess(List<OutfitSuggestion> suggestions) {
                // runOnUiThread vì callback này chạy trên background thread
                // Phải chuyển về UI thread để update giao diện
                runOnUiThread(() -> {
                    // Ẩn ProgressBar, hiện nội dung
                    progressBar.setVisibility(View.GONE);
                    layoutContent.setVisibility(View.VISIBLE);

                    // Cập nhật adapter với danh sách gợi ý mới
                    // Adapter sẽ tự động refresh RecyclerView
                    adapter.updateSuggestions(suggestions);
                });
            }

            /**
             * CALLBACK KHI CÓ LỖI XẢY RA
             *
             * Lỗi có thể là:
             * - Không có kết nối internet
             * - API key không hợp lệ
             * - AI trả về format không đúng
             * - Rate limit (quá nhiều requests)
             */
            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    progressBar.setVisibility(View.GONE);
                    layoutContent.setVisibility(View.VISIBLE);

                    // Hiển thị thông báo lỗi cho user
                    Toast.makeText(OutfitSuggestionActivity.this,
                            "Error: " + error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    /**
     * LẤY ICON TƯƠNG ỨNG VỚI ĐIỀU KIỆN THỜI TIẾT
     *
     * Map các điều kiện thời tiết sang drawable resource
     *
     * @param weatherCondition Điều kiện thời tiết (clear, clouds, rain...)
     * @return Resource ID của icon drawable
     */
    private int getWeatherIconResource(String weatherCondition) {
        // Map weather condition -> drawable resource
        switch (weatherCondition) {
            case "clear":
                // Trời quang đãng
                return R.drawable.sun_cloud_little_rain;

            case "clouds":
                // Trời nhiều mây
                return R.drawable.moon_cloud_mid_rain;

            case "rain":
                // Mưa to
                return R.drawable.big_rain_drops;

            case "drizzle":
                // Mưa phùn
                return R.drawable.sun_cloud_mid_rain;

            case "thunderstorm":
                // Giông bão
                return R.drawable.cloud_3_zap;

            case "snow":
                // Tuyết
                return R.drawable.big_snow;

            case "mist":
            case "fog":
            case "haze":
                // Sương mù
                return R.drawable.moon_cloud_fast_wind;

            case "tornado":
                // Lốc xoáy
                return R.drawable.moon_fast_wind;

            default:
                // Mặc định
                return R.drawable.sun_cloud_angled_rain;
        }
    }
}
