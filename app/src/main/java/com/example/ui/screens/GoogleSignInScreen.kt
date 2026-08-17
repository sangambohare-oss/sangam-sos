package com.example.ui.screens

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.ui.viewmodel.MainViewModel

@Composable
fun GoogleSignInScreen(
    viewModel: MainViewModel,
    onSignInSuccess: () -> Unit,
    modifier: Modifier = Modifier
) {
    LoginScreen(
        viewModel = viewModel,
        onSignInSuccess = onSignInSuccess,
        modifier = modifier
    )
}
