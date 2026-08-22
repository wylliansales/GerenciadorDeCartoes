package com.treinamento.gerenciadordecartoes.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.treinamento.gerenciadordecartoes.data.MockCardRepository
import com.treinamento.gerenciadordecartoes.data.FirebaseAuthRepository
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.repository.CardRepository
import com.treinamento.gerenciadordecartoes.repository.AuthRepository
import com.treinamento.gerenciadordecartoes.state.CardUiState
import com.treinamento.gerenciadordecartoes.state.LoginUiState
import com.treinamento.gerenciadordecartoes.state.ProfileUiState
import com.treinamento.gerenciadordecartoes.state.RegisterUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CardViewModel(
    private val repository: CardRepository = MockCardRepository(),
    private val authRepository: AuthRepository = FirebaseAuthRepository(),
) : ViewModel() {
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
            repository.observeCards().collect { cards ->
                _uiState.update { current ->
                    current.copy(
                        cards = cards,
                        selectedCardId = current.selectedCardId ?: cards.firstOrNull()?.id,
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
            repository.observePurchases(cardId).collect { purchases ->
                _uiState.update { it.copy(purchases = purchases) }
            }
        }
    }

    fun setBlocked(blocked: Boolean) = viewModelScope.launch {
        val id = _uiState.value.selectedCardId ?: return@launch
        repository.setCardBlocked(id, blocked)
        showMessage(if (blocked) "Cartão bloqueado." else "Cartão desbloqueado.")
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

    fun requestCard(name: String, type: String, limit: String, onSuccess: () -> Unit) =
        viewModelScope.launch {
            val amount = limit.replace(',', '.').toDoubleOrNull()
            if (name.isBlank() || amount == null || amount <= 0) {
                showMessage("Preencha nome e limite corretamente.")
                return@launch
            }
            repository.requestCard(CardRequest(name, type, amount))
                .onSuccess {
                    showMessage("Solicitação enviada para análise.")
                    onSuccess()
                }
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

    private fun com.treinamento.gerenciadordecartoes.model.AuthenticatedUser?.toProfileState() =
        ProfileUiState(
            name = this?.name.orEmpty(),
            email = this?.email.orEmpty(),
        )
}
