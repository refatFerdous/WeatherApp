package com.example.weatherApp.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.app.ActivityCompat
import java.util.Locale
import android.location.LocationListener
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.weatherApp.adapter.MainActivityAdapter
import com.example.weatherApp.viewmodels.WeatherInfoViewModel
import com.example.getlocation.databinding.WeatherInfoBinding
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class WeatherInfoActivity : AppCompatActivity(), LocationListener {

    private lateinit var binding: WeatherInfoBinding
    private val locationPermissionCode = 111
    private lateinit var recyclerviewAdapter: MainActivityAdapter

    private lateinit var locationManager: LocationManager
    private lateinit var viewModel: WeatherInfoViewModel


    private var cityName = ""
    private var latitude = 0.0
    private var longitude = 0.0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = WeatherInfoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getLocation()

        binding.weatherDataRecyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        recyclerviewAdapter = MainActivityAdapter()

        viewModel = ViewModelProvider(this).get(WeatherInfoViewModel::class.java)

        viewModel.weatherInfoLiveData.observe(this, Observer {
            binding.weatherTypeTextView.text = it.main
            binding.temperatureTextView.text = it.temperature.toString() + "ºC"
            Glide.with(this).load(it.icon).into(binding.weatherImageView)
        })


        binding.weatherDataRecyclerView.adapter = recyclerviewAdapter
        viewModel.listOfWeatherInfoLiveData.observe(this, Observer { list ->
            recyclerviewAdapter.initTemperature(list)
        })
        binding.weatherDataRecyclerView.adapter = recyclerviewAdapter

        binding.searchCityButton.setOnClickListener {
            var intent = Intent(this, SearchLocationActivity::class.java)
            startActivityForResult(intent, 300)
        }

        binding.forecastButton.setOnClickListener {
            var intent = Intent(this, WeatherDataForecastActivity::class.java)
            intent.putExtra("latitude1", latitude)
            intent.putExtra("longitude1", longitude)
            intent.putExtra("getCityName", cityName)
            startActivity(intent)
        }

        requestLocation()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 300 && data != null) {
            getValuesFromSecondActivity(data)
        }
    }

    override fun onLocationChanged(location: Location) {
        latitude = location.latitude
        longitude = location.longitude
        val city = getCityName(location.latitude, location.longitude)
        binding.cityNameTextView.text = city
        cityName = city

        getApiDataFromViewModel()

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == locationPermissionCode) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                requestLocation()
            }
        }
    }

    private fun requestLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 400, 1f, this)
    }

    private fun getLocation() {
        locationManager = applicationContext.getSystemService(LOCATION_SERVICE) as LocationManager
        if ((ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED)
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode
            )
        }
    }

    private fun getValuesFromSecondActivity(intent: Intent) {
        var lat = intent.getDoubleExtra("lat", 0.0)
        var long = intent.getDoubleExtra("long", 0.0)
        latitude = lat
        longitude = long

        getApiDataFromViewModel()
        val city = intent.getStringExtra("cityName")
        binding.cityNameTextView.text = city
        cityName = city.toString()
    }

    private fun getApiDataFromViewModel() {
        GlobalScope.launch {
            viewModel.fetchWeatherInfoHourly(latitude, longitude)
            viewModel.fetchWeatherInfo(latitude, longitude)
        }
    }

    private fun getCityName(lat: Double, long: Double): String {

        var city: String = ""
        var geoCoder = Geocoder(this, Locale.getDefault())
        var addresses = geoCoder.getFromLocation(lat, long, 3)
        if (addresses.isNullOrEmpty()) {
            return ""
        }
        var address = addresses.get(0)
        city = address.locality.toString()
        return city
    }

}