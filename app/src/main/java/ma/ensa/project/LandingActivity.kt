package ma.ensa.project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.card.MaterialCardView

class LandingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_landing)

        setupClickListeners()
    }

    private fun setupClickListeners() {
        findViewById<MaterialCardView>(R.id.dineInButton).setOnClickListener {
            navigateToOrderScreen(OrderType.DINE_IN)
        }

        findViewById<MaterialCardView>(R.id.takeoutButton).setOnClickListener {
            navigateToOrderScreen(OrderType.TAKEOUT)
        }

        findViewById<MaterialCardView>(R.id.deliveryButton).setOnClickListener {
            navigateToOrderScreen(OrderType.DELIVERY)
        }

        findViewById<MaterialCardView>(R.id.findUsButton).setOnClickListener {
            navigateToLocations()
        }
    }

    private fun navigateToOrderScreen(orderType: OrderType) {
        val intent = Intent(this, PizzaListActivity::class.java).apply {
            putExtra(EXTRA_ORDER_TYPE, orderType)
        }
        startActivity(intent)
    }

    private fun navigateToLocations() {
        startActivity(Intent(this, MapActivity::class.java))
    }

    enum class OrderType {
        DINE_IN, TAKEOUT, DELIVERY
    }

    companion object {
        const val EXTRA_ORDER_TYPE = "extra_order_type"
    }
}