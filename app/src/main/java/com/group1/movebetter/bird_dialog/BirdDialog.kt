package com.group1.movebetter.bird_dialog

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatDialogFragment
import androidx.constraintlayout.widget.ConstraintSet
import androidx.lifecycle.ViewModelProvider
import com.group1.movebetter.database.getDatabase
import com.group1.movebetter.databinding.BirdDialogBinding
import com.group1.movebetter.databinding.BirdDialogBinding.inflate
import com.group1.movebetter.repository.Repository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class BirdDialog : AppCompatDialogFragment() {
    var email: String = ""
    private var tokenFlag: Boolean = false

    private lateinit var alertDialog: AlertDialog
    private lateinit var binding: BirdDialogBinding
    private lateinit var birdDialogViewModel: BirdDialogViewModel
    private lateinit var repository : Repository

    @SuppressLint("MissingPermission")
    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        binding = inflate(LayoutInflater.from(context))
        val builder = AlertDialog.Builder(context)

        val db = getDatabase(context!!)
        var uuid = ""
        runBlocking {
            launch(Dispatchers.IO) {
                uuid = db.databaseDevUuidDao.getDevUuid("1").uuid
            }.join()
        }
        repository = Repository(db, uuid)
        val birdDialogViewModelFactory = BirdDialogViewModelFactory(repository)
        birdDialogViewModel = ViewModelProvider(this, birdDialogViewModelFactory).get(BirdDialogViewModel::class.java)

        binding.lifecycleOwner = this

        builder.setView(binding.root)
            .setTitle("Nutzung von Bird")
            .setNegativeButton("skip") {d, _ -> d.dismiss()}
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
}