package ma.ensa.project.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ma.ensa.project.beans.Pizza
import ma.ensa.project.databinding.ItemPizzaBinding

class PizzaAdapter(
    private val pizzas: List<Pizza>,
    private val onPizzaClicked: (Pizza) -> Unit
) : RecyclerView.Adapter<PizzaAdapter.PizzaViewHolder>() {

    private val filteredPizzas = pizzas.toMutableList()

    inner class PizzaViewHolder(private val binding: ItemPizzaBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(pizza: Pizza) {
            binding.pizzaName.text = pizza.name
            binding.pizzaDescription.text = pizza.description
            binding.pizzaPrice.text = "${String.format("%.2f", pizza.basePrice)}DH"
            binding.pizzaImage.setImageResource(pizza.imageResource)

            binding.root.setOnClickListener {
                onPizzaClicked(pizza)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PizzaViewHolder {
        val binding = ItemPizzaBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PizzaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PizzaViewHolder, position: Int) {
        holder.bind(filteredPizzas[position])
    }

    override fun getItemCount() = filteredPizzas.size

    fun filter(query: String) {
        filteredPizzas.clear()
        if (query.isEmpty()) {
            filteredPizzas.addAll(pizzas)
        } else {
            val lowerCaseQuery = query.toLowerCase()
            filteredPizzas.addAll(pizzas.filter {
                it.name.toLowerCase().contains(lowerCaseQuery)
            })
        }
        notifyDataSetChanged()
    }
}
