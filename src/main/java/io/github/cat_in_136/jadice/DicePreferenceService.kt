package io.github.cat_in_136.jadice

import java.io.File
import java.util.prefs.PreferenceChangeListener
import java.util.prefs.Preferences

object DicePreferenceService {
    private val prefs = Preferences.userNodeForPackage(DicePreferenceService::class.java)

    const val PREF_DELAY_FOR_SEARCH = "PREF_DELAY_FOR_SEARCH"
    const val PREF_NORMALIZE_SEARCH = "PREF_NORMALIZE_SEARCH"
    const val PREF_DICS = "PREF_DICS"

    var prefSearchForDelay
        get() = prefs.getInt(PREF_DELAY_FOR_SEARCH, 100)
        set(value) = prefs.putInt(PREF_DELAY_FOR_SEARCH, value)

    var prefNormalizeSearch
        get() = prefs.getBoolean(PREF_NORMALIZE_SEARCH, true)
        set(value) = prefs.putBoolean(PREF_NORMALIZE_SEARCH, value)

    var prefDics: List<String>
        get() {
            val value = prefs.get(PREF_DICS, "")
            return if (value.isNullOrEmpty()) {
                listOf()
            } else {
                value.split(File.pathSeparator)
            }
        }
        set(value) = prefs.put(PREF_DICS, value.joinToString(File.pathSeparator))

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