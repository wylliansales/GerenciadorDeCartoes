package com.treinamento.gerenciadordecartoes.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.treinamento.gerenciadordecartoes.model.CardRequest
import com.treinamento.gerenciadordecartoes.view.components.AppButton

@Composable
fun RequestCardScreen(
    message: String?,
    defaultHolderName: String,
    contentPadding: PaddingValues,
    onClearMessage: () -> Unit,
    onSubmit: (CardRequest) -> Unit,
) {
    var cardName by remember { mutableStateOf("") }
    var holderName by remember(defaultHolderName) { mutableStateOf(defaultHolderName) }
    var cardNumber by remember { mutableStateOf("") }
    var securityCode by remember { mutableStateOf("") }
    var expirationDate by remember { mutableStateOf("") }
    var limit by remember { mutableStateOf("") }

    LaunchedEffect(message) {
        if (message != null) kotlinx.coroutines.delay(3000).also { onClearMessage() }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Text("Cadastrar cartão", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Text(
                "Cadastre um cartão fictício para demonstrar o gerenciamento no aplicativo.",
                style = MaterialTheme.typography.bodyMedium,
            )
        }
        item { FormField(cardName, { cardName = it }, "Nome do cartão", "Ex.: Visa Platinum") }
        item { FormField(holderName, { holderName = it }, "Nome do cliente") }
        item {
            FormField(
                cardNumber,
                { cardNumber = it.filter(Char::isDigit).take(19) },
                "Número do cartão",
                "13 a 19 dígitos",
                KeyboardType.Number,
            )
        }
        item {
            FormField(
                securityCode,
                { securityCode = it.filter(Char::isDigit).take(4) },
                "CVC",
                "3 ou 4 dígitos",
                KeyboardType.NumberPassword,
            )
        }
        item {
            FormField(
                expirationDate,
                { expirationDate = formatExpiration(it) },
                "Data de vencimento",
                "MM/AA",
                KeyboardType.Number,
            )
        }
        item {
            FormField(
                limit,
                { limit = it },
                "Limite total",
                "Ex.: 8000",
                KeyboardType.Decimal,
            )
        }
        item {
            AppButton(
                text = "Cadastrar cartão",
                enabled = cardName.isNotBlank() && holderName.isNotBlank() &&
                    cardNumber.length >= 13 && securityCode.length >= 3 &&
                    expirationDate.length == 5 && limit.isNotBlank(),
                onClick = {
                    onSubmit(
                        CardRequest(
                            holderName = holderName.trim(),
                            cardName = cardName.trim(),
                            cardNumber = cardNumber,
                            securityCode = securityCode,
                            expirationDate = expirationDate,
                            requestedLimit = limit.replace(',', '.').toDoubleOrNull() ?: 0.0,
                        )
                    )
                },
            )
        }
        message?.let { item { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) } }
    }
}

@Composable
private fun FormField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supporting: String = "",
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = if (supporting.isBlank()) null else ({ Text(supporting) }),
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth(),
    )
}

private fun formatExpiration(value: String): String {
    val digits = value.filter(Char::isDigit).take(4)
    return if (digits.length > 2) "${digits.take(2)}/${digits.drop(2)}" else digits
}
