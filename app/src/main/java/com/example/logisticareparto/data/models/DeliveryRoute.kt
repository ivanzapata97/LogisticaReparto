package com.example.logisticareparto.data.models

data class RouteStop(
    val clientId: String = "",
    val clientName: String = "",
    val address: String = "",
    val order: Int = 0,
    val status: String = "pending"
) {
    val isVisited: Boolean
        get() = status == "visited"

    fun toClient(allClients: List<Client>): Client {
        return allClients.find { it.id == clientId }
            ?: Client(
                id = clientId,
                cliente = clientName,
                direccion = address
            )
    }
}

data class DeliveryRoute(
    val id: String = "",
    val truckId: Int = 0,
    val date: String = "",
    val status: String = "",
    val stops: List<RouteStop> = emptyList()
) {
    fun sortedStops(): List<RouteStop> = stops.sortedBy { it.order }
}
