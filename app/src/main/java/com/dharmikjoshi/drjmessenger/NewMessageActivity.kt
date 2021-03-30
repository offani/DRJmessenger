package com.dharmikjoshi.drjmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class NewMessageActivity : AppCompatActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_message)

            supportActionBar?.title="Select User"


            //adapter settings
            val adapter = GroupAdapter<GroupieViewHolder>()
            rvNew_Message.adapter = adapter
            fetchusers()


    }

    companion object{
        val USER_KEY = "USER_KEY"
    }


    private fun fetchusers(){
       val ref= FirebaseDatabase.getInstance().getReference("/users")
       ref.addListenerForSingleValueEvent(object : ValueEventListener {

           override fun onDataChange(snapshot: DataSnapshot) {
               // executes whenever all the users are called
//create new adapter
               val adapter = GroupAdapter<GroupieViewHolder>()




               snapshot.children.forEach {
                   Log.d("NewMessage", it.toString())
                   val user = it.getValue(User::class.java)
                   if (user != null) {
                       adapter.add(UserItem(user))
                   }
               }
//click on any user
                   adapter.setOnItemClickListener { item, view ->
                        val userItem = item as UserItem

                       val intent = Intent(view.context , ChatLogActivity::class.java)
                       intent.putExtra(USER_KEY,userItem.user)
                       startActivity(intent)
                       overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                       finish()

                   }


               rvNew_Message.adapter = adapter
               
           }

           override fun onCancelled(error: DatabaseError) {
           }
       })
    }


}



///all the users are here
class UserItem(val user : User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {


    viewHolder.itemView.tv_username_new_message.text = user.username
        Picasso.get().load(user.profileImageUrl).into(viewHolder.itemView.ivLatest_Message_row)

    }

    override fun getLayout(): Int {
    return R.layout.user_row_new_message
    }
}