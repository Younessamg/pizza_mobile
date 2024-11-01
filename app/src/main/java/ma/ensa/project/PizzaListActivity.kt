package ma.ensa.project

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import ma.ensa.project.adapter.PizzaAdapter
import ma.ensa.project.beans.Pizza
import ma.ensa.project.databinding.ActivityPizzaListBinding

class PizzaListActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPizzaListBinding
    private lateinit var adapter: PizzaAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPizzaListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        setupRecyclerView()
        setupSearchView()
    }

    private fun setupRecyclerView() {
        val pizzas = createPizzaList()
        adapter = PizzaAdapter(pizzas) { pizza ->
            navigateToMainActivity(pizza)
        }

        binding.pizzaRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@PizzaListActivity)
            adapter = this@PizzaListActivity.adapter
        }
    }

    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { adapter.filter(it) }
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let { adapter.filter(it) }
                return true
            }
        })
    }

    private fun createPizzaList(): List<Pizza> {
        return listOf(
            Pizza(
                id = 0,
                name = "Pizza Vide",
                description = "sauce tomate",
                imageResource = R.drawable.pizza_base,
                toppings = mutableListOf(),
                basePrice = 10.0
            ),
            Pizza(
                id = 1,
                name = "Pizza Margherita",
                description = "mozzarella et sauce tomate",
                imageResource = R.drawable.pizza_margherita,
                toppings = mutableListOf("Mozzarella", "Basil"),
                basePrice = 20.0
            ),
            Pizza(
                id = 2,
                name = "Pizza Viande Hachée",
                description = "Fromage, viande d'hachée, olives noires ",
                imageResource = R.drawable.pizza_viande,
                toppings = mutableListOf("Ground Beef", "Onions", "Bell Peppers"),
                basePrice = 30.0
            ),
            Pizza(
                id = 3,
                name = "Pizza Thon",
                description = "Tuna avec onion et olives noir",
                imageResource = R.drawable.pizza_thon,
                toppings = mutableListOf(),
                basePrice = 25.0
            ),
            Pizza(
                id = 4,
                name = "Pizza Quatre Fromages",
                description = "le mixe de quatre fromages différents",
                imageResource = R.drawable.pizza_fromage,
                toppings = mutableListOf(),
                basePrice = 30.0
            ),
            Pizza(
                id = 5,
                name = "Pizza Végétarienne",
                description = "Frais légumes et mushrooms",
                imageResource = R.drawable.pizza_vegetarienne,
                toppings = mutableListOf(),
                basePrice = 25.0
            ),
            Pizza(
                id = 6,
                name = "Pizza 4 saisons",
                description = "dinde, viande hachée, crevette, thon",
                imageResource = R.drawable.pizza_vegetarienne,
                toppings = mutableListOf(),
                basePrice = 30.0
            )
        )
    }

    private fun navigateToMainActivity(pizza: Pizza) {
        val intent = if (pizza.id == 0) {
            Intent(this, MainActivity1::class.java)
        } else {
            Intent(this, MainActivity::class.java)
        }

        intent.apply {
            putExtra("PIZZA_ID", pizza.id)
            putExtra("PIZZA_NAME", pizza.name)
            putExtra("PIZZA_BASE_PRICE", pizza.basePrice)
            putExtra("PIZZA_IMAGE", pizza.imageResource)
            putStringArrayListExtra("PIZZA_TOPPINGS", ArrayList(pizza.toppings))
        }

        startActivity(intent)
    }

}
