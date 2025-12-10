package com.example.studenthub.ui.profile

import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.studenthub.ui.theme.STUDENTHUBTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    modifier: Modifier = Modifier,
    userId: String? = null,
    onBack: () -> Unit = {}
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var degreeProgram by remember { mutableStateOf("") }
    var university by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    var isGoogleAccount by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val auth: FirebaseAuth = Firebase.auth
    val db: FirebaseFirestore = Firebase.firestore

    LaunchedEffect(userId) {
        if (userId != null) {
            isLoading = true
            // Check if user is logged in via Google to hide password field later
            val currentUser = auth.currentUser
            if (currentUser != null) {
                // Pre-fill with Auth data as fallback
                if (name.isEmpty()) name = currentUser.displayName ?: ""
                if (email.isEmpty()) email = currentUser.email ?: ""

                // Check provider
                isGoogleAccount = currentUser.providerData.any { it.providerId == "google.com" }
            }

            // Load user data from Firestore
            db.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        name = document.getString("name") ?: name // Keep fallback if firestore empty
                        email = document.getString("email") ?: email
                        phone = document.getString("phone") ?: ""
                        degreeProgram = document.getString("degreeProgram") ?: ""
                        university = document.getString("university") ?: ""
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    // Even if loading fails, we might have partial data from Auth
                    isLoading = false
                    Toast.makeText(context, "Error loading profile details", Toast.LENGTH_SHORT).show()
                }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (userId == null) "Sign Up" else "Profile Update") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text("Please enter the details of you below")
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("E-mail") },
                enabled = userId == null, // Disable editing email for existing users
                modifier = Modifier.fillMaxWidth()
            )

            // Show password field only for Sign Up (userId == null) or non-Google accounts
            if (userId == null || !isGoogleAccount) {
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    enabled = userId == null // Disable password edit in update mode for simplicity (usually requires re-auth)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = phone,
                onValueChange = { phone = it },
                label = { Text("Phone") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = degreeProgram,
                onValueChange = { degreeProgram = it },
                label = { Text("Degree Program") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = university,
                onValueChange = { university = it },
                label = { Text("University") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                Button(
                    onClick = {
                        isLoading = true
                        if (userId == null) {
                            // Sign Up Logic
                            if (email.isNotEmpty() && password.isNotEmpty()) {
                                auth.createUserWithEmailAndPassword(email, password)
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val user = auth.currentUser
                                            user?.let {
                                                val profileUpdates = userProfileChangeRequest {
                                                    displayName = name
                                                }
                                                it.updateProfile(profileUpdates).addOnCompleteListener { updateTask ->
                                                    val userData = hashMapOf(
                                                        "name" to name,
                                                        "email" to email,
                                                        "phone" to phone,
                                                        "degreeProgram" to degreeProgram,
                                                        "university" to university
                                                    )
                                                    db.collection("users").document(it.uid)
                                                        .set(userData)
                                                        .addOnSuccessListener {
                                                            isLoading = false
                                                            if(updateTask.isSuccessful) {
                                                                Toast.makeText(context, "Account created successfully", Toast.LENGTH_SHORT).show()
                                                            } else {
                                                                Toast.makeText(context, "Account created, but failed to set display name.", Toast.LENGTH_LONG).show()
                                                            }
                                                            onBack()
                                                        }
                                                        .addOnFailureListener { e ->
                                                            isLoading = false
                                                            Toast.makeText(context, "Error saving user data: ${e.message}", Toast.LENGTH_SHORT).show()
                                                        }
                                                }
                                            }
                                        } else {
                                            isLoading = false
                                            Toast.makeText(context, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                            } else {
                                isLoading = false
                                Toast.makeText(context, "Please enter email and password", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // Update Profile Logic
                            val currentUser = auth.currentUser
                            if (currentUser != null && userId == currentUser.uid) { // Ensure we are updating the logged-in user
                                isLoading = true

                                // Update Firebase Auth display name
                                val profileUpdates = userProfileChangeRequest {
                                    displayName = name
                                }
                                currentUser.updateProfile(profileUpdates).addOnCompleteListener { authUpdateTask ->
                                    val userData = hashMapOf<String, Any>(
                                        "name" to name,
                                        "phone" to phone,
                                        "degreeProgram" to degreeProgram,
                                        "university" to university
                                    )
                                    // Update Firestore
                                    db.collection("users").document(userId)
                                        .set(userData, com.google.firebase.firestore.SetOptions.merge())
                                        .addOnCompleteListener { firestoreTask ->
                                            isLoading = false
                                            if (authUpdateTask.isSuccessful && firestoreTask.isSuccessful) {
                                                Toast.makeText(context, "Profile updated", Toast.LENGTH_SHORT).show()
                                            } else {
                                                Toast.makeText(context, "Failed to update profile completely.", Toast.LENGTH_LONG).show()
                                            }
                                            onBack() // Go back anyway
                                        }
                                }
                            } else {
                                // This case should ideally not happen if logic is correct.
                                Toast.makeText(context, "Cannot update profile. Not authenticated.", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    STUDENTHUBTheme {
        ProfileScreen()
    }
}
