package com.example.demo_chat

import android.app.ProgressDialog
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Binder
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import com.example.demo_chat.databinding.ActivityLoginBinding
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    //view binding
    private lateinit var binding: ActivityLoginBinding

    //if code failed, will used to resend
    private lateinit var resendingToken: PhoneAuthProvider.ForceResendingToken

    private lateinit var mCallBack: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private lateinit var mVerificationId: String
    private lateinit var firebaseAuth: FirebaseAuth

    private val TAG = "masrur"

    private lateinit var progressDialog: ProgressDialog



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.phoneLl.visibility = View.VISIBLE
        binding.codeLl.visibility = View.GONE

        //init firebase auth
        firebaseAuth = FirebaseAuth.getInstance()

        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        mCallBack = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks(){
            override fun onVerificationCompleted(phoneAuth: PhoneAuthCredential) {

                Log.d(TAG, "onVerificationCompleted: ")
                
                signInWithPhoneAuthCredential(phoneAuth)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                progressDialog.dismiss()
                Log.d(TAG, "onVerificationFailed: {${e.message}}")
                Toast.makeText(this@LoginActivity,"${e.message}", Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                Log.d(TAG,"onCodeSent: $verificationId")
                mVerificationId = verificationId
                resendingToken = token
                progressDialog.dismiss()

                Log.d(TAG, "onCodeSent: $verificationId")

                //hide phone layot show code layout
                binding.phoneLl.visibility = View.GONE
                binding.codeLl.visibility = View.VISIBLE

                Toast.makeText(this@LoginActivity,"Verification code sent", Toast.LENGTH_SHORT).show()
                binding.codeSent.text = "Please type the verification code we sent to ${binding.phoneEt.text.toString().trim()}"

            }

        }

        //phoneNextbtn click

        binding.phoneNextBtn.setOnClickListener {
            val phone = binding.phoneEt.toString().trim()
            //validate phone number
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(this@LoginActivity,"Please enter phone number", Toast.LENGTH_SHORT).show()
            } else {
                startPhoneNumberVerification(phone)
            }

        }

        //resendCode click: OTP
        binding.resendCodeTv.setOnClickListener {
            val phone = binding.phoneEt.toString().trim()
            //validate phone number
            if (TextUtils.isEmpty(phone)){
                Toast.makeText(this@LoginActivity,"Please enter phone number", Toast.LENGTH_SHORT).show()
            } else {
                resendVerificationCode(phone,resendingToken)
            }
        }

        //codeSubmitBtn click: input verification code
        binding.codeSubmitBtn.setOnClickListener {
            //inpute verification code
            val code = binding.codeEt.text.toString().trim()
            if (TextUtils.isEmpty(code)) {
                Toast.makeText(this@LoginActivity, "Please enter verification code", Toast.LENGTH_SHORT).show()
            } else {
                verificationNumberWithCode(mVerificationId,code)
            }
        }
    }

    private fun startPhoneNumberVerification(phone: String) {
        Log.d(TAG, "startPhoneNumberVerification: $phone")
        progressDialog.setMessage("Verification Phone Number...")
        progressDialog.show()

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBack)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun resendVerificationCode(phone: String, token: PhoneAuthProvider.ForceResendingToken){
        progressDialog.setMessage("Resending Code...")
        progressDialog.show()

        Log.d(TAG, "resendVerificationCode: $phone")

        val options = PhoneAuthOptions.newBuilder(firebaseAuth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(mCallBack)
            .setForceResendingToken(token)
            .build()

        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private fun verificationNumberWithCode(verificationId: String,code:String){
        Log.d(TAG, "verificationNumberWithCode: $verificationId $code")
        progressDialog.setMessage("Verification code...")
        progressDialog.show()

        val credential = PhoneAuthProvider.getCredential(verificationId,code)
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        Log.d(TAG, "signInWithPhoneAuthCredential: ")
        progressDialog.setMessage("Logging In")

        firebaseAuth.signInWithCredential(credential)
            .addOnSuccessListener {
               //login success
                progressDialog.dismiss()
                val phone = firebaseAuth.currentUser!!.phoneNumber
                Toast.makeText(this,"Logged In as $phone",Toast.LENGTH_SHORT).show()

                //start main activity
                startActivity(Intent(this,MainActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                //login failed
                progressDialog.dismiss()
                Toast.makeText(this,"${it.message}", Toast.LENGTH_SHORT).show()
            }

    }
}