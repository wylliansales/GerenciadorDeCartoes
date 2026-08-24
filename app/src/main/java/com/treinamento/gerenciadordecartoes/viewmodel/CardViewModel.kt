package com.treinamento.gerenciadordecartoes.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.treinamento.gerenciadordecartoes.data.FirebaseAuthRepository
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.model.PurchaseRequest
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.repository.CardRepository
import com.treinamento.gerenciadordecartoes.repository.AuthRepository
import com.treinamento.gerenciadordecartoes.data.local.CardDatabase
import com.treinamento.gerenciadordecartoes.data.local.RoomCardDataSource
import com.treinamento.gerenciadordecartoes.repository.OfflineFirstCardRepository
import com.treinamento.gerenciadordecartoes.state.CardUiState
import com.treinamento.gerenciadordecartoes.state.LoginUiState
import com.treinamento.gerenciadordecartoes.state.ProfileUiState
import com.treinamento.gerenciadordecartoes.state.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: CardRepository = OfflineFirstCardRepository(
        RoomCardDataSource(CardDatabase.getInstance(application).cardDao()),
    ),
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(CardUiState(isLoading = true))
    val uiState: StateFlow<CardUiState> = _uiState.asStateFlow()

    private val _loginState = MutableStateFlow(LoginUiState())
    val loginState: StateFlow<LoginUiState> = _loginState.asStateFlow()

    private val _registerState = MutableStateFlow(RegisterUiState())
    val registerState: StateFlow<RegisterUiState> = _registerState.asStateFlow()

    private val _profileState = MutableStateFlow(authRepository.currentUser().toProfileState())
    val profileState: StateFlow<ProfileUiState> = _profileState.asStateFlow()

    init {
        viewModelScope.launch {
            repository.observeCards()
                .catch { error ->
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            message = error.toFirestoreMessage(),
                        )
                    }
                }
                .collect { cards ->
                _uiState.update { current ->
                    current.copy(
                        cards = cards,
                        selectedCardId = current.selectedCardId
                            ?.takeIf { selectedId -> cards.any { it.id == selectedId } }
                            ?: cards.firstOrNull()?.id,
                        isLoading = false,
                    )
                }
            }
        }
    }

    fun updateEmail(value: String) = _loginState.update { it.copy(email = value, error = null) }
    fun updatePassword(value: String) = _loginState.update { it.copy(password = value, error = null) }

    fun updateRegisterName(value: String) = _registerState.update { it.copy(name = value, error = null) }
    fun updateRegisterEmail(value: String) = _registerState.update { it.copy(email = value, error = null) }
    fun updateRegisterPassword(value: String) = _registerState.update { it.copy(password = value, error = null) }

    fun login(onSuccess: () -> Unit) = viewModelScope.launch {
        val form = _loginState.value
        _loginState.update { it.copy(isLoading = true, error = null) }
        authRepository.login(form.email, form.password)
            .onSuccess {
                refreshProfile()
                onSuccess()
            }
            .onFailure { error -> _loginState.update { it.copy(error = error.message) } }
        _loginState.update { it.copy(isLoading = false) }
    }

    fun register(onSuccess: () -> Unit) = viewModelScope.launch {
        val form = _registerState.value
        _registerState.update { it.copy(isLoading = true, error = null) }
        authRepository.register(form.name, form.email, form.password)
            .onSuccess {
                refreshProfile()
                onSuccess()
            }
            .onFailure { error -> _registerState.update { it.copy(error = error.message) } }
        _registerState.update { it.copy(isLoading = false) }
    }

    fun selectCard(cardId: String) {
        _uiState.update { it.copy(selectedCardId = cardId, purchases = emptyList()) }
        viewModelScope.launch {
            repository.observePurchases(cardId)
                .catch { error -> showMessage(error.toFirestoreMessage()) }
                .collect { purchases -> _uiState.update { it.copy(purchases = purchases) } }
        }
    }

    fun setBlockStatus(status: CardBlockStatus) = viewModelScope.launch {
        val id = _uiState.value.selectedCardId ?: return@launch
        repository.setCardBlockStatus(id, status)
            .onSuccess {
                showMessage(
                    if (status == CardBlockStatus.ACTIVE) "Cartão desbloqueado."
                    else "Status alterado para ${status.displayName.lowercase()}."
                )
            }
            .onFailure { showMessage(it.toFirestoreMessage()) }
    }

    fun updateLimit(value: String) = viewModelScope.launch {
        val id = _uiState.value.selectedCardId ?: return@launch
        val limit = value.replace(',', '.').toDoubleOrNull()
        if (limit == null) {
            showMessage("Digite um limite válido.")
            return@launch
        }
        repository.updateLimit(id, limit)
            .onSuccess { showMessage("Limite atualizado.") }
            .onFailure { showMessage(it.message ?: "Não foi possível atualizar.") }
    }

    fun requestCard(request: CardRequest, onSuccess: () -> Unit) =
        viewModelScope.launch {
            repository.requestCard(request)
                .onSuccess {
                    showMessage("Cartão cadastrado com sucesso.")
                    onSuccess()
                }
                .onFailure { showMessage(it.toFirestoreMessage()) }
        }

    fun addPurchase(request: PurchaseRequest, onSuccess: () -> Unit) = viewModelScope.launch {
        repository.addPurchase(request)
            .onSuccess {
                showMessage("Compra registrada e limite atualizado.")
                onSuccess()
            }
            .onFailure { showMessage(it.toFirestoreMessage()) }
    }

    fun clearMessage() = _uiState.update { it.copy(message = null) }

    fun isUserLoggedIn(): Boolean = authRepository.currentUser() != null

    fun refreshProfile() {
        _profileState.value = authRepository.currentUser().toProfileState()
    }

    fun logout() {
        authRepository.logout()
        _profileState.value = ProfileUiState()
        _loginState.value = LoginUiState()
        _registerState.value = RegisterUiState()
    }

    private fun showMessage(message: String) = _uiState.update { it.copy(message = message) }

    private fun Throwable.toFirestoreMessage(): String = when {
        message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
            "Acesso negado pelo Firestore. Confira e publique as regras de segurança."
        message?.contains("NOT_FOUND", ignoreCase = true) == true ->
            "Crie o banco Cloud Firestore no Console Firebase."
        else -> message ?: "Não foi possível acessar os dados no Firebase."
    }

    private fun com.treinamento.gerenciadordecartoes.model.AuthenticatedUser?.toProfileState() =
        ProfileUiState(
            name = this?.name.orEmpty(),
            email = this?.email.orEmpty(),
        )
}
