package com.dadm.artisticall.lobby

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import com.dadm.artisticall.login.GoogleSignInClient
import kotlinx.coroutines.launch

@Composable
fun LobbyScreen(
    navController: NavController
){
    val context = LocalContext.current
    val googleSignInClient = remember { GoogleSignInClient(context) }
    var isSignedIn by remember { mutableStateOf(false) }
    val lifecycleOwner = LocalLifecycleOwner.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column{
            Text(
                text = "Selecciona un juego",
                fontSize = 20.sp,
                modifier = Modifier.padding(16.dp),
                color = Color.White
            )
            Row(
                modifier = Modifier.padding(bottom = 16.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                Button(
                    onClick = {
                        navController.navigate("solo_screen")
                    }
                ) {
                    Text(text = "Solo")
                }
            }
            Spacer(modifier = Modifier.padding(16.dp))
            OutlinedButton(onClick = {
                lifecycleOwner.lifecycleScope.launch {
                    googleSignInClient.signOut()
                    isSignedIn = false
                }
                navController.navigate("login_screen")
            }) {
                Text(
                    text = "Sign Out",
                    fontSize = 16.sp,
                    modifier = Modifier.padding(
                        horizontal = 24.dp,
                        vertical = 4.dp
                    )
                )
            }
        }
    }
}