package com.group1.movebetter.bird_dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.view.marginRight
import androidx.core.view.setMargins
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.group1.movebetter.R
import com.group1.movebetter.database.getDatabase
import com.group1.movebetter.databinding.BirdDialogBinding
import com.group1.movebetter.databinding.BirdDialogBinding.inflate
import com.group1.movebetter.repository.Repository

class BirdDialog : AppCompatDialogFragment() {
    var email: String = ""
    private val REQUEST_CODE_EMAIL = 1
    private var tokenFlag: Boolean = false

    private lateinit var alertDialog: AlertDialog
    private lateinit var binding: BirdDialogBinding
    private lateinit var googleButton: SignInButton
    private lateinit var birdDialogViewModel: BirdDialogViewModel
    private lateinit var repository : Repository

    @SuppressLint("MissingPermission")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)

        repository = Repository(getDatabase(context!!))
        birdDialogViewModel = BirdDialogViewModel(repository)

        binding.lifecycleOwner = this

//        googleButton = binding.googleButton
//        setGooglePlusButtonText(googleButton, "Weiter mit Google")
//        googleButton.setOnClickListener {
//            signInWithGoogle()
//        }

        builder.setView(binding.root)
            .setTitle("Nutzung von Bird")
            .setNegativeButton("skip", null)
            .setPositiveButton("send", null)

        alertDialog = builder.show()
        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (tokenFlag) {
                if (binding.enterEmail.text.isNotEmpty()) {
                    val magicToken = binding.enterEmail.text.toString()
                    verify(magicToken)
                    dismiss()
                }
            } else {
                if (!binding.enterEmail.text.matches(Regex("[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"))) {
                    binding.errorText.visibility = View.VISIBLE
                } else {
                    email = binding.enterEmail.text.toString()
                    binding.enterEmail.setText("")
                    send()
                }
            }
        }

        return alertDialog
    }

    private fun verify(magicToken: String?) {
        if (magicToken != null) {
            birdDialogViewModel.birdController.postAuthToken(magicToken)
        }
    }

    private fun changeToMagicToken() {
        binding.containerConstraint.removeAllViewsInLayout()

        val textView = binding.textView
        textView.text = "Deine Email-Adresse wurde zu Bird gesendet." +
                " Du erhälst jetzt gleich eine Mail wo ein Token drin steht." +
                " Kopier es und füge es hier ein."
        binding.containerConstraint.addView(textView)

        val input = binding.enterEmail
        input.hint = "magic token"
        binding.containerConstraint.addView(input)

        val set = ConstraintSet()
        set.clone(binding.containerConstraint)

        val margin = 32
        set.constrainHeight(textView.id, ConstraintSet.WRAP_CONTENT)
        set.constrainWidth(textView.id, ConstraintSet.MATCH_CONSTRAINT)

        set.connect(textView.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, margin)
        set.connect(textView.id, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, margin)
        set.connect(textView.id, ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, margin)
        set.connect(textView.id, ConstraintSet.BOTTOM, input.id, ConstraintSet.TOP, margin*2)

        set.constrainHeight(input.id, ConstraintSet.WRAP_CONTENT)
        set.constrainWidth(input.id, ConstraintSet.WRAP_CONTENT)

        set.connect(input.id, ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, margin)
        set.connect(input.id, ConstraintSet.RIGHT,ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, margin)
        set.connect(input.id, ConstraintSet.BOTTOM, ConstraintSet.PARENT_ID, ConstraintSet.BOTTOM, margin)

        set.applyTo(binding.containerConstraint)
    }

    private fun send() {
        birdDialogViewModel.birdController.getAuthToken(email)
        tokenFlag = true
        changeToMagicToken()
    }

    /*private fun signInWithGoogle() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.web_token))
            .requestEmail()
            .build()

        val mGoogleSignInClient = GoogleSignIn.getClient(context!!, gso)
        val signInIntent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, REQUEST_CODE_EMAIL)
    }*/

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