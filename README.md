# Weather App 🌤️

A beautiful iOS-style weather application for Android with glassmorphism design, real-time weather data, and AI-powered outfit suggestions.

## ✨ Features

### Core Features
- 🌡️ **Real-time Weather Data** - Current temperature, conditions, and forecasts
- 📍 **Location-based Weather** - Auto-detect location or search any city
- 📊 **Detailed Weather Metrics** - UV Index, humidity, wind speed, pressure, visibility, and more
- 🕐 **Hourly & Weekly Forecasts** - Comprehensive weather predictions
- 🌅 **Sunrise & Sunset Times** - Beautiful sunrise/sunset visualization
- 💨 **Air Quality Index** - Monitor air quality in your area

### Premium Features
- 👔 **AI Outfit Suggestions** - Smart clothing recommendations based on weather
- 📈 **Weather Charts** - Visual representation of weather trends
- 🎨 **Dynamic Backgrounds** - Weather-responsive background images
- ⚙️ **Customizable Settings** - Personalize your weather experience

## 🎨 Design

### UI/UX Highlights
- **iOS-style Interface** - Clean, modern, and intuitive design
- **Glassmorphism Effects** - Frosted glass UI elements with backdrop blur
- **Premium Typography** - Carefully crafted text hierarchy
- **Smooth Animations** - Fluid transitions and interactions
- **Dark Overlay Design** - Enhanced readability with elegant overlays

### Design System
- Custom glassmorphic cards
- Gradient backgrounds
- Icon-based weather indicators
- Responsive layout for all screen sizes

## 🛠️ Technologies

- **Language**: Java
- **Platform**: Android (API 24+)
- **Architecture**: MVVM-ready structure
- **UI Framework**: Native Android XML layouts
- **Weather API**: OpenWeatherMap API
- **Location Services**: Android Location API
- **Data Binding**: AndroidX libraries

## 📱 Screenshots

The app features:
- Glassmorphic top bar with city name and quick actions
- Large temperature display with weather description
- Segmented control for hourly/weekly forecasts
- Interactive weather detail cards
- Premium outfit suggestion feature
- Comprehensive weather charts

## 🚀 Getting Started

### Prerequisites
- Android Studio Arctic Fox or later
- Android SDK 24+
- OpenWeatherMap API key

### Installation

1. Clone the repository:
```bash
git clone https://github.com/nichikei/weather-app.git
```

2. Open the project in Android Studio

3. Add your OpenWeatherMap API key:
   - Get a free API key from [OpenWeatherMap](https://openweathermap.org/api)
   - Add it to your project (check the code for API key location)

4. Build and run the app on your device or emulator

## 📦 Project Structure

```
WeatherApp/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/yourpackage/weatherapp/
│   │   │   │   ├── MainActivity.java
│   │   │   │   ├── ChartsActivity.java
│   │   │   │   ├── OutfitSuggestionActivity.java
│   │   │   │   ├── SettingsActivity.java
│   │   │   │   └── ... (other activities and utilities)
│   │   │   └── res/
│   │   │       ├── layout/
│   │   │       │   ├── activity_main.xml
│   │   │       │   ├── card_*.xml (weather cards)
│   │   │       │   └── ... (other layouts)
│   │   │       ├── drawable/
│   │   │       ├── values/
│   │   │       └── font/
│   │   └── androidTest/
│   └── build.gradle
└── build.gradle
```

## 🎯 Key Components

### Main Screen
- Dynamic weather background
- Glassmorphic top bar with search and settings
- Large temperature display
- Hourly/Weekly forecast toggle
- Weather detail cards (UV, Wind, Humidity, etc.)

### Weather Cards
- **Air Quality Index** - AQI monitoring
- **UV Index** - Sun exposure tracking
- **Sunrise/Sunset** - Solar times
- **Wind** - Speed and direction
- **Rainfall** - Precipitation probability
- **Feels Like** - Apparent temperature
- **Humidity** - Moisture levels
- **Visibility** - Viewing distance
- **Pressure** - Atmospheric pressure

### Additional Screens
- **Charts Activity** - Weather trend visualization
- **Outfit Suggestion** - AI-powered clothing recommendations
- **Settings** - App customization options

## 🌐 API Integration

The app uses OpenWeatherMap API for weather data:
- Current Weather Data API
- 5 Day / 3 Hour Forecast API
- One Call API (for comprehensive data)
- Air Pollution API

## 📄 License

This project is open source and available under the [MIT License](LICENSE).

## 👨‍💻 Developer

Created with ❤️ by [nichikei](https://github.com/nichikei)

## 🤝 Contributing

Contributions, issues, and feature requests are welcome!

1. Fork the project
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📞 Contact

GitHub: [@nichikei](https://github.com/nichikei)

Project Link: [https://github.com/nichikei/weather-app](https://github.com/nichikei/weather-app)

## 🙏 Acknowledgments

- OpenWeatherMap for weather data API
- Material Design Icons
- Android community for various libraries and tools
- iOS Weather app for design inspiration

---

⭐ Star this repo if you find it helpful!

