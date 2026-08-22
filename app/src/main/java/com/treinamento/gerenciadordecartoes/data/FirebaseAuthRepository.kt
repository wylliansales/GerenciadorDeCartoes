package com.treinamento.gerenciadordecartoes.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.UserProfileChangeRequest
import com.treinamento.gerenciadordecartoes.repository.AuthRepository
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class FirebaseAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
) : AuthRepository {

    override suspend fun login(email: String, password: String): Result<Unit> = runCatching {
        require(email.isNotBlank() && password.isNotBlank()) { "Informe e-mail e senha." }
        awaitResult { done ->
            auth.signInWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task -> done(task.exception) }
        }
    }.mapFirebaseError()

    override suspend fun register(name: String, email: String, password: String): Result<Unit> = runCatching {
        require(name.isNotBlank()) { "Informe seu nome completo." }
        require(email.isNotBlank()) { "Informe seu e-mail." }
        require(password.length >= 6) { "A senha deve ter pelo menos 6 caracteres." }

        awaitResult { done ->
            auth.createUserWithEmailAndPassword(email.trim(), password)
                .addOnCompleteListener { task -> done(task.exception) }
        }

        val profile = UserProfileChangeRequest.Builder()
            .setDisplayName(name.trim())
            .build()
        awaitResult { done ->
            auth.currentUser?.updateProfile(profile)
                ?.addOnCompleteListener { task -> done(task.exception) }
                ?: done(IllegalStateException("Usuário criado, mas a sessão não foi iniciada."))
        }
    }.mapFirebaseError()

    private suspend fun awaitResult(start: ((Throwable?) -> Unit) -> Unit) =
        suspendCoroutine { continuation ->
            start { error ->
                if (error == null) continuation.resume(Unit)
                else continuation.resumeWith(Result.failure(error))
            }
        }

    private fun Result<Unit>.mapFirebaseError(): Result<Unit> = fold(
        onSuccess = { Result.success(Unit) },
        onFailure = { error ->
            Result.failure(IllegalArgumentException(error.toUserMessage(), error))
        },
    )

    private fun Throwable.toUserMessage(): String {
        if (message?.contains("CONFIGURATION_NOT_FOUND", ignoreCase = true) == true) {
            return "Ative o provedor E-mail/senha no Firebase Authentication e tente novamente."
        }

        return when ((this as? FirebaseAuthException)?.errorCode) {
            "ERROR_INVALID_EMAIL" -> "E-mail inválido."
            "ERROR_INVALID_CREDENTIAL", "ERROR_WRONG_PASSWORD", "ERROR_USER_NOT_FOUND" ->
                "E-mail ou senha incorretos."
            "ERROR_EMAIL_ALREADY_IN_USE" -> "Este e-mail já está cadastrado."
            "ERROR_WEAK_PASSWORD" -> "A senha deve ter pelo menos 6 caracteres."
            "ERROR_NETWORK_REQUEST_FAILED" -> "Sem conexão. Verifique a internet e tente novamente."
            "ERROR_TOO_MANY_REQUESTS" -> "Muitas tentativas. Aguarde um pouco e tente novamente."
            else -> message ?: "Não foi possível autenticar. Tente novamente."
        }
    }
}
