package ma.ensa.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ma.ensa.project.beans.Pizza
import ma.ensa.project.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var pizza: Pizza
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Récupérer les données de l'intent
        val pizzaId = intent.getIntExtra("PIZZA_ID", 0)
        val pizzaName = intent.getStringExtra("PIZZA_NAME") ?: "Custom Pizza"
        val pizzaBasePrice = intent.getDoubleExtra("PIZZA_BASE_PRICE", 10.0)
        val pizzaImageResource = intent.getIntExtra("PIZZA_IMAGE", R.drawable.pizza_header)
        val pizzaToppings = intent.getStringArrayListExtra("PIZZA_TOPPINGS") ?: ArrayList()

        // Initialiser la pizza avec les données reçues
        pizza = Pizza(
            id = pizzaId,
            name = pizzaName,
            basePrice = pizzaBasePrice,
            imageResource = pizzaImageResource,
            toppings = pizzaToppings.toMutableList()
        )

        // Mettre à jour l'interface utilisateur
        setupUI()

        // Afficher l'image de la pizza
        binding.pizzaImage.setImageResource(pizzaImageResource)
        binding.pizzaNameText.text = pizzaName
    }

    private fun setupUI() {
        // Setup Crust Spinner
        ArrayAdapter.createFromResource(
            this,
            R.array.crust_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.crustSpinner.adapter = adapter
        }

        // Size Radio Group Listener
        binding.sizeGroup.setOnCheckedChangeListener { _, checkedId ->
            pizza.size = when (checkedId) {
                R.id.smallSize -> "Petit"
                R.id.mediumSize -> "Moyen"
                R.id.largeSize -> "Grand"
                else -> "Moyen"
            }
            updateTotal()
        }

        // Crust Spinner Listener
        binding.crustSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, pos: Int, id: Long) {
                pizza.crust = parent.getItemAtPosition(pos).toString()
                updateTotal()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        // Toppings Checkboxes Listeners
        val checkBoxes = listOf(
            binding.pepperoniCheck,
            binding.mushroomsCheck,
            binding.onionsCheck,
            binding.sausageCheck,
            binding.extraCheeseCheck,
        )

        // Pré-cocher les garnitures existantes
        pizza.toppings.forEach { topping ->
            checkBoxes.find { it.text.toString() == topping }?.isChecked = true
        }

        checkBoxes.forEach { checkbox ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                val topping = checkbox.text.toString()
                if (isChecked) {
                    pizza.toppings.add(topping)
                } else {
                    pizza.toppings.remove(topping)
                }
                updateTotal()
            }
        }

        // Order Button
        binding.orderButton.setOnClickListener {
            showOrderSummary()
        }

        // Mettre à jour le prix initial
        updateTotal()
    }

    private fun updateTotal() {
        binding.totalPrice.text = "Total: ${String.format("%.2f", pizza.calculateTotal())} DH"
    }

    private fun showOrderSummary() {
        val intent = Intent(this, OrderSummaryActivity::class.java).apply {
            putExtra("PIZZA_ID", pizza.id)
            putExtra("PIZZA_NAME", pizza.name)
            putExtra("PIZZA_BASE_PRICE", pizza.basePrice)
            putExtra("PIZZA_IMAGE", pizza.imageResource)
            putStringArrayListExtra("PIZZA_TOPPINGS", ArrayList(pizza.toppings))
        }
        startActivity(intent)
    }

}