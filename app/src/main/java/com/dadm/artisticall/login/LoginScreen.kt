package com.dadm.artisticall.login

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(){
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
        if (isSignedIn) {
            OutlinedButton(onClick = {
                lifecycleOwner.lifecycleScope.launch {
                    googleSignInClient.signOut()
                    isSignedIn = false
                }
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
        } else {
            OutlinedButton(onClick = {
                lifecycleOwner.lifecycleScope.launch {
                    isSignedIn = googleSignInClient.signIn()
                }
            }) {
                Text(
                    text = "Sign In with Google",
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