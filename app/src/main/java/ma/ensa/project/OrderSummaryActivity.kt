package ma.ensa.project

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import ma.ensa.project.beans.Pizza
import ma.ensa.project.databinding.ActivityOrderSummaryBinding

class OrderSummaryActivity : AppCompatActivity() {
    private lateinit var pizza: Pizza
    private lateinit var binding: ActivityOrderSummaryBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderSummaryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupérer les données de la commande
        val pizzaId = intent.getIntExtra("PIZZA_ID", 0)
        val pizzaName = intent.getStringExtra("PIZZA_NAME") ?: "Custom Pizza"
        val pizzaBasePrice = intent.getDoubleExtra("PIZZA_BASE_PRICE", 10.0)
        val pizzaImageResource = intent.getIntExtra("PIZZA_IMAGE", R.drawable.pizza_header)
        val pizzaToppings = intent.getStringArrayListExtra("PIZZA_TOPPINGS") ?: ArrayList()

        pizza = Pizza(
            id = pizzaId,
            name = pizzaName,
            basePrice = pizzaBasePrice,
            imageResource = pizzaImageResource,
            toppings = pizzaToppings.toMutableList()
        )

        // Mettre à jour l'interface utilisateur avec les détails de la pizza
        setupOrderSummaryUI()
        binding.pizzaImage.setImageResource(pizzaImageResource)

    }

    private fun setupOrderSummaryUI() {

        binding.pizzaNameText.text = pizza.name
        binding.pizzaSize.text = "Taille : ${pizza.size}"
        binding.pizzaCrust.text = "Croûte : ${pizza.crust}"
        binding.pizzaToppings.text = "Garnitures : ${pizza.getToppingsText()}"
        binding.pizzaTotal.text = "Total : ${String.format("%.2f", pizza.calculateTotal())} DH"

        // Bouton de confirmation de commande
        binding.confirmButton.setOnClickListener {
            Toast.makeText(this, "Commande passée avec succès !", Toast.LENGTH_LONG).show()

            // Démarre l'activité PizzaListActivity
            val intent = Intent(this, PizzaListActivity::class.java)
            startActivity(intent)

            // Optionnel : Si vous souhaitez fermer l'activité actuelle et empêcher le retour à cette page
            finish()
        }

        // Bouton pour revenir en arrière
        binding.cancelButton.setOnClickListener {
            finish()
        }
    }
}
