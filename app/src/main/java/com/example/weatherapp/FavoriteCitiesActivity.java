package com.example.weatherapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteCitiesActivity extends AppCompatActivity {

    private static final String API_KEY = "bd5e378503939ddaee76f12ad7a97608";
    private RecyclerView recyclerView;
    private FavoriteCitiesAdapter adapter;
    private FavoriteCitiesManager favoritesManager;
    private View tvEmptyState; // Changed from TextView to View

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_cities);

        favoritesManager = new FavoriteCitiesManager(this);

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        tvEmptyState = findViewById(R.id.tvEmptyState); // Now correctly typed as View (LinearLayout)
        FloatingActionButton fabAddCity = findViewById(R.id.fabAddCity);
        ImageButton btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadFavoriteCities();

        fabAddCity.setOnClickListener(v -> {
            if (favoritesManager.canAddMoreCities()) {
                // Open search activity to add new city
                Intent intent = new Intent(this, SearchActivity.class);
                intent.putExtra("ADD_TO_FAVORITES", true);
                startActivity(intent);
            } else {
                Toast.makeText(this, "Maximum 10 favorite cities allowed", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadFavoriteCities();
    }

    private void loadFavoriteCities() {
        List<FavoriteCity> cities = favoritesManager.getFavoriteCities();

        if (cities.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            tvEmptyState.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            tvEmptyState.setVisibility(View.GONE);

            adapter = new FavoriteCitiesAdapter(cities);
            recyclerView.setAdapter(adapter);

            // Refresh weather data for all cities
            refreshAllCitiesWeather();
        }
    }

    private void refreshAllCitiesWeather() {
        List<FavoriteCity> cities = favoritesManager.getFavoriteCities();
        WeatherApiService apiService = RetrofitClient.getInstance().getWeatherApi();

        for (int i = 0; i < cities.size(); i++) {
            FavoriteCity city = cities.get(i);
            final int position = i;

            Call<WeatherResponse> call = apiService.getWeatherByCity(
                    city.getCityName(), API_KEY, "metric");

            call.enqueue(new Callback<WeatherResponse>() {
                @Override
                public void onResponse(Call<WeatherResponse> call, Response<WeatherResponse> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        WeatherResponse weather = response.body();
                        city.setCurrentTemp(weather.getMain().getTemp());
                        city.setWeatherCondition(weather.getWeather().get(0).getMain());
                        city.setWeatherDescription(weather.getWeather().get(0).getDescription());

                        favoritesManager.updateCityWeather(
                                city.getCityName(),
                                city.getCurrentTemp(),
                                city.getWeatherCondition(),
                                city.getWeatherDescription()
                        );

                        if (adapter != null) {
                            adapter.notifyItemChanged(position);
                        }
                    }
                }

                @Override
                public void onFailure(Call<WeatherResponse> call, Throwable t) {
                    // Silently fail - keep cached data
                }
            });
        }
    }

    private class FavoriteCitiesAdapter extends RecyclerView.Adapter<FavoriteCitiesAdapter.ViewHolder> {

        private final List<FavoriteCity> cities;

        FavoriteCitiesAdapter(List<FavoriteCity> cities) {
            this.cities = cities;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_favorite_city, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FavoriteCity city = cities.get(position);
            holder.bind(city);
        }

        @Override
        public int getItemCount() {
            return cities.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCityName, tvTemperature, tvWeatherDesc;
            ImageButton btnRemove;

            ViewHolder(View itemView) {
                super(itemView);
                tvCityName = itemView.findViewById(R.id.tvCityName);
                tvTemperature = itemView.findViewById(R.id.tvTemperature);
                tvWeatherDesc = itemView.findViewById(R.id.tvWeatherDesc);
                btnRemove = itemView.findViewById(R.id.btnRemove);
            }

            void bind(FavoriteCity city) {
                tvCityName.setText(city.getDisplayName());

                if (city.getCurrentTemp() != 0) {
                    tvTemperature.setText(String.format(Locale.getDefault(),
                            "%.0f°C", city.getCurrentTemp()));
                    tvWeatherDesc.setText(city.getWeatherDescription());
                } else {
                    tvTemperature.setText("--°");
                    tvWeatherDesc.setText("Loading...");
                }

                itemView.setOnClickListener(v -> {
                    // Open MainActivity with this city
                    Intent intent = new Intent(FavoriteCitiesActivity.this, MainActivity.class);
                    intent.putExtra("CITY_NAME", city.getCityName());
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                });

                btnRemove.setOnClickListener(v -> {
                    new AlertDialog.Builder(FavoriteCitiesActivity.this)
                            .setTitle("Remove City")
                            .setMessage("Remove " + city.getCityName() + " from favorites?")
                            .setPositiveButton("Remove", (dialog, which) -> {
                                int position = getBindingAdapterPosition();
                                if (position != RecyclerView.NO_POSITION) {
                                    favoritesManager.removeFavoriteCity(city.getCityName());
                                    cities.remove(position);
                                    notifyItemRemoved(position);

                                    if (cities.isEmpty()) {
                                        recyclerView.setVisibility(View.GONE);
                                        tvEmptyState.setVisibility(View.VISIBLE);
                                    }
                                }
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }
        }
    }
}
