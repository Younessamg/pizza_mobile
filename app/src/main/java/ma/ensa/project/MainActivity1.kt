package ma.ensa.project

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import ma.ensa.project.beans.Pizza
import ma.ensa.project.databinding.ActivityMainBinding
import ma.ensa.project.AnimatedPizzaView
import ma.ensa.project.databinding.ActivityMain1Binding

class MainActivity1 : AppCompatActivity() {
    private lateinit var pizza: Pizza
    private lateinit var binding: ActivityMain1Binding
    private lateinit var animatedPizzaView: AnimatedPizzaView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain1Binding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize AnimatedPizzaView
        animatedPizzaView = binding.animatedPizzaView
        setupPizzaView()

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

    private fun setupPizzaView() {
        // Load pizza base and toppings drawables
        animatedPizzaView.loadToppingDrawable("base", R.drawable.pizza_base)
        animatedPizzaView.loadToppingDrawable("pepperoni", R.drawable.topping_pepperoni)
        animatedPizzaView.loadToppingDrawable("mushrooms", R.drawable.topping_mushrooms)
        animatedPizzaView.loadToppingDrawable("onions", R.drawable.topping_onions)
        animatedPizzaView.loadToppingDrawable("sausage", R.drawable.topping_sausage)
        animatedPizzaView.loadToppingDrawable("extra_cheese", R.drawable.topping_cheese)
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

        // Size Radio Group Listener with Animation
        binding.sizeGroup.setOnCheckedChangeListener { _, checkedId ->
            pizza.size = when (checkedId) {
                R.id.smallSize -> "Petit"
                R.id.mediumSize -> "Moyen"
                R.id.largeSize -> "Grand"
                else -> "Moyen"
            }
            // Animate pizza size
            animatedPizzaView.setSize(pizza.size.lowercase())
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

        // Toppings Checkboxes Listeners with Animations
        setupToppingCheckboxes()

        // Order Button
        binding.orderButton.setOnClickListener {
            showOrderSummary()
        }

        // Mettre à jour le prix initial
        updateTotal()
    }

    private fun setupToppingCheckboxes() {
        val toppingMap = mapOf(
            binding.pepperoniCheck to "pepperoni",
            binding.mushroomsCheck to "mushrooms",
            binding.onionsCheck to "onions",
            binding.sausageCheck to "sausage",
            binding.extraCheeseCheck to "extra_cheese"
        )

        // Pré-cocher les garnitures existantes
        pizza.toppings.forEach { topping ->
            toppingMap.entries.find { it.key.text.toString() == topping }?.key?.isChecked = true
        }

        toppingMap.forEach { (checkbox, toppingId) ->
            checkbox.setOnCheckedChangeListener { _, isChecked ->
                val topping = checkbox.text.toString()
                if (isChecked) {
                    pizza.toppings.add(topping)
                } else {
                    pizza.toppings.remove(topping)
                }
                // Animate topping
                animatedPizzaView.toggleTopping(toppingId, isChecked)
                updateTotal()
            }
        }
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