package com.br.ml.brpathfinder

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.br.ml.brpathfinder.ui.main.ArcoreFragment
import com.br.ml.brpathfinder.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.container, ArcoreFragment.newInstance())
                .commitNow()
        }
    }

}
