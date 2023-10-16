package com.example.weatherApp.dao

import com.example.weatherApp.apiResponse.HourlyWeatherInfoResponse
import com.example.weatherApp.apiResponse.TemperatureValueResponse
import com.example.weatherApp.apiResponse.WeatherResponse
import com.example.weatherApp.apiResponse.WindResponse
import com.example.weatherApp.realm.WeatherForecast
import io.realm.Realm

class WeatherForecastDao {
    private val realm = Realm.getDefaultInstance()
    fun getDataFromRealm(): List<HourlyWeatherInfoResponse> {
        val results = realm.where(WeatherForecast::class.java).findAll()
        val weatherInfoList = mutableListOf<HourlyWeatherInfoResponse>()
        results.forEach { item ->
            val hourlyWeather = HourlyWeatherInfoResponse(
                TemperatureValueResponse(
                    item.temperature,
                    item.minTemperature,
                    item.maxTemperature
                ),
                listOf(WeatherResponse(item.temperatureType, item.icon)),
                WindResponse(item.windSpeed),
                item.date
            )
            weatherInfoList.add(hourlyWeather)
        }
        return weatherInfoList
    }

    fun setDataToRealm(weatherResponse : List<HourlyWeatherInfoResponse>){
        realm.executeTransaction { realm ->
            realm.where(WeatherForecast::class.java).findAll().deleteAllFromRealm()

            weatherResponse?.forEach { item ->
                val realmObject = WeatherForecast()
                realmObject.date = item.dt_txt
                realmObject.time = item.dt_txt
                realmObject.icon = item.weather[0].icon
                realmObject.temperatureType = item.weather[0].main
                realmObject.maxTemperature = item.main.temp_max
                realmObject.minTemperature = item.main.temp_min
                realmObject.temperature = item.main.temp
                realmObject.windSpeed = item.wind.speed
                realm.copyToRealm(realmObject)
            }
        }
    }
    fun closeRealm(){
        realm.close()
    }

}