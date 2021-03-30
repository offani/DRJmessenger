package com.dharmikjoshi.drjmessenger

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Parcelable
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.parcel.Parcelize
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

        supportActionBar!!.hide()



        ivInfo.setOnClickListener {

            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        btnRegister_Register.setOnClickListener {
           performRegister()
        }
        tvAlready_have_acc.setOnClickListener {
            val intent = Intent(this , LoginActivity::class.java)
            startActivity(intent)


//animation to go on next activity
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)



        }

        // Add image button code is here

        btnRegister_Image.setOnClickListener {
            setprofilepic()
        }
    }

    // All the functions are below this line --------------------------------------

    private fun setprofilepic(){
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, 0)
    }


    var selectedPhotoUri: Uri? = null


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode==0 && resultCode== Activity.RESULT_OK && data != null)
        {
            //check what the image is selected
            Toast.makeText(this, "Profile Picture Set !" , Toast.LENGTH_SHORT).show()

            selectedPhotoUri = data.data
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver,selectedPhotoUri)
            selectphoto_imageview.setImageBitmap(bitmap)


        //            val bitmapDrawable = BitmapDrawable(bitmap)
        //            btnRegister_Image.setBackgroundDrawable(bitmapDrawable)


        }
    }





    private fun performRegister(){
        val email = etEmail_register.text.toString()
        val password = etPassword_register.text.toString()

        if (email.isEmpty() || password.isEmpty())
        {
            Toast.makeText(this, "Enter your credentials first", Toast.LENGTH_SHORT).show()
        }

        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email,password)
            .addOnCompleteListener {
                if  (!it.isSuccessful) return@addOnCompleteListener

                Log.d("Main", "Registered with uid : ${it.result!!.user!!.uid}")
                UploadImageToFirebaseStorage()
            }
            .addOnFailureListener {
                Toast.makeText(this, "${it.message}", Toast.LENGTH_SHORT).show()
            }
    }



    private fun UploadImageToFirebaseStorage(){
        if (selectedPhotoUri== null)return

        val filename = UUID.randomUUID().toString()
        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")
        ref.putFile(selectedPhotoUri!!)
            .addOnSuccessListener {
                ref.downloadUrl.addOnSuccessListener {
                    it.toString()

                    SaveuserToDatabase(it.toString())
                }
            }
            .addOnFailureListener{
                Toast.makeText(this, "Failed to Upload Image, Please Try Again ", Toast.LENGTH_SHORT).show()
            }

    }

    private fun SaveuserToDatabase(profileImageUrl: String) {
        val uid = FirebaseAuth.getInstance().uid?: ""
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        val user = User(uid,etUsername_register.text.toString(), profileImageUrl)

        ref.setValue(user)
            .addOnSuccessListener {
                Toast.makeText(this, "Successfully Registered !", Toast.LENGTH_SHORT).show()

                // go to message page
                val intent = Intent(this, LatestMessagesActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)


            }
            .addOnFailureListener {
                Toast.makeText(this, "Something went wrong ! PLease check your Internet Connection ", Toast.LENGTH_SHORT).show()
            }

    }



}
@Parcelize
class User(val uid : String, val username : String , val profileImageUrl: String) : Parcelable
{
    constructor() : this("","","")
}