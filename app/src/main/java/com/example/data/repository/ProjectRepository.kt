package com.example.data.repository

import android.content.Context
import com.example.R
import com.example.firebase.OperationType
import com.example.firebase.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

@IgnoreExtraProperties
data class StudioProject(
    @DocumentId val id: String = "",
    val userId: String = "",
    val name: String = "",
    val geometryMode: String = "SMOOTH_ORGANIQUE",
    val renderEngine: String = "POLYX3D_MOBILE",
    val nodeCount: Long = 0L,
    val keyframeCount: Long = 0L,
    val sceneSummary: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

class ProjectRepository(private val db: FirebaseFirestore) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String {
        return auth.currentUser?.uid
            ?: throw IllegalStateException("Utilisateur non connecté avec Google.")
    }

    fun observeUserProjects(userId: String): Flow<List<StudioProject>> = flow {
        val path = "users/$userId/projects"
        emitAll(
            db.collection("users").document(userId).collection("projects")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        StudioProject::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun getUserProjects(): Result<List<StudioProject>> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val path = "users/$uid/projects"
        return try {
            val snap = db.collection("users").document(uid).collection("projects").get().await()
            val list = snap.toObjects(
                StudioProject::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
            ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            Result.success(list)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.LIST, path)
            Result.failure(e)
        }
    }

    suspend fun saveProject(
        name: String,
        geometryMode: String,
        renderEngine: String,
        nodeCount: Long,
        keyframeCount: Long,
        sceneSummary: String
    ): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection("users").document(uid).collection("projects").document()
        val payload = mapOf(
            "userId" to uid,
            "name" to name.trim().ifBlank { "Projet OmniCore" }.take(80),
            "geometryMode" to geometryMode,
            "renderEngine" to renderEngine,
            "nodeCount" to nodeCount,
            "keyframeCount" to keyframeCount,
            "sceneSummary" to sceneSummary.take(32000),
            "createdAt" to FieldValue.serverTimestamp(),
            "updatedAt" to FieldValue.serverTimestamp()
        )
        return try {
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun updateProject(project: StudioProject): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        require(project.id.isNotBlank()) { "Project ID must not be blank for update" }
        requireNotNull(project.createdAt) { "Project createdAt must not be null for update" }
        val docRef = db.collection("users").document(uid).collection("projects").document(project.id)
        return try {
            docRef.set(project.copy(updatedAt = null)).await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun deleteProject(projectId: String): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val docRef = db.collection("users").document(uid).collection("projects").document(projectId)
        return try {
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }
}
