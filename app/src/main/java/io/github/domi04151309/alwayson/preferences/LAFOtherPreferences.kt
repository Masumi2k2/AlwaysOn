package io.github.domi04151309.alwayson.preferences

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import io.github.domi04151309.alwayson.R
import io.github.domi04151309.alwayson.objects.Theme

class LAFOtherPreferences : AppCompatActivity(),
        PreferenceFragmentCompat.OnPreferenceStartFragmentCallback {

    override fun onCreate(savedInstanceState: Bundle?) {
        Theme.set(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, PreferenceFragment())
                .commit()
    }

    override fun onPreferenceStartFragment(caller: PreferenceFragmentCompat, pref: Preference): Boolean {
        val fragment = supportFragmentManager.fragmentFactory.instantiate(
                classLoader,
                pref.fragment)
        fragment.arguments = pref.extras
        fragment.setTargetFragment(caller, 0)
        supportFragmentManager.beginTransaction()
                .replace(R.id.settings, fragment)
                .addToBackStack(null)
                .commit()
        return true
    }

    class PreferenceFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            addPreferencesFromResource(R.xml.pref_laf_other)
            findPreference<Preference>("dark_mode")?.setOnPreferenceClickListener {
                startActivity(Intent(context, Preferences::class.java))
                true
            }
            findPreference<Preference>("charging_style")?.setOnPreferenceClickListener {
                startActivity(Intent(context, ChargingLookActivity::class.java))
                true
            }
        }
    }
}
