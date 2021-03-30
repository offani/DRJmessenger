package com.dharmikjoshi.drjmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_from_row.view.*
import kotlinx.android.synthetic.main.chat_to_row.view.*
import kotlinx.android.synthetic.main.user_row_new_message.view.*

class ChatLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)
        rVChat_Log.adapter = adapter     // adds the message to the RV layout



        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        supportActionBar?.title= user.username




        //listen for new messages
        listenForMessages()

        btnSend.setOnClickListener {
            Log.d(TAG,"Pressed the send button")
            performSendMessage()

        }

    }

    private fun listenForMessages() {
        val toId = FirebaseAuth.getInstance().uid
        val fromuser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromId =fromuser.uid
        val ref  = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId")
        ref.addChildEventListener(object : ChildEventListener{

            //all the child functions
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatMessage::class.java)

                if (chatMessage !=null) {
                    if (chatMessage.toId == FirebaseAuth.getInstance().uid){

                        val currentUser = LatestMessagesActivity.currentuser ?: return

                        adapter.add(ChatToItem(chatMessage.typedmessage, currentUser))


                    }else{


                        val fromuser = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
                        adapter.add(ChatFromItem(chatMessage.typedmessage,fromuser))
                    }
                    Log.d(TAG, chatMessage.typedmessage)


                }

                rVChat_Log.scrollToPosition(adapter.itemCount -1)
            }

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

            }

            override fun onChildRemoved(snapshot: DataSnapshot) {

            }
        })
    }



    class ChatMessage(val id :String,val typedmessage :String, val toId :String, val fromId : String, val timestamp : Long)
    {
        constructor() : this("","","","",-1)
    }

    private fun performSendMessage() {
        val toId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMessageActivity.USER_KEY)
        val fromId = user.uid

        if (toId==null) return

        val typedmessage  = editTextMessage.text.toString()

        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()
        val fromreference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()



        val chatMessage = ChatMessage(reference.key!!, typedmessage, toId , fromId , System.currentTimeMillis() / 1000 )
        reference.setValue(chatMessage)
            .addOnSuccessListener {
                Log.d(TAG,"Message sent : ${reference.key}")
                editTextMessage.text.clear()
                rVChat_Log.scrollToPosition(adapter.itemCount -1)
            }
        fromreference.setValue(chatMessage)


        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageRef.setValue(chatMessage)


        val latestMessageToRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessageToRef.setValue(chatMessage)


    }
}
//Chat item from another person
class ChatFromItem(val dummymessage : String, val user: User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.tvFrom_msg.text = dummymessage
        //load the users image
        val uri = user.profileImageUrl
        val targetImageView =  viewHolder.itemView.ivchat_from_row
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
    return R.layout.chat_from_row
    }
}

//chat item from me
class ChatToItem(val dummymessage : String, val user: User) : Item<GroupieViewHolder>(){
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.tvTo_msg.text = dummymessage
        //load the users image
        val uri = user.profileImageUrl
        val targetImageView =  viewHolder.itemView.ivchat_to_row
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chat_to_row
    }
}