# Weather App - Detailed Layout Specifications

## 📐 Layout Structure Overview

This document provides detailed specifications for implementing the Weather Hero, Tab Container, and Hourly Pills components according to the design system.

---

## 1️⃣ Weather Hero (Top Section)

### Padding & Spacing
```xml
<!-- Status bar padding -->
android:paddingTop="@dimen/hero_padding_top"  <!-- 24-32dp, default: 28dp -->
```

### Text Hierarchy
The hero section contains 3 text elements stacked vertically:

1. **City Title** - 32sp Medium
   ```xml
   <TextView
       android:textAppearance="@style/TextAppearance.App.CityTitle"
       android:textSize="@dimen/text_size_city_title"  <!-- 32sp -->
       android:letterSpacing="@dimen/letter_spacing_city_title"  <!-- -0.008 -->
       android:textColor="@color/text_high" />
   ```

2. **Temperature Display** - 64sp Light
   ```xml
   <TextView
       android:textAppearance="@style/TextAppearance.App.DisplayTemp"
       android:textSize="@dimen/text_size_display_temp"  <!-- 64sp -->
       android:letterSpacing="@dimen/letter_spacing_display_temp"  <!-- -0.01 -->
       android:textColor="@color/text_high" />
   ```

3. **Subtitle/Condition** - 18sp Regular
   ```xml
   <TextView
       android:textAppearance="@style/TextAppearance.App.Subtitle"
       android:textSize="@dimen/text_size_subtitle"  <!-- 18sp -->
       android:textColor="@color/text_mid" />
   ```

### Text Spacing Between Elements
```xml
<!-- Between City and Temperature -->
android:layout_marginTop="@dimen/hero_text_spacing_small"  <!-- 6dp -->

<!-- Between Temperature and Subtitle -->
android:layout_marginTop="@dimen/hero_text_spacing_medium"  <!-- 10dp -->
```

### Weather Illustration
```xml
<ImageView
    android:id="@+id/weatherIllustration"
    android:layout_width="match_parent"
    android:layout_height="0dp"
    android:layout_weight="0.38"  <!-- 36-42% of screen, default: 38% -->
    android:layout_gravity="bottom"
    android:scaleType="fitEnd"
    android:adjustViewBounds="true" />
```

**Note**: Illustration should be aligned to the bottom of the hero section and take up 36-42% of screen height.

---

## 2️⃣ Tab Container (Hourly / Weekly)

### Container Specifications

#### Height & Radius
```xml
<com.google.android.material.tabs.TabLayout
    android:layout_width="match_parent"
    android:layout_height="@dimen/tab_container_height"  <!-- 56-64dp, default: 60dp -->
    android:background="@drawable/tab_container_background"
    app:tabIndicatorHeight="@dimen/tab_indicator_height"  <!-- 2dp -->
    app:tabIndicator="@drawable/tab_indicator"
    app:tabIndicatorColor="@color/accent_400"
    android:padding="@dimen/tab_inner_padding" />  <!-- 16dp inner padding -->
```

#### Background Composition
The tab container uses a layered background (`tab_container_background.xml`):
1. **Card Accent Gradient** (#2A1B6B → #3A217F at 180°)
2. **Glass Overlay** (#14FFFFFF - 8% white)
3. **Stroke** (#33FFFFFF - 20% white, 1dp)
4. **Corner Radius**: 30dp

#### Tab Indicator
```xml
<!-- Thin line indicator: 2dp height, Accent 400 color -->
app:tabIndicatorHeight="2dp"
app:tabIndicatorColor="@color/accent_400"  <!-- #C07BFF -->
```

#### Tab Labels
```xml
<TextView
    android:textAppearance="@style/TextAppearance.App.TabLabel"
    android:textSize="@dimen/tab_label_size"  <!-- 14sp -->
    android:textColor="@color/tab_label_color"  <!-- #E6E8FF -->
    android:fontFamily="@font/poppins_medium" />
```

### Usage Example
```xml
<com.google.android.material.tabs.TabLayout
    android:id="@+id/tabLayout"
    android:layout_width="match_parent"
    android:layout_height="60dp"
    android:background="@drawable/tab_container_background"
    app:tabMode="fixed"
    app:tabGravity="fill"
    app:tabIndicatorHeight="2dp"
    app:tabIndicator="@drawable/tab_indicator"
    app:tabTextAppearance="@style/TextAppearance.App.TabLabel"
    app:tabSelectedTextColor="@color/text_high"
    app:tabTextColor="@color/tab_label_color"
    android:paddingHorizontal="16dp"
    android:paddingVertical="8dp"
    app:tabIndicatorFullWidth="false" />
```

---

## 3️⃣ Hourly Pills (Forecast Items)

### Pill Dimensions
```xml
<androidx.cardview.widget.CardView
    android:layout_width="@dimen/hourly_pill_width"  <!-- 68-76dp, default: 72dp -->
    android:layout_height="@dimen/hourly_pill_height"  <!-- 120-132dp, default: 126dp -->
    app:cardCornerRadius="@dimen/hourly_pill_radius"  <!-- 22dp -->
    app:cardElevation="0dp"
    android:background="@drawable/hourly_card_background">
```

### Pill Background
- **Normal State**: `@drawable/hourly_card_background`
  - Glass morphism: #14FFFFFF (8% white)
  - Stroke: #33FFFFFF (20% white, 1dp)
  - Radius: 22dp

- **Active State** (current hour): `@drawable/hourly_card_active`
  - Background: @color/primary_700 (#33248F)
  - Stroke: #80FFFFFF (50% white, 1dp)
  - Radius: 22dp

### Pill Content Structure
```xml
<LinearLayout
    android:layout_width="72dp"
    android:layout_height="126dp"
    android:orientation="vertical"
    android:gravity="center"
    android:padding="8dp">

    <!-- Time Label -->
    <TextView
        android:textAppearance="@style/TextAppearance.App.Caption"
        android:textSize="12sp"
        android:textColor="@color/text_low"
        android:text="Now" />

    <!-- Weather Icon -->
    <ImageView
        android:layout_width="@dimen/hourly_pill_icon_size"  <!-- 28-32dp, default: 30dp -->
        android:layout_height="@dimen/hourly_pill_icon_size"
        android:layout_marginVertical="8dp"
        android:src="@drawable/ic_weather_sunny" />

    <!-- Chance of Rain -->
    <TextView
        android:textAppearance="@style/TextAppearance.App.Caption"
        android:textSize="12sp"
        android:textColor="@color/info"
        android:text="30%"
        android:drawableStart="@drawable/ic_raindrop"
        android:drawablePadding="2dp" />

    <!-- Temperature -->
    <TextView
        android:textAppearance="@style/TextAppearance.App.HourlyTemp"
        android:textSize="@dimen/hourly_pill_temp_size"  <!-- 18-20sp, default: 19sp -->
        android:textColor="@color/text_high"
        android:text="24°"
        android:layout_marginTop="4dp" />
</LinearLayout>
```

### RecyclerView Configuration
```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerViewHourly"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:clipToPadding="false"
    android:paddingHorizontal="16dp"
    app:layoutManager="androidx.recyclerview.widget.LinearLayoutManager" />
```

### ItemDecoration for Spacing
```kotlin
// Add 12dp spacing between pills
recyclerViewHourly.addItemDecoration(object : RecyclerView.ItemDecoration() {
    override fun getItemOffsets(
        outRect: Rect,
        view: View,
        parent: RecyclerView,
        state: RecyclerView.State
    ) {
        outRect.right = resources.getDimensionPixelSize(R.dimen.hourly_pill_item_spacing) // 12dp
    }
})
```

### Snap Behavior (Horizontal Scroll)
```kotlin
// Add snap helper for smooth pill scrolling
val snapHelper = LinearSnapHelper()
snapHelper.attachToRecyclerView(recyclerViewHourly)
```

---

## 📏 Complete Dimension Reference

| Element | Dimension | Resource |
|---------|-----------|----------|
| **Weather Hero** |
| Status bar padding | 24-32dp (28dp) | `@dimen/hero_padding_top` |
| City to Temp spacing | 6dp | `@dimen/hero_text_spacing_small` |
| Temp to Subtitle spacing | 10dp | `@dimen/hero_text_spacing_medium` |
| Illustration height | 36-42% screen | Calculate programmatically |
| **Tab Container** |
| Container height | 56-64dp (60dp) | `@dimen/tab_container_height` |
| Container radius | 30dp | `@dimen/tab_container_radius` |
| Indicator height | 2dp | `@dimen/tab_indicator_height` |
| Label size | 14sp | `@dimen/tab_label_size` |
| Inner padding | 16dp | `@dimen/tab_inner_padding` |
| **Hourly Pills** |
| Pill width | 68-76dp (72dp) | `@dimen/hourly_pill_width` |
| Pill height | 120-132dp (126dp) | `@dimen/hourly_pill_height` |
| Pill radius | 22dp | `@dimen/hourly_pill_radius` |
| Icon size | 28-32dp (30dp) | `@dimen/hourly_pill_icon_size` |
| Temperature size | 18-20sp (19sp) | `@dimen/hourly_pill_temp_size` |
| Item spacing | 12dp | `@dimen/hourly_pill_item_spacing` |

---

## 🎨 Color Reference

| Element | Color | Resource |
|---------|-------|----------|
| Tab label | #E6E8FF | `@color/tab_label_color` |
| Tab indicator | #C07BFF (Accent 400) | `@color/accent_400` |
| Pill background | #14FFFFFF (8% white) | `@color/surface_glass` |
| Pill stroke | #33FFFFFF (20% white) | `@color/stroke_on_glass` |
| Active pill bg | #33248F (Primary 700) | `@color/primary_700` |

---

## 📱 Implementation Tips

### 1. Weather Hero Layout
```xml
<LinearLayout
    android:orientation="vertical"
    android:paddingTop="@dimen/hero_padding_top"
    android:paddingHorizontal="@dimen/screen_padding_large">
    
    <TextView
        android:id="@+id/tvCityName"
        android:textAppearance="@style/TextAppearance.App.CityTitle" />
    
    <TextView
        android:id="@+id/tvTemperature"
        android:textAppearance="@style/TextAppearance.App.DisplayTemp"
        android:layout_marginTop="@dimen/hero_text_spacing_small" />
    
    <TextView
        android:id="@+id/tvCondition"
        android:textAppearance="@style/TextAppearance.App.Subtitle"
        android:layout_marginTop="@dimen/hero_text_spacing_medium" />
</LinearLayout>
```

### 2. Tab Container with ViewPager2
```xml
<LinearLayout android:orientation="vertical">
    <com.google.android.material.tabs.TabLayout
        android:id="@+id/tabLayout"
        android:layout_width="match_parent"
        android:layout_height="@dimen/tab_container_height"
        android:background="@drawable/tab_container_background" />
    
    <androidx.viewpager2.widget.ViewPager2
        android:id="@+id/viewPager"
        android:layout_width="match_parent"
        android:layout_height="0dp"
        android:layout_weight="1" />
</LinearLayout>
```

### 3. Hourly Pills Adapter ViewHolder
```kotlin
class HourlyForecastViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    fun bind(forecast: HourlyForecast, isNow: Boolean) {
        itemView.background = if (isNow) {
            ContextCompat.getDrawable(context, R.drawable.hourly_card_active)
        } else {
            ContextCompat.getDrawable(context, R.drawable.hourly_card_background)
        }
    }
}
```

---

## ✅ Implementation Checklist

- [ ] Weather Hero padding and text spacing applied
- [ ] Hero text using correct TextAppearance styles
- [ ] Weather illustration sized to 36-42% of screen
- [ ] Tab container using `tab_container_background.xml`
- [ ] Tab indicator styled with 2dp height and Accent 400 color
- [ ] Tab labels using 14sp Medium with #E6E8FF color
- [ ] Hourly pills sized to 72dp × 126dp with 22dp radius
- [ ] Pills using glass morphism background
- [ ] Pill content properly structured (icon 30dp, temp 19sp)
- [ ] 12dp spacing between pills with ItemDecoration
- [ ] Snap behavior enabled for smooth scrolling
- [ ] Active pill state differentiated

---

**All resources have been created and are ready to use!** 🚀

