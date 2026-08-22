package com.treinamento.gerenciadordecartoes.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.PauseCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.treinamento.gerenciadordecartoes.view.components.AppButton
import com.treinamento.gerenciadordecartoes.view.components.CardItem
import com.treinamento.gerenciadordecartoes.view.components.TopBar
import com.treinamento.gerenciadordecartoes.model.Card as CreditCard
import com.treinamento.gerenciadordecartoes.model.CardBlockStatus
import com.treinamento.gerenciadordecartoes.util.toCurrency

@Composable
fun ManageCardScreen(
    card: CreditCard?,
    message: String?,
    onBack: () -> Unit,
    onBlockStatusChange: (CardBlockStatus) -> Unit,
    onUpdateLimit: (String) -> Unit,
    onClearMessage: () -> Unit,
) {
    if (card == null) return
    var limit by remember(card.id) { mutableStateOf(card.limit.toFloat()) }
    LaunchedEffect(message) { if (message != null) kotlinx.coroutines.delay(3000).also { onClearMessage() } }
    Column(Modifier.fillMaxSize()) {
        TopBar("Gerenciar Cartão", onBack)
        androidx.compose.foundation.lazy.LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
            item { CardItem(card, onClick = {}) }
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 2.dp) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Bloquear cartão", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Temporariamente ou definitivamente.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))
                        BlockOption(
                            Icons.Rounded.PauseCircle,
                            "Bloqueio temporário",
                            Color(0xFF3D63D9),
                            card.blockStatus == CardBlockStatus.TEMPORARY_BLOCKED,
                        ) {
                            onBlockStatusChange(
                                if (card.blockStatus == CardBlockStatus.TEMPORARY_BLOCKED) {
                                    CardBlockStatus.ACTIVE
                                } else {
                                    CardBlockStatus.TEMPORARY_BLOCKED
                                }
                            )
                        }
                        Spacer(Modifier.height(8.dp))
                        BlockOption(
                            Icons.Rounded.Lock,
                            "Bloqueio definitivo",
                            Color(0xFFD33B4A),
                            card.blockStatus == CardBlockStatus.PERMANENTLY_BLOCKED,
                        ) {
                            onBlockStatusChange(
                                if (card.blockStatus == CardBlockStatus.PERMANENTLY_BLOCKED) {
                                    CardBlockStatus.ACTIVE
                                } else {
                                    CardBlockStatus.PERMANENTLY_BLOCKED
                                }
                            )
                        }
                    }
                }
            }
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = Color.White, shadowElevation = 2.dp) {
                    Column(Modifier.padding(14.dp)) {
                        Text("Alterar limite", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                        Text("Ajuste o limite total do seu cartão.", style = MaterialTheme.typography.bodySmall, color = Color.Gray)
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("Limite atual"); Text(card.limit.toCurrency(), fontWeight = FontWeight.Medium) }
                        Spacer(Modifier.height(12.dp))
                        OutlinedTextField(value = limit.toInt().toString(), onValueChange = { it.toFloatOrNull()?.let { value -> limit = value.coerceIn(2000f, 20000f) } }, label = { Text("Novo limite") }, prefix = { Text("R$ ") }, modifier = Modifier.fillMaxWidth())
                        Slider(value = limit, onValueChange = { limit = it }, valueRange = 2000f..20000f)
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) { Text("R$ 2.000", style = MaterialTheme.typography.labelSmall); Text("R$ 20.000", style = MaterialTheme.typography.labelSmall) }
                        Spacer(Modifier.height(10.dp))
                        AppButton("Salvar novo limite", { onUpdateLimit(limit.toInt().toString()) })
                    }
                }
            }
            message?.let { item { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) } }
        }
    }
}

@Composable
private fun BlockOption(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String, color: Color, selected: Boolean, onClick: () -> Unit) {
    Surface(onClick = onClick, shape = RoundedCornerShape(9.dp), border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) color else Color(0xFFE1E4EB)), color = Color.White, modifier = Modifier.fillMaxWidth()) {
        Row(Modifier.padding(13.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.background(color.copy(alpha = .10f), RoundedCornerShape(50)).padding(7.dp)) { Icon(icon, null, tint = color) }
            Spacer(Modifier.width(10.dp)); Text(text, color = color, fontWeight = FontWeight.Medium)
        }
    }
}
