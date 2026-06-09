package com.example.logisticareparto.data.repository

import com.example.logisticareparto.data.models.Client
import com.example.logisticareparto.data.models.DeliveryRoute
import com.example.logisticareparto.data.models.RouteStats
import com.example.logisticareparto.data.models.RouteStop
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import java.time.LocalDate

class RouteRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) {

    suspend fun startRoute(truckId: Int, clients: List<Client>): Result<String> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(IllegalStateException("Debe iniciar sesion antes de iniciar la ruta"))

            if (truckId <= 0) {
                return Result.failure(IllegalArgumentException("Selecciona un camion antes de iniciar la ruta"))
            }

            if (clients.isEmpty()) {
                return Result.failure(IllegalArgumentException("Agrega al menos una parada a la ruta"))
            }

            val routeId = getTodayRouteId(truckId, currentUser.uid)
            val timestamp = Timestamp.now()

            val routeData = mapOf(
                "driverId" to currentUser.uid,
                "truckId" to truckId,
                "date" to LocalDate.now().toString(),
                "status" to "started",
                "createdAt" to timestamp,
                "startedAt" to timestamp,
                "finishedAt" to null,
                "notificationDispatched" to false,
                "stops" to clients.mapIndexed { index, client ->
                    mapOf(
                        "clientId" to client.id,
                        "clientName" to client.cliente,
                        "address" to client.direccion,
                        "order" to index + 1,
                        "status" to "pending"
                    )
                }
            )

            db.collection("routes").document(routeId).set(routeData).await()
            Result.success(routeId)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getActiveRouteForToday(truckId: Int): Result<DeliveryRoute?> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(IllegalStateException("Debe iniciar sesion antes de continuar"))

            if (truckId <= 0) {
                return Result.success(null)
            }

            val snapshot = db.collection("routes")
                .document(getTodayRouteId(truckId, currentUser.uid))
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.success(null)
            }

            val route = snapshot.toDeliveryRoute()
            if (route == null || route.status != "started") {
                Result.success(null)
            } else {
                Result.success(route)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun markStopVisited(truckId: Int, clientId: String): Result<DeliveryRoute> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(IllegalStateException("Debe iniciar sesion antes de continuar"))

            val document = db.collection("routes")
                .document(getTodayRouteId(truckId, currentUser.uid))

            val currentRoute = document.get().await().toDeliveryRoute()
                ?: return Result.failure(IllegalStateException("No hay una ruta activa para hoy"))

            val updatedStops = currentRoute.sortedStops().map { stop ->
                if (stop.clientId == clientId) {
                    stop.copy(status = "visited")
                } else {
                    stop
                }
            }

            document.update("stops", updatedStops.map { stop ->
                mapOf(
                    "clientId" to stop.clientId,
                    "clientName" to stop.clientName,
                    "address" to stop.address,
                    "order" to stop.order,
                    "status" to stop.status
                )
            }).await()

            Result.success(currentRoute.copy(stops = updatedStops))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun finishRoute(truckId: Int): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(IllegalStateException("Debe iniciar sesion antes de continuar"))

            db.collection("routes")
                .document(getTodayRouteId(truckId, currentUser.uid))
                .update(
                    mapOf(
                        "status" to "finished",
                        "finishedAt" to Timestamp.now()
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getDriverStats(): Result<RouteStats> {
        return try {
            val currentUser = auth.currentUser
                ?: return Result.failure(IllegalStateException("Debe iniciar sesion"))

            val snapshot = db.collection("routes")
                .whereEqualTo("driverId", currentUser.uid)
                .get()
                .await()

            val allRoutes = snapshot.documents.mapNotNull { it.toDeliveryRoute() }
            val finishedRoutes = allRoutes.filter { it.status == "finished" }

            val totalRoutes = allRoutes.size
            val totalFinishedRoutes = finishedRoutes.size
            val totalStops = allRoutes.sumOf { it.stops.size }
            val totalClientsVisited = finishedRoutes.sumOf { route ->
                route.stops.count { it.status == "visited" }
            }

            Result.success(
                RouteStats(
                    totalRoutes = totalRoutes,
                    totalFinishedRoutes = totalFinishedRoutes,
                    totalClientsVisited = totalClientsVisited,
                    totalStops = totalStops
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun getTodayRouteId(truckId: Int, driverUid: String): String {
        return "${LocalDate.now()}_${truckId}_$driverUid"
    }

    private fun DocumentSnapshot.toDeliveryRoute(): DeliveryRoute? {
        if (!exists()) return null

        val stopsData = get("stops") as? List<Map<String, Any?>> ?: emptyList()
        val stops = stopsData.map { stop ->
            RouteStop(
                clientId = stop["clientId"] as? String ?: "",
                clientName = stop["clientName"] as? String ?: "",
                address = stop["address"] as? String ?: "",
                order = (stop["order"] as? Number)?.toInt() ?: 0,
                status = stop["status"] as? String ?: "pending"
            )
        }

        return DeliveryRoute(
            id = id,
            truckId = (getLong("truckId") ?: 0L).toInt(),
            date = getString("date") ?: "",
            status = getString("status") ?: "",
            stops = stops
        )
    }
}
