package ma.ensa.project


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import ma.ensa.project.databinding.ActivitySplashBinding
import kotlinx.coroutines.*

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Charger les animations
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        val slideUp = AnimationUtils.loadAnimation(this, R.anim.slide_up)
        val slideInRight = AnimationUtils.loadAnimation(this, R.anim.slide_in_right)

        // Appliquer les animations
        binding.ivLogo.startAnimation(fadeIn)
        binding.tvAppName.startAnimation(slideInRight)
        binding.tvTagline.startAnimation(slideUp)

        // Lancer l'activité principale après un délai
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000) // 3 secondes
            startActivity(Intent(this@SplashActivity, PizzaListActivity::class.java))
            finish()
            overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        }
    }
}