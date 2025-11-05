# Weather App - Dashboard Tiles, Bottom Dock & Animations

## 📐 Complete Layout Specifications Part 2

This document extends the layout specifications to include Dashboard Tiles, Bottom Dock/FAB, and Effects/Animations.

---

## 4️⃣ Dashboard Tiles (Screen 2 - Grid Layout)

### Grid Configuration
- **Layout**: 2 columns
- **Minimum Height**: 128-144dp (default: 136dp)
- **Spacing**: 12dp between tiles
- **Padding**: Screen padding 20-24dp

```xml
<androidx.recyclerview.widget.RecyclerView
    android:id="@+id/recyclerViewDashboard"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:clipToPadding="false"
    android:paddingHorizontal="20dp"
    app:layoutManager="androidx.recyclerview.widget.GridLayoutManager"
    app:spanCount="2" />
```

### Tile Dimensions & Style
```xml
<androidx.cardview.widget.CardView
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:minHeight="@dimen/tile_min_height"  <!-- 136dp -->
    app:cardCornerRadius="@dimen/tile_radius"  <!-- 24dp -->
    app:cardElevation="0dp"
    android:background="@drawable/tile_background"
    android:padding="@dimen/tile_padding">  <!-- 16dp -->
```

### Tile Structure

#### Basic Tile Layout
```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@drawable/tile_background">

    <!-- Header (Label + Icon) -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal"
        android:gravity="center_vertical">

        <TextView
            android:id="@+id/tvTileLabel"
            android:layout_width="0dp"
            android:layout_height="wrap_content"
            android:layout_weight="1"
            android:textAppearance="@style/TextAppearance.App.TileHeader"
            android:text="HUMIDITY" />

        <ImageView
            android:id="@+id/ivTileIcon"
            android:layout_width="@dimen/tile_header_icon_size"  <!-- 18dp -->
            android:layout_height="@dimen/tile_header_icon_size"
            android:src="@drawable/ic_humidity"
            app:tint="@color/text_low" />
    </LinearLayout>

    <!-- Primary Value -->
    <TextView
        android:id="@+id/tvTileValuePrimary"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="12dp"
        android:textAppearance="@style/TextAppearance.App.TileValuePrimary"
        android:text="65%" />

    <!-- Secondary Value (optional) -->
    <TextView
        android:id="@+id/tvTileValueSecondary"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_marginTop="4dp"
        android:textAppearance="@style/TextAppearance.App.TileValueSecondary"
        android:text="Feels comfortable" />
</LinearLayout>
```

---

### Tiles with Mini Charts

#### Line Chart Tile
```xml
<LinearLayout
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@drawable/tile_background">

    <!-- Header -->
    <LinearLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        android:orientation="horizontal">

        <TextView
            android:textAppearance="@style/TextAppearance.App.TileHeader"
            android:text="TEMPERATURE" />

        <ImageView
            android:layout_width="18dp"
            android:layout_height="18dp"
            android:src="@drawable/ic_temperature" />
    </LinearLayout>

    <!-- Mini Line Chart -->
    <com.github.mikephil.charting.charts.LineChart
        android:id="@+id/miniChart"
        android:layout_width="match_parent"
        android:layout_height="@dimen/mini_chart_height"  <!-- 40-56dp, default: 48dp -->
        android:layout_marginTop="8dp" />

    <!-- Current Value -->
    <TextView
        android:textAppearance="@style/TextAppearance.App.TileValuePrimary"
        android:text="24°C"
        android:layout_marginTop="8dp" />
</LinearLayout>
```

**Chart Specifications:**
- **Height**: 40-56dp (default: 48dp)
- **Stroke Width**: 1.5dp
- **Stroke Opacity**: 70%
- **Colors**: Use Accent 400-600 gradient

```kotlin
// Chart configuration
lineChart.apply {
    setDrawGridBackground(false)
    description.isEnabled = false
    legend.isEnabled = false
    axisLeft.isEnabled = false
    axisRight.isEnabled = false
    xAxis.isEnabled = false
    
    // Line styling
    val dataSet = LineDataSet(entries, "").apply {
        color = ContextCompat.getColor(context, R.color.accent_500)
        lineWidth = 1.5f
        setDrawCircles(false)
        setDrawValues(false)
        mode = LineDataSet.Mode.CUBIC_BEZIER
        fillAlpha = 70 // 70% opacity
        setDrawFilled(true)
        fillColor = ContextCompat.getColor(context, R.color.accent_400)
    }
}
```

---

### Wind Compass Tile

```xml
<LinearLayout
    android:orientation="vertical"
    android:padding="16dp"
    android:background="@drawable/tile_background"
    android:gravity="center">

    <!-- Header -->
    <TextView
        android:textAppearance="@style/TextAppearance.App.TileHeader"
        android:text="WIND"
        android:layout_gravity="start" />

    <!-- Compass View -->
    <com.example.weatherapp.ui.views.WindCompassView
        android:id="@+id/windCompass"
        android:layout_width="@dimen/compass_size"  <!-- 80dp -->
        android:layout_height="@dimen/compass_size"
        android:layout_marginVertical="12dp" />

    <!-- Wind Speed -->
    <TextView
        android:textAppearance="@style/TextAppearance.App.TileValuePrimary"
        android:text="12 km/h" />

    <TextView
        android:textAppearance="@style/TextAppearance.App.TileValueSecondary"
        android:text="NE" />
</LinearLayout>
```

**Compass Specifications:**
- **Circle Stroke**: 2dp
- **Tick Marks**: 0.75dp
- **Text**: 12sp Regular
- **Size**: 80dp × 80dp

```kotlin
class WindCompassView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.compass_stroke_width) // 2dp
        color = ContextCompat.getColor(context, R.color.stroke_on_glass)
    }

    private val tickPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = resources.getDimension(R.dimen.compass_tick_width) // 0.75dp
        color = ContextCompat.getColor(context, R.color.text_mid)
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = resources.getDimension(R.dimen.compass_text_size) // 12sp
        color = ContextCompat.getColor(context, R.color.text_mid)
        textAlign = Paint.Align.CENTER
    }

    // Draw compass implementation...
}
```

---

## 5️⃣ Bottom Dock / FAB

### Dock Container Specifications

#### Height & Background
```xml
<FrameLayout
    android:id="@+id/bottomDock"
    android:layout_width="match_parent"
    android:layout_height="@dimen/dock_height"  <!-- 88-96dp, default: 92dp -->
    android:background="@drawable/dock_background"
    android:elevation="@dimen/elevation_bottom_dock"
    android:layout_gravity="bottom">
```

**Background Gradient:**
- Primary700 → Primary800
- Opacity: 85-95% (D9-F2 in hex)
- Colors: `#D933248F` → `#F2251B6A`

### Middle FAB

```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    android:id="@+id/fabCenter"
    android:layout_width="@dimen/dock_fab_size"  <!-- 56dp -->
    android:layout_height="@dimen/dock_fab_size"
    android:layout_gravity="center|top"
    android:layout_marginTop="-28dp"
    app:elevation="@dimen/dock_fab_elevation"  <!-- 12dp -->
    app:backgroundTint="@color/accent_600"
    android:src="@drawable/ic_add"
    app:tint="@color/white"
    app:fabSize="normal" />
```

**FAB Icon:**
- Size: 24dp
- Color: White (#FFFFFF)
- Shadow: Deep elevation (12dp)

### Dock Menu Items (Left & Right)

```xml
<LinearLayout
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="horizontal"
    android:gravity="center_vertical"
    android:paddingHorizontal="24dp">

    <!-- Left Menu Item -->
    <LinearLayout
        android:layout_width="0dp"
        android:layout_weight="1"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        android:clickable="true"
        android:focusable="true"
        android:background="?attr/selectableItemBackgroundBorderless">

        <ImageView
            android:layout_width="@dimen/dock_menu_icon_size"  <!-- 24dp -->
            android:layout_height="@dimen/dock_menu_icon_size"
            android:src="@drawable/ic_menu_home"
            app:tint="@color/text_mid" />

        <TextView
            android:layout_width="wrap_content"
            android:layout_height="wrap_content"
            android:layout_marginTop="4dp"
            android:textAppearance="@style/TextAppearance.App.DockMenuLabel"
            android:text="Home" />
    </LinearLayout>

    <!-- Spacer for FAB -->
    <View
        android:layout_width="80dp"
        android:layout_height="1dp" />

    <!-- Right Menu Item -->
    <LinearLayout
        android:layout_width="0dp"
        android:layout_weight="1"
        android:layout_height="wrap_content"
        android:orientation="vertical"
        android:gravity="center"
        android:clickable="true"
        android:focusable="true"
        android:background="?attr/selectableItemBackgroundBorderless">

        <ImageView
            android:layout_width="24dp"
            android:layout_height="24dp"
            android:src="@drawable/ic_menu_settings"
            app:tint="@color/text_mid" />

        <TextView
            android:textAppearance="@style/TextAppearance.App.DockMenuLabel"
            android:text="Settings"
            android:layout_marginTop="4dp" />
    </LinearLayout>
</LinearLayout>
```

---

## 6️⃣ Effects & Animations

### Tab Transition Animation

**Duration**: 200-250ms (default: 225ms)  
**Interpolator**: FastOutSlowIn

```kotlin
// ViewPager2 with TabLayout
viewPager.apply {
    setPageTransformer { page, position ->
        page.apply {
            alpha = 0f + (1f - abs(position))
            translationX = -position * width * 0.05f
        }
    }
}

// Apply custom animation
TabLayoutMediator(tabLayout, viewPager) { tab, position ->
    tab.text = titles[position]
}.attach()
```

**Using XML animations:**
```kotlin
viewPager.setPageTransformer { page, position ->
    val animation = AnimationUtils.loadAnimation(
        context, 
        R.anim.tab_transition_enter
    )
    animation.duration = resources.getInteger(R.integer.anim_tab_transition_duration).toLong()
    page.startAnimation(animation)
}
```

---

### Hourly Pill Press Effect

**Scale**: 0.98  
**Overlay Brightness**: +10%  
**Duration**: 150ms

```xml
<!-- Use selector drawable -->
<androidx.cardview.widget.CardView
    android:foreground="@drawable/hourly_pill_selector"
    android:clickable="true"
    android:focusable="true" />
```

**Programmatic animation:**
```kotlin
pillView.setOnTouchListener { view, event ->
    when (event.action) {
        MotionEvent.ACTION_DOWN -> {
            view.animate()
                .scaleX(0.98f)
                .scaleY(0.98f)
                .setDuration(150)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
        MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
            view.animate()
                .scaleX(1.0f)
                .scaleY(1.0f)
                .setDuration(150)
                .setInterpolator(FastOutSlowInInterpolator())
                .start()
        }
    }
    false
}
```

---

### Lottie Weather Animations

**Frame Rate**: 30-60fps  
**Color Sync**: Accent 400-600 (#C07BFF - #7B5CFF)

```xml
<com.airbnb.lottie.LottieAnimationView
    android:id="@+id/lottieWeather"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:lottie_autoPlay="true"
    app:lottie_loop="true"
    app:lottie_fileName="weather_snow.json"
    app:lottie_speed="1.0"
    app:lottie_colorFilter="@color/accent_500" />
```

**Configuration in code:**
```kotlin
lottieWeather.apply {
    setAnimation("weather_rain.json")
    repeatCount = LottieDrawable.INFINITE
    speed = 1.0f // 30-60fps
    
    // Color sync with accent colors
    addValueCallback(
        KeyPath("**"),
        LottieProperty.COLOR_FILTER
    ) {
        PorterDuffColorFilter(
            ContextCompat.getColor(context, R.color.accent_500),
            PorterDuff.Mode.SRC_ATOP
        )
    }
    
    playAnimation()
}
```

---

### Scroll Hero → Dock Parallax Effect

**Parallax Ratio**: 0.8  
**Blur**: Gradually increase to 30px

```kotlin
scrollView.setOnScrollChangeListener { _, _, scrollY, _, _ ->
    val parallaxRatio = 0.8f
    val maxBlur = 30f
    
    // Parallax effect on hero
    heroContainer.translationY = -scrollY * parallaxRatio
    
    // Blur effect (requires RenderScript or alternative)
    val blurAmount = (scrollY / scrollView.height.toFloat() * maxBlur).coerceAtMost(maxBlur)
    applyBlur(heroContainer, blurAmount)
    
    // Fade effect
    val alpha = 1f - (scrollY / 500f).coerceIn(0f, 1f)
    heroContainer.alpha = alpha
}
```

**Using NestedScrollView with CoordinatorLayout:**
```xml
<androidx.coordinatorlayout.widget.CoordinatorLayout>
    
    <com.google.android.material.appbar.AppBarLayout
        android:layout_width="match_parent"
        android:layout_height="wrap_content"
        app:elevation="0dp">
        
        <com.google.android.material.appbar.CollapsingToolbarLayout
            android:layout_width="match_parent"
            android:layout_height="match_parent"
            app:layout_scrollFlags="scroll|exitUntilCollapsed"
            app:collapsedTitleTextAppearance="@style/TextAppearance.App.CityTitle"
            app:contentScrim="@drawable/gradient_background">
            
            <!-- Hero content here -->
            
        </com.google.android.material.appbar.CollapsingToolbarLayout>
    </com.google.android.material.appbar.AppBarLayout>
    
    <!-- Main content -->
    
</androidx.coordinatorlayout.widget.CoordinatorLayout>
```

---

## 📏 Complete Dimension Reference (Extended)

| Element | Dimension | Resource |
|---------|-----------|----------|
| **Dashboard Tiles** |
| Tile min height | 128-144dp (136dp) | `@dimen/tile_min_height` |
| Tile radius | 24dp | `@dimen/tile_radius` |
| Tile padding | 16dp | `@dimen/tile_padding` |
| Header icon | 18dp | `@dimen/tile_header_icon_size` |
| Header text | 14sp | `@dimen/tile_header_text_size` |
| Primary value | 20sp | `@dimen/tile_value_primary_size` |
| Secondary value | 12sp | `@dimen/tile_value_secondary_size` |
| Mini chart height | 40-56dp (48dp) | `@dimen/mini_chart_height` |
| Chart stroke | 1.5dp | `@dimen/mini_chart_stroke_width` |
| Compass size | 80dp | `@dimen/compass_size` |
| Compass stroke | 2dp | `@dimen/compass_stroke_width` |
| Compass tick | 0.75dp | `@dimen/compass_tick_width` |
| **Bottom Dock** |
| Dock height | 88-96dp (92dp) | `@dimen/dock_height` |
| FAB size | 56dp | `@dimen/dock_fab_size` |
| FAB elevation | 12dp | `@dimen/dock_fab_elevation` |
| FAB icon | 24dp | `@dimen/dock_fab_icon_size` |
| Menu icon | 24dp | `@dimen/dock_menu_icon_size` |
| Menu label | 10-12sp (11sp) | `@dimen/dock_menu_label_size` |
| **Animations** |
| Tab transition | 225ms | `@integer/anim_tab_transition_duration` |
| Pill press | 150ms | `@integer/anim_pill_press_duration` |
| Lottie FPS | 30-60 | `@integer/anim_lottie_fps_min/max` |
| Scale pressed | 0.98 | `@dimen/scale_pill_pressed` |
| Brighten overlay | 10% | `@dimen/overlay_brighten_amount` |
| Parallax ratio | 0.8 | `@dimen/parallax_scroll_ratio` |
| Max blur | 30px | `@dimen/blur_max_radius` |

---

## 🎨 New Drawable Resources

| Drawable | Purpose |
|----------|---------|
| `tile_background.xml` | Dashboard tile with glass morphism |
| `dock_background.xml` | Bottom dock gradient (Primary700→800) |
| `hourly_pill_selector.xml` | Press effect for hourly pills |

---

## 🎬 Animation Resources

| File | Purpose |
|------|---------|
| `animations.xml` | Animation style definitions |
| `tab_transition_enter.xml` | Tab enter animation |
| `tab_transition_exit.xml` | Tab exit animation |

---

## ✅ Implementation Checklist Part 2

- [ ] Dashboard tiles in 2-column grid layout
- [ ] Tiles with 24dp radius and 16dp padding
- [ ] Tile headers with 18dp icon and 14sp Medium text
- [ ] Primary values 20sp SemiBold, secondary 12sp Regular
- [ ] Mini charts with 48dp height and 1.5dp stroke
- [ ] Wind compass with 2dp stroke and 0.75dp ticks
- [ ] Bottom dock 92dp height with gradient background
- [ ] Center FAB 56dp with 12dp elevation
- [ ] Dock menu icons 24dp with 11sp labels
- [ ] Tab transitions 225ms with FastOutSlowIn
- [ ] Pill press effect: scale 0.98 + brighten 10%
- [ ] Lottie animations 30-60fps with accent colors
- [ ] Parallax scroll ratio 0.8 with blur effect

---

**All resources for Dashboard Tiles, Bottom Dock, and Animations are ready!** 🚀

