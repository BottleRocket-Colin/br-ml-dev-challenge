package com.br.ml.brpathfinder

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.br.ml.brpathfinder.common.FragmentName
import com.br.ml.brpathfinder.ui.main.ArcoreFragment
import com.br.ml.brpathfinder.ui.main.MainFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_activity)
        val fragmentName = intent.extras
        val frag = fragmentName?.getSerializable(INTENT_FRAGMENT_NAME) as FragmentName
        when (frag) {
            FragmentName.AR_FRAGMENT -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, ArcoreFragment.newInstance())
                    .commitNow()
            }
            FragmentName.ML_FRAGMENT -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
            }
        }
    }

    companion object {

        private val INTENT_FRAGMENT_NAME = "frag_name"

        fun newIntent(context: Context, fragName: FragmentName?): Intent {
            val intent: Intent = Intent(context, MainActivity::class.java)
            intent.putExtra(INTENT_FRAGMENT_NAME, fragName)
            return intent
        }
    }

}
