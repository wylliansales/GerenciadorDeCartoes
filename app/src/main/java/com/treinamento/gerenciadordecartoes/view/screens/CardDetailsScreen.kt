package com.treinamento.gerenciadordecartoes.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.MoreHoriz
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.treinamento.gerenciadordecartoes.model.Purchase
import com.treinamento.gerenciadordecartoes.state.CardUiState
import com.treinamento.gerenciadordecartoes.view.components.CardItem
import com.treinamento.gerenciadordecartoes.view.components.TopBar
import com.treinamento.gerenciadordecartoes.util.toCurrency

@Composable
fun CardDetailsScreen(
    state: CardUiState,
    onBack: () -> Unit,
    onManage: () -> Unit,
    onAddPurchase: () -> Unit,
) {
    val card = state.selectedCard ?: return
    var showPurchases by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        TopBar("Detalhes do Cartão", onBack)
        LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { CardItem(card, onClick = {}) }
            item {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    ActionTile("Bloquear\ncartão", Icons.Rounded.Lock, Modifier.weight(1f), onManage)
                    ActionTile("Alterar\nlimite", Icons.Rounded.SwapVert, Modifier.weight(1f), onManage)
                    ActionTile("Nova\ncompra", Icons.Rounded.MoreHoriz, Modifier.weight(1f), onAddPurchase)
                }
            }
            item {
                Surface(shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Informações", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        InfoRow("Titular", card.holderName)
                        InfoRow("Vencimento", card.expirationDate.ifBlank { "Não informado" })
                        InfoRow("Limite total", card.limit.toCurrency())
                        InfoRow("Limite disponível", card.availableLimit.toCurrency())
                        InfoRow("Fatura atual", card.usedLimit.toCurrency(), true)
                    }
                }
            }
            item {
                Surface(onClick = { showPurchases = !showPurchases }, shape = RoundedCornerShape(12.dp), color = Color.White, shadowElevation = 2.dp, modifier = Modifier.fillMaxWidth()) {
                    Row(Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text(if (showPurchases) "Ocultar compras" else "Ver compras da fatura", fontWeight = FontWeight.SemiBold)
                        Text(if (showPurchases) "⌃" else "›")
                    }
                }
            }
            if (showPurchases) {
                item {
                    Button(onClick = onAddPurchase, modifier = Modifier.fillMaxWidth()) {
                        Text("Lançar nova compra")
                    }
                }
                items(state.purchases, key = { it.id }) { PurchaseRow(it) }
            }
        }
    }
}

@Composable
private fun ActionTile(label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, modifier: Modifier, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = modifier, shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 3.dp) {
        Column(Modifier.padding(vertical = 14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.background(Color(0xFFEEF1FF), RoundedCornerShape(50)).padding(9.dp)) { Icon(icon, null, tint = Color(0xFF554BE7)) }
            Spacer(Modifier.height(6.dp)); Text(label, style = MaterialTheme.typography.bodySmall, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, bold: Boolean = false) {
    Row(Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Color.DarkGray); Text(value, fontWeight = if (bold) FontWeight.Bold else FontWeight.Medium)
    }
    HorizontalDivider(color = Color(0xFFE9EBF1))
}

@Composable
private fun PurchaseRow(purchase: Purchase) {
    Surface(shape = RoundedCornerShape(10.dp), color = Color.White) {
        Row(Modifier.fillMaxWidth().padding(14.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Column { Text(purchase.merchant, fontWeight = FontWeight.SemiBold); Text("${purchase.category} • ${purchase.date}", style = MaterialTheme.typography.bodySmall, color = Color.Gray) }
            Text(purchase.amount.toCurrency(), fontWeight = FontWeight.Bold)
        }
    }
}
