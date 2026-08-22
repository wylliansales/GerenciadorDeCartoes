package com.treinamento.gerenciadordecartoes.view.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.PersonAdd
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.treinamento.gerenciadordecartoes.state.RegisterUiState
import com.treinamento.gerenciadordecartoes.view.components.AppButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    state: RegisterUiState,
    onNameChange: (String) -> Unit,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onRegister: () -> Unit,
    onBack: () -> Unit
) {
    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedBorderColor = Color(0xFF6AA7F8),
        unfocusedBorderColor = Color(0xFF5780A9),
        focusedLabelColor = Color.White,
        unfocusedLabelColor = Color(0xFFD9E6F5),
        cursorColor = Color.White,
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(listOf(Color(0xFF0B437B), Color(0xFF063260), Color(0xFF052B55)))
            )
            .padding(horizontal = 28.dp),
    ) {
        Spacer(Modifier.height(16.dp))
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Voltar", tint = Color.White)
        }
        
        Column(
            modifier = Modifier.fillMaxWidth().weight(1f),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .background(
                        Brush.linearGradient(listOf(Color(0xFF3B87FA), Color(0xFF6950F3))),
                        RoundedCornerShape(12.dp)
                    )
                    .padding(16.dp),
            ) {
                Icon(Icons.Rounded.PersonAdd, null, tint = Color.White)
            }
            
            Spacer(Modifier.height(22.dp))
            Text(
                "Criar Conta",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                "Preencha os dados abaixo para começar.",
                color = Color(0xFFE7F0FA),
                textAlign = TextAlign.Center
            )
            
            Spacer(Modifier.height(30.dp))
            
            OutlinedTextField(
                value = state.name,
                onValueChange = onNameChange,
                label = { Text("Nome completo") },
                singleLine = true,
                colors = fieldColors,
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = state.email,
                onValueChange = onEmailChange,
                label = { Text("E-mail") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                colors = fieldColors,
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(Modifier.height(12.dp))
            
            OutlinedTextField(
                value = state.password,
                onValueChange = onPasswordChange,
                label = { Text("Senha") },
                supportingText = { Text("Mínimo de 6 caracteres") },
                singleLine = true,
                visualTransformation = PasswordVisualTransformation(),
                colors = fieldColors,
                shape = RoundedCornerShape(9.dp),
                modifier = Modifier.fillMaxWidth()
            )
            
            state.error?.let {
                Text(
                    it,
                    color = Color(0xFFFFB4AB),
                    modifier = Modifier.padding(top = 8.dp),
                    style = MaterialTheme.typography.bodySmall
                )
            }
            
            Spacer(Modifier.height(24.dp))
            
            AppButton(
                text = "Cadastrar",
                onClick = onRegister,
                loading = state.isLoading,
                enabled = state.name.isNotBlank() && state.email.isNotBlank() && state.password.length >= 6,
            )
            
            Spacer(Modifier.height(18.dp))
            
            Text(
                "Já tem uma conta? Entre aqui",
                color = Color(0xFFD7E5F3),
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable { onBack() }
            )
        }
    }
}
