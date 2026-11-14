package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.util.UUID

class ProjectRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveProject_validPayload_createsDocumentAndReturnsId() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = ProjectRepository(firestore)

        val saveResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveProject(
                name = "Robot_${UUID.randomUUID().toString().take(6)}",
                geometryMode = "SMOOTH_ORGANIQUE",
                renderEngine = "POLYX3D_MOBILE",
                nodeCount = 3L,
                keyframeCount = 4L,
                sceneSummary = "Cube_Studio:CUBE:0.0:0.0:0.0"
            )
        }
        assertTrue("Expected saveProject to succeed: ${saveResult.exceptionOrNull()}", saveResult.isSuccess)
        assertTrue(saveResult.getOrThrow().isNotEmpty())
    }

    @Test
    fun getUserProjects_authenticatedOwner_returnsSavedProjects() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = ProjectRepository(firestore)
        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveProject(
                name = "Vaisseau",
                geometryMode = "VOXEL_MODULAIRE",
                renderEngine = "AAA_REALISME",
                nodeCount = 5L,
                keyframeCount = 2L,
                sceneSummary = "Vaisseau:CUBE:0:0:0"
            ).getOrThrow()
        }

        val projectsResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getUserProjects() }
        assertTrue(projectsResult.isSuccess)
        assertTrue(projectsResult.getOrThrow().any { it.id == projectId })
    }

    @Test
    fun observeUserProjects_authenticatedOwner_emitsRealtimeUpdates() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = ProjectRepository(firestore)
        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveProject(
                name = "RealtimeScene",
                geometryMode = "SMOOTH_ORGANIQUE",
                renderEngine = "POLYX3D_MOBILE",
                nodeCount = 2L,
                keyframeCount = 3L,
                sceneSummary = "Sphere:SPHERE:0:0:0"
            ).getOrThrow()
        }

        val emitted = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeUserProjects(aliceUid).first { list -> list.any { it.id == projectId } }
        }
        assertTrue(emitted.any { it.id == projectId })
    }

    @Test
    fun updateAndDeleteProject_authenticatedOwner_succeeds() = runBlocking {
        signInTestUser(ALICE_EMAIL)
        val repository = ProjectRepository(firestore)
        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveProject(
                name = "Initial",
                geometryMode = "SMOOTH_ORGANIQUE",
                renderEngine = "POLYX3D_MOBILE",
                nodeCount = 1L,
                keyframeCount = 1L,
                sceneSummary = "Cube:CUBE:0:0:0"
            ).getOrThrow()
        }

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.getUserProjects().getOrThrow().first { it.id == projectId }
        }
        val updateRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.updateProject(fetched.copy(name = "UpdatedScene"))
        }
        assertTrue("Expected updateProject to succeed: ${updateRes.exceptionOrNull()}", updateRes.isSuccess)

        val deleteRes = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.deleteProject(projectId)
        }
        assertTrue("Expected deleteProject to succeed: ${deleteRes.exceptionOrNull()}", deleteRes.isSuccess)
    }

    @Test
    fun crossUserRead_aliceProjectByBob_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = ProjectRepository(firestore)
        val projectId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveProject(
                name = "AliceSecretScene",
                geometryMode = "SMOOTH_ORGANIQUE",
                renderEngine = "POLYX3D_MOBILE",
                nodeCount = 1L,
                keyframeCount = 1L,
                sceneSummary = "Cube:CUBE:0:0:0"
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        try {
            withTimeout(DEFAULT_TIMEOUT_MS) {
                firestore.collection("users").document(aliceUid)
                    .collection("projects").document(projectId)
                    .get()
                    .await()
            }
            fail("Expected PERMISSION_DENIED when Bob reads Alice's project")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    @Test
    fun observeUserProjects_unauthenticatedUser_failsWithPermissionDenied() = runBlocking {
        auth.signOut()
        val repository = ProjectRepository(firestore)

        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeUserProjects("alice_fake_uid").first()
            }
            fail("Expected PERMISSION_DENIED for unauthenticated user")
        } catch (e: Exception) {
            val fsEx = e.unwrapFirestoreException()
            assertNotNull("Expected nested FirebaseFirestoreException, got $e", fsEx)
            assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, fsEx!!.code)
        }
    }

    private companion object {
        const val ALICE_EMAIL = "alice@test.com"
        const val BOB_EMAIL = "bob@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
