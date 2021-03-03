package com.group1.movebetter.bird_dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.group1.movebetter.databinding.BirdDialogBinding
import com.group1.movebetter.databinding.BirdDialogBinding.inflate
import com.group1.movebetter.repository.Repository


class BirdDialog : AppCompatDialogFragment() {
    var email: String = ""
    private val REQUEST_CODE_EMAIL = 1

    private lateinit var binding: BirdDialogBinding
    private lateinit var googleButton: SignInButton
    private lateinit var birdDialogViewModel: BirdDialogViewModel
    private lateinit var repository : Repository

    @SuppressLint("MissingPermission")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)

        repository = Repository()
        birdDialogViewModel = BirdDialogViewModel(repository)

        binding.lifecycleOwner = this

       googleButton = binding.googleButton
        setGooglePlusButtonText(googleButton, "Weiter mit Google")
        googleButton.setOnClickListener {
            signInWithGoogle()
        }

        builder.setView(binding.root)
            .setTitle("Nutzung von Bird")
            .setNegativeButton(
                "skip"
            ) { _, _ -> }
            .setPositiveButton(
                "send"
            ) { _, _ ->
                if (binding.enterEmail.text.isNotEmpty()) {
                    email = binding.enterEmail.text.toString()
                }
                send()
            }


        return builder.create()
    }

    private fun send() {
        Log.d("Email", email)
        // TODO: send email to bird-auth-api
    }

    private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_EMAIL)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_EMAIL) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            handleSignInResult(task)
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            val account: GoogleSignInAccount = task!!.getResult(ApiException::class.java)

            // Signed in successfully, show authenticated UI.
            email = account.email.toString()
            send()
        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.d("Sign in Error", "signInResult:failed code=" + e.statusCode)
        }
    }

    private fun setGooglePlusButtonText(signInButton: SignInButton, buttonText: String?) {
        // Find the TextView that is inside of the SignInButton and set its text
        for (i in 0 until signInButton.childCount) {
            val v: View = signInButton.getChildAt(i)
            if (v is TextView) {
                v.text = buttonText
                return
            }
        }
    }
}