package io.github.hyuwah.catatanku.ui.splash

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import io.github.hyuwah.catatanku.databinding.ActivitySplashBinding
import io.github.hyuwah.catatanku.ui.notelist.NoteListActivity

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Delayed 2s to Main Activity
        val launch: Thread = object : Thread() {
            override fun run() {
                try {
                    sleep(2000)
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } finally {
                    startActivity(Intent(this@SplashActivity, NoteListActivity::class.java))
                    finish()
                }
            }
        }
        launch.start()
    }
}