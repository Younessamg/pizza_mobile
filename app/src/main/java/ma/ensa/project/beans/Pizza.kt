package ma.ensa.project.beans


data class Pizza(
    val id: Int = 0,
    val name: String = "Personnaliser votre Pizza",
    val description: String = "",
    val imageResource: Int = 0,
    var size: String = "Moyen",
    var crust: String = "Normal",
    val toppings: MutableList<String> = mutableListOf(),
    var basePrice: Double = 10.0
) {
    fun calculateTotal(): Double {
        // Supplément selon la taille
        val sizePrice = when (size) {
            "Petit" -> 0.0
            "Moyen" -> 3.0
            "Grand" -> 6.0
            else -> 0.0
        }

        // Supplément selon le type de croûte
        val crustPrice = when (crust) {
            "Fine" -> 0.0
            "Normal" -> 0.0
            "Epais" -> 1.0
            "Farcie" -> 2.0
            else -> 0.0
        }

        // Supplément pour chaque garniture (1.5€ par garniture)
        val toppingsPrice = toppings.size * 3.0

        // Calcul du prix total
        return basePrice + sizePrice + crustPrice + toppingsPrice
    }

    // Méthode pour ajouter une garniture
    fun addTopping(topping: String) {
        if (!toppings.contains(topping)) {
            toppings.add(topping)
        }
    }

    // Méthode pour retirer une garniture
    fun removeTopping(topping: String) {
        toppings.remove(topping)
    }

    // Méthode pour vérifier si une garniture est présente
    fun hasTopping(topping: String): Boolean {
        return toppings.contains(topping)
    }

    // Méthode pour obtenir la liste des garnitures sous forme de texte
    fun getToppingsText(): String {
        return if (toppings.isEmpty()) {
            "Aucune garniture"
        } else {
            toppings.joinToString(", ")
        }
    }

    // Méthode pour réinitialiser la pizza aux valeurs par défaut
    fun reset() {
        size = "Moyen"
        crust = "Normal"
        toppings.clear()
    }

    companion object {
        // Liste des tailles disponibles
        val SIZES = listOf("Petit", "Moyen", "Grand")

        // Liste des types de croûte disponibles
        val CRUST_TYPES = listOf("Fine", "Normal", "Epais", "Farcie")

        // Liste des garnitures disponibles
        val AVAILABLE_TOPPINGS = listOf(
            "Pepperoni",
            "Mushrooms",
            "Onions",
            "Sausage",
            "Extra Cheese",
            "Green Peppers",
            "Black Olives",
            "Ground Beef",
            "Chicken",
            "Tomatoes",
            "Spinach",
            "Jalapeños"
        )
    }
}