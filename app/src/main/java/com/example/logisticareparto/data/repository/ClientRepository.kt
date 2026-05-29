package com.example.logisticareparto.data.repository

import android.net.Uri
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.example.logisticareparto.data.models.Client
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class ClientRepository(private val db: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    suspend fun getClients(): List<Client> = try {
        val snapshot = db.collection("clientes").get().await()
        snapshot.documents.mapNotNull { it.toObject(Client::class.java)?.copy(id = it.id) }
    } catch (e: Exception) {
        emptyList()
    }

    suspend fun createClient(client: Client): Result<Unit> = try {
        val clientMap = mapOf(
            "cliente" to client.cliente,
            "direccion" to client.direccion,
            "contacto" to client.contacto,
            "cuil" to client.cuil,
            "dias" to client.dias,
            "apertura" to client.apertura,
            "cierre" to client.cierre,
            "es24" to client.es24,
            "imagenUrl" to client.imagenUrl,
            "latitud" to client.latitud,
            "longitud" to client.longitud,
            "reparto" to client.reparto
        )
        db.collection("clientes").add(clientMap).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun updateClientBasicData(clientId: String, data: Map<String, Any>): Result<Unit> = try {
        db.collection("clientes").document(clientId).update(data).await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    suspend fun uploadImage(uri: Uri, preset: String, cloudName: String): Result<String> = suspendCoroutine { continuation ->
        MediaManager.get().upload(uri)
            .unsigned(preset)
            .option("cloud_name", cloudName)
            .callback(object : UploadCallback {
                override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                    continuation.resume(Result.success(resultData["secure_url"] as String))
                }
                override fun onError(requestId: String, error: ErrorInfo) {
                    continuation.resume(Result.failure(Exception(error.description)))
                }
                override fun onStart(requestId: String) {}
                override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {}
                override fun onReschedule(requestId: String, error: ErrorInfo) {}
            }).dispatch()
    }
}
