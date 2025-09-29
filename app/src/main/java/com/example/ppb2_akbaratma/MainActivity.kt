package com.example.ppb2_akbaratma

import android.os.Bundle
import android.renderscript.ScriptGroup.Binding
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import androidx.lifecycle.lifecycleScope
import com.example.ppb2_akbaratma.databinding.ActivityMainBinding
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthCredential
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {
    // bikin binding dari main activity
    private lateinit var binding: ActivityMainBinding
    private lateinit var credentialManager: CredentialManager
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        // inisiasi binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        // set content dari binding
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        credentialManager = CredentialManager.create(this)
        auth = Firebase.auth

        // daftar event yg diperlukan
        registerEvent()
    }


    fun registerEvent (){
        binding.btnLogin.setOnClickListener {
            lifecycleScope.launch {
                val request= prepareRequest()
                loginByGoogle(request)
            }
        }
    }

    fun prepareRequest(): GetCredentialRequest{
        val serverClient = "997882886667-n3c56i9ms3nibmrv4ofu8ue0ag6td2e9.apps.googleusercontent.com"

        val googleOption = GetGoogleIdOption
            .Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClient)
            .build()

        val request = GetCredentialRequest
            .Builder()
            .addCredentialOption(googleOption)
            .build()

        return request
    }


    suspend fun loginByGoogle(request: GetCredentialRequest){
        try {
            val result = credentialManager.getCredential(
                context = this,
                request = request
            )
            val credentials = result.credential
            val idToken = GoogleIdTokenCredential.createFrom(credentials.data)

            firebaseLoginCallback(idToken.idToken)

        } catch (exc: NoCredentialException) {
            Toast.makeText(this, "Login gagal :" + exc.message, Toast.LENGTH_LONG).show()
        } catch (exc: Exception) {
            Toast.makeText(this, "Login gagal :" + exc.message, Toast.LENGTH_LONG).show()
        }
    }

    fun firebaseLoginCallback(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) {task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Login berhasil", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(this, "Login gagal", Toast.LENGTH_LONG).show()
                }
            }
    }

}