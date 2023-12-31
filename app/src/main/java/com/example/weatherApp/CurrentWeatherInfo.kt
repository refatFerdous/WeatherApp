package com.example.weatherApp

import io.realm.RealmObject

open class CurrentWeatherInfo : RealmObject() {
    var icon: String = ""
    var temperatureType: String = ""
    var temperature: Double = 0.0
}