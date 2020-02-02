package io.github.cat_in_136.jadice

import java.util.prefs.PreferenceChangeListener
import java.util.prefs.Preferences

object DicePreferenceService {
    private val prefs = Preferences.userNodeForPackage(DicePreferenceService::class.java)

    const val PREF_DELAY_FOR_SEARCH = "PREF_DELAY_FOR_SEARCH"

    var prefSearchForDelay
        get() = prefs.getInt(PREF_DELAY_FOR_SEARCH, 100)
        set(value) = prefs.putInt(PREF_DELAY_FOR_SEARCH, value)

    fun addPreferenceChangeListener(listener: PreferenceChangeListener) {
        return prefs.addPreferenceChangeListener(listener)
    }

    fun removePreferenceChangeListener(listener: PreferenceChangeListener) {
        prefs.removePreferenceChangeListener(listener)
    }

    fun flush() {
        prefs.flush()
    }
}