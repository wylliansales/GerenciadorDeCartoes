package com.treinamento.gerenciadordecartoes.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.treinamento.gerenciadordecartoes.model.Card as CreditCard
import com.treinamento.gerenciadordecartoes.state.CardUiState
import com.treinamento.gerenciadordecartoes.view.components.CardItem
import com.treinamento.gerenciadordecartoes.util.toCurrency

@Composable
fun CardListScreen(
    state: CardUiState,
    contentPadding: PaddingValues,
    onCardClick: (String) -> Unit,
    onRequestCard: () -> Unit,
) {
    if (state.isLoading) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        return
    }
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(contentPadding),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.NotificationsNone, "Notificações")
                Text("Meus Cartões", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                IconButton(onClick = onRequestCard) { Icon(Icons.Rounded.Add, "Adicionar cartão") }
            }
            Spacer(Modifier.height(8.dp))
            Text("Cartão Principal", fontWeight = FontWeight.SemiBold)
        }
        state.cards.firstOrNull()?.let { card ->
            item {
                Column {
                    CardItem(card, { onCardClick(card.id) })
                    Surface(
                        shape = RoundedCornerShape(bottomStart = 14.dp, bottomEnd = 14.dp),
                        shadowElevation = 3.dp,
                        modifier = Modifier.fillMaxWidth(),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Column { Text("Limite disponível", style = MaterialTheme.typography.labelSmall); Text(card.availableLimit.toCurrency(), fontWeight = FontWeight.Bold) }
                                Column(horizontalAlignment = Alignment.End) { Text("Limite total", style = MaterialTheme.typography.labelSmall); Text(card.limit.toCurrency()) }
                            }
                            Spacer(Modifier.height(10.dp))
                            LinearProgressIndicator(progress = { (card.usedLimit / card.limit).toFloat() }, modifier = Modifier.fillMaxWidth())
                        }
                    }
                }
            }
        }
        item { Text("Outros Cartões", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 8.dp)) }
        items(state.cards.drop(1), key = { it.id }) { card -> CompactCard(card) { onCardClick(card.id) } }
    }
}

@Composable
private fun CompactCard(card: CreditCard, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(54.dp).background(if (card.isBlocked) Color(0xFF2D3545) else Color(0xFFDAA845), RoundedCornerShape(8.dp)), contentAlignment = Alignment.Center) {
                Text(if (card.brand.contains("Master")) "●●" else "VISA", color = Color.White, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(card.brand, fontWeight = FontWeight.SemiBold)
                Text("•••• •••• •••• ${card.lastFourDigits}", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                Text("Disponível  ${card.availableLimit.toCurrency()}", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Medium)
            }
            Text(
                card.blockStatus.shortDisplayName,
                color = if (card.isBlocked) Color(0xFFCF3344) else Color(0xFF148463),
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}
