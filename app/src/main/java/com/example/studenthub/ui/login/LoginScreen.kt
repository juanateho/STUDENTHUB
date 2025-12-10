package com.example.studenthub.ui.login

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studenthub.R
import com.example.studenthub.ui.theme.STUDENTHUBTheme
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    modifier: Modifier = Modifier,
    onLoginClick: () -> Unit = {},
    onSignUpClick: () -> Unit = {}
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore
    val scope = rememberCoroutineScope()

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            try {
                val account = task.getResult(ApiException::class.java)
                val idToken = account.idToken
                if (idToken != null) {
                    val credential = GoogleAuthProvider.getCredential(idToken, null)
                    isLoading = true
                    auth.signInWithCredential(credential)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                // Usuario autenticado correctamente
                                val user = auth.currentUser
                                if (user != null) {
                                    val userRef = db.collection("users").document(user.uid)
                                    // Intentamos leer/crear el documento en Firestore
                                    userRef.get()
                                        .addOnSuccessListener { document ->
                                            if (!document.exists()) {
                                                // Si el usuario no existe en la BD, lo creamos
                                                val userData = hashMapOf(
                                                    "name" to (user.displayName ?: ""),
                                                    "email" to (user.email ?: ""),
                                                    "phone" to "",
                                                    "degreeProgram" to "",
                                                    "university" to ""
                                                )
                                                userRef.set(userData)
                                                    .addOnCompleteListener {
                                                        // Terminamos (haya o no error en la BD, el login fue exitoso)
                                                        isLoading = false
                                                        onLoginClick()
                                                    }
                                            } else {
                                                // El usuario ya existe, procedemos
                                                isLoading = false
                                                onLoginClick()
                                            }
                                        }
                                        .addOnFailureListener { e ->
                                            // Si falla Firestore (ej. permisos), igual dejamos pasar al usuario
                                            // porque la autenticación fue exitosa.
                                            isLoading = false
                                            onLoginClick()
                                        }
                                } else {
                                    // Fallback raro si user es null
                                    isLoading = false
                                    onLoginClick()
                                }
                            } else {
                                isLoading = false
                                errorMessage = authTask.exception?.message ?: "Authentication failed."
                            }
                        }
                } else {
                    errorMessage = "Google Sign-In error: No ID Token"
                }
            } catch (e: ApiException) {
                errorMessage = "Google Sign-In failed: ${e.statusCode}"
            }
        }
    }

    LaunchedEffect(errorMessage) {
        errorMessage?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            errorMessage = null
        }
    }

    Scaffold {
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(it)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Filled.AccountCircle,
                contentDescription = "StudentHub Logo",
                modifier = Modifier.size(100.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Text(
                text = "STUDENTHUB",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.padding(bottom = 32.dp)
            )
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            if (isLoading) {
                CircularProgressIndicator()
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    FilledTonalButton(onClick = onSignUpClick, modifier = Modifier.weight(1f)) {
                        Text("Sign Up")
                    }
                    Button(
                        onClick = {
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                isLoading = true
                                auth.signInWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        isLoading = false
                                        if (task.isSuccessful) {
                                            onLoginClick()
                                        } else {
                                            errorMessage = task.exception?.message ?: "Authentication failed."
                                        }
                                    }
                            } else {
                                errorMessage = "Please enter email and password"
                            }
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Log In")
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedButton(
                    onClick = {
                        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(context.getString(R.string.default_web_client_id))
                            .requestEmail()
                            .build()
                        val googleSignInClient = GoogleSignIn.getClient(context, gso)
                        googleSignInLauncher.launch(googleSignInClient.signInIntent)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Continue with Google")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    STUDENTHUBTheme {
        LoginScreen()
    }
}
