package com.treinamento.gerenciadordecartoes.view.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.treinamento.gerenciadordecartoes.model.Card
import com.treinamento.gerenciadordecartoes.model.PurchaseRequest
import com.treinamento.gerenciadordecartoes.util.toCurrency
import com.treinamento.gerenciadordecartoes.view.components.AppButton
import com.treinamento.gerenciadordecartoes.view.components.TopBar

@Composable
fun PurchaseEntryScreen(
    card: Card?,
    message: String?,
    onBack: () -> Unit,
    onSubmit: (PurchaseRequest) -> Unit,
) {
    if (card == null) return
    var merchant by remember { mutableStateOf("") }
    var date by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }

    Column(Modifier.fillMaxSize()) {
        TopBar("Lançar compra", onBack)
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(card.brand, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text("Limite disponível: ${card.availableLimit.toCurrency()}")
            PurchaseField(merchant, { merchant = it }, "Onde comprou", "Ex.: Supermercado Central")
            PurchaseField(date, { date = it }, "Data", "Ex.: 24/08/2026")
            PurchaseField(
                amount, { amount = it }, "Valor", "Ex.: 186,90", KeyboardType.Decimal,
            )
            PurchaseField(category, { category = it }, "Categoria", "Ex.: Alimentação")
            Spacer(Modifier.height(4.dp))
            AppButton(
                text = "Registrar compra",
                enabled = merchant.isNotBlank() && date.isNotBlank() &&
                    amount.isNotBlank() && category.isNotBlank() && !card.isBlocked,
                onClick = {
                    onSubmit(
                        PurchaseRequest(
                            cardId = card.id,
                            merchant = merchant.trim(),
                            date = date.trim(),
                            amount = amount.replace(',', '.').toDoubleOrNull() ?: 0.0,
                            category = category.trim(),
                        )
                    )
                },
            )
            if (card.isBlocked) Text("O cartão precisa estar ativo para lançar compras.", color = MaterialTheme.colorScheme.error)
            message?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
        }
    }
}

@Composable
private fun PurchaseField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    supporting: String,
    keyboardType: KeyboardType = KeyboardType.Text,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        supportingText = { Text(supporting) },
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        singleLine = true,
        modifier = Modifier.fillMaxWidth(),
    )
}
