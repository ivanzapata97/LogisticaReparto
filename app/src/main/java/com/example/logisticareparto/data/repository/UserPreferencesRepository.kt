package com.example.logisticareparto.data.repository

import android.content.Context
import android.content.SharedPreferences

class UserPreferencesRepository(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

    fun saveSelectedTruck(truckId: Int) {
        prefs.edit().putInt("selected_truck", truckId).apply()
    }

    fun getSelectedTruck(): Int {
        return prefs.getInt("selected_truck", 0)
    }

    fun clearSelectedTruck() {
        prefs.edit().remove("selected_truck").apply()
    }
}
