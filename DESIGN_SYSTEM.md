# Weather App - Design System Implementation

## 🎨 Typography System

### Font Family
Currently using **Poppins** (can be replaced with **Inter** for closer match to SF Pro)

### Text Styles

| Style | Size | Weight | Letter Spacing | Usage | Style Reference |
|-------|------|--------|----------------|-------|-----------------|
| **Display Temp** | 64sp | Light | -0.5sp | Main temperature display | `TextAppearance.App.DisplayTemp` |
| **City Title** | 32sp | Medium | -0.25sp | Location/city name | `TextAppearance.App.CityTitle` |
| **Subtitle/Condition** | 18sp | Regular | 0 | Weather condition text | `TextAppearance.App.Subtitle` |
| **Metric Value** | 20sp | SemiBold | 0 | Card metric values | `TextAppearance.App.MetricValue` |
| **Metric Label** | 14sp | Medium | +0.2sp | Card metric labels | `TextAppearance.App.MetricLabel` |
| **Caption/Hint** | 12sp | Regular | +0.2sp | Hints, small text | `TextAppearance.App.Caption` |

### Usage in XML
```xml
<TextView
    android:textAppearance="@style/TextAppearance.App.DisplayTemp"
    android:text="24°" />
```

---

## 📏 Spacing & Grid System

### Base Unit
- **8dp** - All spacing should be multiples of 8dp

### Spacing Values
```xml
@dimen/spacing_xs        <!-- 4dp -->
@dimen/spacing_small     <!-- 8dp -->
@dimen/spacing_medium    <!-- 12dp -->
@dimen/spacing_large     <!-- 16dp -->
@dimen/spacing_xl        <!-- 24dp -->
@dimen/spacing_xxl       <!-- 32dp -->
```

### Layout Spacing
- **Gutter (List/Grid)**: `@dimen/gutter_list` (16dp)
- **Screen Padding**: `@dimen/screen_padding_small` (20dp) to `@dimen/screen_padding_large` (24dp)
- **Card Padding Vertical**: `@dimen/card_padding_vertical` (16dp)
- **Card Padding Horizontal**: `@dimen/card_padding_horizontal_small` (16dp) to `@dimen/card_padding_horizontal_large` (20dp)
- **Hourly Pill Spacing**: `@dimen/hourly_pill_spacing` (12dp)

---

## 🔲 Corner Radius System

### Size Categories

| Component Type | Radius | Usage | Dimen Reference |
|----------------|--------|-------|-----------------|
| **Large Container/Tab** | 28-32dp (default: 30dp) | Bottom sheets, large containers | `@dimen/corner_radius_large_container` |
| **Card/Tile** | 22-26dp (default: 24dp) | Weather cards, detail cards | `@dimen/corner_radius_card` |
| **Pill/Chip** | 18-22dp (default: 20dp) | Hourly pills, chips, search bar | `@dimen/corner_radius_pill` |
| **FAB** | 28dp | Floating action button | `@dimen/fab_corner_radius` |

### Applied Drawables
- `card_background.xml` - Uses card radius (24dp)
- `weather_detail_card_background.xml` - Uses card radius (24dp)
- `hourly_card_background.xml` - Uses pill radius (20dp)
- `bottom_dock_background.xml` - Uses large container radius (30dp)
- `search_background.xml` - Uses pill radius (20dp)

---

## 🌟 Shadow & Elevation

### Elevation Values
```xml
@dimen/elevation_surface_card    <!-- 12dp -->
@dimen/elevation_bottom_dock     <!-- 16dp -->
@dimen/elevation_fab             <!-- 8dp -->
```

### Shadow Color
- **Outer Glow**: `@color/shadow_outer_glow` (#503A2EFF)
- Applied to all major cards with radial gradient fade

### Usage
```xml
<androidx.cardview.widget.CardView
    android:elevation="@dimen/elevation_surface_card"
    android:background="@drawable/card_background" />
```

---

## 🎯 Component Styles

### Card Styles
```xml
<!-- Surface card with elevation -->
<androidx.cardview.widget.CardView
    style="@style/Widget.App.Card.Surface" />

<!-- Large container card -->
<androidx.cardview.widget.CardView
    style="@style/Widget.App.Card.LargeContainer" />
```

### FAB (Floating Action Button)
```xml
<com.google.android.material.floatingactionbutton.FloatingActionButton
    style="@style/Widget.App.FloatingActionButton"
    android:background="@drawable/fab_background" />
```
- **Size**: 56dp × 56dp
- **Shadow**: Circular shadow with purple glow effect

### Chips/Pills
```xml
<com.google.android.material.chip.Chip
    style="@style/Widget.App.Chip.Pill" />
```

### Bottom Dock
```xml
<LinearLayout
    style="@style/Widget.App.BottomDock"
    android:background="@drawable/bottom_dock_background" />
```

---

## 🎨 Color Usage

### Primary Colors
- `@color/primary_900` (#1B1247) - Deep purple background
- `@color/primary_800` (#251B6A)
- `@color/primary_700` (#33248F)

### Accent Colors
- `@color/accent_600` (#7B5CFF)
- `@color/accent_500` (#9E4CFF)
- `@color/accent_400` (#C07BFF)

### Text Colors
- `@color/text_high` (#FFFFFF) - Primary text, 100% opacity
- `@color/text_mid` (#D4D6FF) - Secondary text, 70-80% opacity
- `@color/text_low` (#A7ABF7) - Tertiary text, 55-65% opacity

### Surface/Glass
- `@color/surface_glass` (#14FFFFFF) - 8% white glass overlay
- `@color/stroke_on_glass` (#33FFFFFF) - 20% white stroke

---

## 📦 New Drawable Resources

### Background Gradients
- `gradient_background.xml` - Cosmic gradient (180°)
- `gradient_card_accent.xml` - Card accent gradient (180°)
- `gradient_glow_purple.xml` - Purple glow gradient (45°)

### Card Backgrounds
- `card_background.xml` - Main card with shadow & glow
- `weather_detail_card_background.xml` - Weather detail cards
- `bottom_dock_background.xml` - Bottom navigation dock
- `fab_background.xml` - FAB with shadow circle

### Interactive Elements
- `hourly_card_background.xml` - Hourly forecast pills
- `hourly_card_active.xml` - Active state for hourly pills
- `search_background.xml` - Search bar background

---

## 🔄 Migration Guide

### Replacing Poppins with Inter Font

1. Download Inter font family from [Google Fonts](https://fonts.google.com/specimen/Inter)
2. Add font files to `res/font/`:
   - `inter_light.ttf`
   - `inter_regular.ttf`
   - `inter_medium.ttf`
   - `inter_semibold.ttf`

3. Update `styles.xml` font references:
```xml
<item name="android:fontFamily">@font/inter_light</item>
<item name="fontFamily">@font/inter_light</item>
```

### Applying Text Styles in Layouts
```xml
<!-- Temperature Display -->
<TextView
    android:textAppearance="@style/TextAppearance.App.DisplayTemp" />

<!-- City Name -->
<TextView
    android:textAppearance="@style/TextAppearance.App.CityTitle" />

<!-- Weather Condition -->
<TextView
    android:textAppearance="@style/TextAppearance.App.Subtitle" />

<!-- Metric Value (e.g., Humidity %) -->
<TextView
    android:textAppearance="@style/TextAppearance.App.MetricValue" />

<!-- Metric Label (e.g., "Humidity") -->
<TextView
    android:textAppearance="@style/TextAppearance.App.MetricLabel" />

<!-- Small hints/captions -->
<TextView
    android:textAppearance="@style/TextAppearance.App.Caption" />
```

---

## ✅ Implementation Complete

All specifications have been implemented:
- ✅ Typography system (6 text styles)
- ✅ Spacing & grid (base 8dp unit)
- ✅ Corner radius system (3 size categories)
- ✅ Shadow & elevation (with outer glow)
- ✅ Color system integration
- ✅ All drawable resources updated
- ✅ Material3 theme integration

**Note**: Currently using Poppins font. Replace with Inter font files when available for perfect match to SF Pro.

