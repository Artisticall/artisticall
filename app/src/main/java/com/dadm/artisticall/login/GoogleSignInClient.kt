package com.dadm.artisticall.login

import android.content.Context
import android.content.res.Resources
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.dadm.artisticall.R
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleSignInClient (
    private val context: Context,
){
    private val tag = "GoogleSingInClient: "

    private val credentialManager = CredentialManager.create(context)
    private val firebaseAuth = FirebaseAuth.getInstance()

    private fun isSignedIn(): Boolean{
        return false
    }

    suspend fun signIn(): Boolean{
        if (isSignedIn()) {
            if (firebaseAuth.currentUser != null) {
                Log.d(tag, "Already signed in")
                return true
            }
            return false
        }
        try {
            val result = getCredentialRequest()
            return handleSingIn(result)

        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            Log.d(tag, "Error: ${e.localizedMessage}")
            return false
        }
    }

    private suspend fun handleSingIn(result: GetCredentialResponse): Boolean {
        val credential = result.credential

        if (
            credential is CustomCredential &&
            credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {

            try {

                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                Log.d(tag, "name: ${tokenCredential.displayName}")
                Log.d(tag, "email: ${tokenCredential.id}")
                Log.d(tag, "image: ${tokenCredential.profilePictureUri}")

                val authCredential = GoogleAuthProvider.getCredential(
                    tokenCredential.idToken, null
                )
                val authResult = firebaseAuth.signInWithCredential(authCredential).await()

                return authResult.user != null

            } catch (e: GoogleIdTokenParsingException) {
                println(tag + "GoogleIdTokenParsingException: ${e.message}")
                return false
            }

        } else {
            println(tag + "credential is not GoogleIdTokenCredential")
            return false
        }

    }

    private suspend fun getCredentialRequest(): GetCredentialResponse{
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(
                        context.getString(R.string.client_id)
                    )
                    .setAutoSelectEnabled(false)
                    .build()
            )
            .build()
        return credentialManager.getCredential(
            request = request, context = context
        )
    }


    suspend fun signOut(){
        credentialManager.clearCredentialState(
            ClearCredentialStateRequest()
        )
    }
}