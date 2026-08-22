package com.treinamento.gerenciadordecartoes.navigation

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.treinamento.gerenciadordecartoes.view.components.BottomNavigation
import com.treinamento.gerenciadordecartoes.view.screens.CardDetailsScreen
import com.treinamento.gerenciadordecartoes.view.screens.CardListScreen
import com.treinamento.gerenciadordecartoes.view.screens.LoginScreen
import com.treinamento.gerenciadordecartoes.view.screens.RegisterScreen
import com.treinamento.gerenciadordecartoes.view.screens.ManageCardScreen
import com.treinamento.gerenciadordecartoes.view.screens.RequestCardScreen
import com.treinamento.gerenciadordecartoes.view.screens.ProfileScreen
import com.treinamento.gerenciadordecartoes.viewmodel.CardViewModel

@Composable
fun CardManagerApp(cardViewModel: CardViewModel = viewModel()) {
    val navController = rememberNavController()
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route
    val showBottomBar = route == AppRoute.Cards.route ||
        route == AppRoute.Request.route ||
        route == AppRoute.Profile.route

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                BottomNavigation(route) { target ->
                    navController.navigate(target) {
                        popUpTo(AppRoute.Cards.route) { inclusive = false }
                        launchSingleTop = true
                    }
                }
            }
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = if (cardViewModel.isUserLoggedIn()) {
                AppRoute.Cards.route
            } else {
                AppRoute.Login.route
            },
            modifier = Modifier,
        ) {
            composable(AppRoute.Login.route) {
                val state by cardViewModel.loginState.collectAsStateWithLifecycle()
                LoginScreen(
                    state = state,
                    onEmailChange = cardViewModel::updateEmail,
                    onPasswordChange = cardViewModel::updatePassword,
                    onLogin = {
                        cardViewModel.login {
                            navController.navigate(AppRoute.Cards.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
                            }
                        }
                    },
                    onRegisterClick = {
                        navController.navigate(AppRoute.Register.route)
                    }
                )
            }
            composable(AppRoute.Register.route) {
                val state by cardViewModel.registerState.collectAsStateWithLifecycle()
                RegisterScreen(
                    state = state,
                    onNameChange = cardViewModel::updateRegisterName,
                    onEmailChange = cardViewModel::updateRegisterEmail,
                    onPasswordChange = cardViewModel::updateRegisterPassword,
                    onRegister = {
                        cardViewModel.register {
                            navController.navigate(AppRoute.Cards.route) {
                                popUpTo(AppRoute.Login.route) { inclusive = true }
                            }
                        }
                    },
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppRoute.Cards.route) {
                val state by cardViewModel.uiState.collectAsStateWithLifecycle()
                CardListScreen(
                    state = state,
                    contentPadding = padding,
                    onCardClick = { id ->
                        cardViewModel.selectCard(id)
                        navController.navigate(AppRoute.Details.create(id))
                    },
                    onRequestCard = { navController.navigate(AppRoute.Request.route) },
                )
            }
            composable(AppRoute.Details.route) { entry ->
                val id = entry.arguments?.getString("cardId").orEmpty()
                LaunchedEffect(id) { cardViewModel.selectCard(id) }
                val state by cardViewModel.uiState.collectAsStateWithLifecycle()
                CardDetailsScreen(
                    state = state,
                    onBack = navController::popBackStack,
                    onManage = { navController.navigate(AppRoute.Manage.create(id)) },
                )
            }
            composable(AppRoute.Request.route) {
                val state by cardViewModel.uiState.collectAsStateWithLifecycle()
                RequestCardScreen(
                    message = state.message,
                    contentPadding = padding,
                    onClearMessage = cardViewModel::clearMessage,
                    onSubmit = { name, type, limit ->
                        cardViewModel.requestCard(name, type, limit) {
                            navController.navigate(AppRoute.Cards.route) { launchSingleTop = true }
                        }
                    },
                )
            }
            composable(AppRoute.Profile.route) {
                LaunchedEffect(Unit) { cardViewModel.refreshProfile() }
                val state by cardViewModel.profileState.collectAsStateWithLifecycle()
                ProfileScreen(
                    state = state,
                    contentPadding = padding,
                    onLogout = {
                        cardViewModel.logout()
                        navController.navigate(AppRoute.Login.route) {
                            popUpTo(navController.graph.startDestinationId) { inclusive = true }
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable(AppRoute.Manage.route) { entry ->
                val id = entry.arguments?.getString("cardId").orEmpty()
                LaunchedEffect(id) { cardViewModel.selectCard(id) }
                val state by cardViewModel.uiState.collectAsStateWithLifecycle()
                ManageCardScreen(
                    card = state.selectedCard,
                    message = state.message,
                    onBack = navController::popBackStack,
                    onBlockStatusChange = cardViewModel::setBlockStatus,
                    onUpdateLimit = cardViewModel::updateLimit,
                    onClearMessage = cardViewModel::clearMessage,
                )
            }
        }
    }
}
