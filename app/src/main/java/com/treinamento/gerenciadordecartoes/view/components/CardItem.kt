package com.treinamento.gerenciadordecartoes.view.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.treinamento.gerenciadordecartoes.model.Card as CreditCard
import com.treinamento.gerenciadordecartoes.view.theme.CardBlue
import com.treinamento.gerenciadordecartoes.view.theme.CardPurple

@Composable
fun CardItem(card: CreditCard, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent, contentColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
    ) {
        Column(
            modifier = Modifier.background(
                Brush.linearGradient(listOf(CardPurple, Color(0xFF3A48D8), CardBlue))
            ).padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(card.brand, fontWeight = FontWeight.SemiBold)
                Text(
                    card.blockStatus.shortDisplayName,
                    color = Color.White,
                    modifier = Modifier.background(
                        if (card.isBlocked) Color(0xFFE55563) else Color(0xFF37CFA9),
                        RoundedCornerShape(6.dp),
                    ).padding(horizontal = 8.dp, vertical = 3.dp),
                    style = MaterialTheme.typography.labelSmall,
                )
            }
            Text("••••  ••••  ••••  ${card.lastFourDigits}", style = MaterialTheme.typography.titleLarge)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column { Text("Titular", style = MaterialTheme.typography.labelSmall); Text(card.holderName) }
                Text("VISA", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Black)
            }
        }
    }
}
