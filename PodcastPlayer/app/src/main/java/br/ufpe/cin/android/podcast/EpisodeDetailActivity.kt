package br.ufpe.cin.android.podcast

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.activity_episode_detail.*

class EpisodeDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_episode_detail)
        val t1 = textView4
        val t2 = textView5
        var bundle :Bundle ?=intent.extras
        t1.setText(bundle!!.getString("description"))
        t2.setText(bundle!!.getString("link"))

    }
}
