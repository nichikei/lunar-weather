package com.example.weatherapp.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.weatherapp.R;
import com.example.weatherapp.data.api.RetrofitClient;
import com.example.weatherapp.data.api.WeatherApiService;
import com.example.weatherapp.data.models.FavoriteCity;
import com.example.weatherapp.data.responses.WeatherResponse;
import com.example.weatherapp.domain.repository.FavoriteCitiesManager;
import com.example.weatherapp.presentation.viewmodel.FavoriteCitiesViewModel;
import com.example.weatherapp.ui.helpers.RecyclerViewScrollAnimator;
import com.example.weatherapp.ui.helpers.SlideInItemAnimator;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FavoriteCitiesActivity extends AppCompatActivity {

    // ViewModel manages favorite cities state
    private FavoriteCitiesViewModel viewModel;
    
    private RecyclerView recyclerView;
    private FavoriteCitiesAdapter adapter;
    private View tvEmptyState;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite_cities);

        // === INITIALIZE VIEWMODEL ===
        viewModel = new ViewModelProvider(this).get(FavoriteCitiesViewModel.class);
        viewModel.init(this);

        recyclerView = findViewById(R.id.recyclerViewFavorites);
        tvEmptyState = findViewById(R.id.tvEmptyState);
        progressBar = findViewById(R.id.progressBar);
        FloatingActionButton fabAddCity = findViewById(R.id.fabAddCity);
        ImageButton btnBack = findViewById(R.id.btnBack);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        
        // Add smooth slide-in animation
        recyclerView.setItemAnimator(new SlideInItemAnimator());
        RecyclerViewScrollAnimator scrollAnimator = new RecyclerViewScrollAnimator();
        recyclerView.addOnScrollListener(scrollAnimator);

        // Setup adapter
        adapter = new FavoriteCitiesAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // === SETUP OBSERVERS ===
        setupObservers();

        // Load favorite cities
        viewModel.loadFavoriteCities();

        fabAddCity.setOnClickListener(v -> {
            Boolean canAdd = viewModel.getCanAddMoreCities().getValue();
            if (canAdd != null && canAdd) {
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
        viewModel.loadFavoriteCities();
    }

    /**
     * SETUP LIVEDATA OBSERVERS
     * Observes ViewModel state changes and updates UI
     */
    private void setupObservers() {
        // Observe favorite cities state (Loading/Success/Error)
        viewModel.getFavoriteCitiesState().observe(this, uiState -> {
            if (uiState.isLoading()) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.VISIBLE);
                }
                recyclerView.setVisibility(View.GONE);
                tvEmptyState.setVisibility(View.GONE);
            } else if (uiState.isSuccess()) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                
                List<FavoriteCitiesViewModel.FavoriteCityWithWeather> citiesWithWeather = uiState.getData();
                
                if (citiesWithWeather == null || citiesWithWeather.isEmpty()) {
                    recyclerView.setVisibility(View.GONE);
                    tvEmptyState.setVisibility(View.VISIBLE);
                } else {
                    recyclerView.setVisibility(View.VISIBLE);
                    tvEmptyState.setVisibility(View.GONE);
                    
                    // Convert to FavoriteCity list for adapter
                    List<FavoriteCity> cities = new ArrayList<>();
                    for (FavoriteCitiesViewModel.FavoriteCityWithWeather item : citiesWithWeather) {
                        cities.add(item.city);
                    }
                    adapter.updateCities(cities);
                }
            } else if (uiState.isError()) {
                if (progressBar != null) {
                    progressBar.setVisibility(View.GONE);
                }
                recyclerView.setVisibility(View.VISIBLE);
                Toast.makeText(this, "Error: " + uiState.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }



    private class FavoriteCitiesAdapter extends RecyclerView.Adapter<FavoriteCitiesAdapter.ViewHolder> {

        private final List<FavoriteCity> cities;

        FavoriteCitiesAdapter(List<FavoriteCity> cities) {
            this.cities = cities;
        }

        /**
         * Update cities list and refresh adapter
         */
        public void updateCities(List<FavoriteCity> newCities) {
            cities.clear();
            cities.addAll(newCities);
            notifyDataSetChanged();
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
                                // Use ViewModel to remove city
                                viewModel.removeFavoriteCity(city);
                            })
                            .setNegativeButton("Cancel", null)
                            .show();
                });
            }
        }
    }
}
