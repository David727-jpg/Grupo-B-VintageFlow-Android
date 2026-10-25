package com.vintageflow.app.ui.catalog

import com.vintageflow.app.data.model.Prenda

// Datos falsos SOLO para previews y pruebas visuales.
// Cuando exista el ViewModel real, las pantallas reciben su lista y esto queda solo para @Preview.
val prendasDePrueba = listOf(
    Prenda("VINT-001", "Chaqueta Retro", "L", "Chaquetas", 45.00),
    Prenda("VINT-002", "Jeans Alta Cintura", "M", "Pantalones", 32.50, "RESERVADO"),
    Prenda("VINT-003", "Camisa Floral 90s", "S", "Camisas", 18.00, "VENDIDO"),
    Prenda("VINT-004", "Vestido Lunares", "M", "Vestidos", 38.75)
)
