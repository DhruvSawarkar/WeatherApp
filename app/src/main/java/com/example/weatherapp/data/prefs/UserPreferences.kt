package com.example.weatherapp.data.prefs

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.weatherapp.domain.model.TempUnit
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "weather_prefs")

/** Persists lightweight user settings (unit choice, whether GPS has been granted before). */
@Singleton
class UserPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val UNIT = stringPreferencesKey("temp_unit")
        val ONBOARDED = booleanPreferencesKey("onboarded")
    }

    val tempUnit: Flow<TempUnit> = context.dataStore.data.map { prefs ->
        when (prefs[Keys.UNIT]) {
            TempUnit.FAHRENHEIT.name -> TempUnit.FAHRENHEIT
            else -> TempUnit.CELSIUS
        }
    }

    suspend fun setTempUnit(unit: TempUnit) {
        context.dataStore.edit { it[Keys.UNIT] = unit.name }
    }

    val hasOnboarded: Flow<Boolean> = context.dataStore.data.map { it[Keys.ONBOARDED] ?: false }

    suspend fun setOnboarded() {
        context.dataStore.edit { it[Keys.ONBOARDED] = true }
    }
}
