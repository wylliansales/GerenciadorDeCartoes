package com.treinamento.gerenciadordecartoes.view.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.treinamento.gerenciadordecartoes.navigation.AppRoute

@Composable
fun BottomNavigation(currentRoute: String?, onNavigate: (String) -> Unit) {
    NavigationBar {
        NavigationBarItem(
            selected = false,
            onClick = { onNavigate(AppRoute.Cards.route) },
            icon = { Icon(Icons.Rounded.Home, null) },
            label = { Text("Início") },
        )
        NavigationBarItem(
            selected = currentRoute == AppRoute.Cards.route,
            onClick = { onNavigate(AppRoute.Cards.route) },
            icon = { Icon(Icons.Rounded.CreditCard, null) },
            label = { Text("Cartões") },
        )
        NavigationBarItem(
            selected = currentRoute == AppRoute.Request.route,
            onClick = { onNavigate(AppRoute.Request.route) },
            icon = { Icon(Icons.Rounded.ShoppingCart, null) },
            label = { Text("Solicitar") },
        )
        NavigationBarItem(
            selected = currentRoute == AppRoute.Profile.route,
            onClick = { onNavigate(AppRoute.Profile.route) },
            icon = { Icon(Icons.Rounded.Person, null) },
            label = { Text("Perfil") },
        )
    }
}
