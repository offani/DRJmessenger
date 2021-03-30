package com.dharmikjoshi.drjmessenger

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item

import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_latest_messages.*
import kotlinx.android.synthetic.main.latest_message_row.view.*

class LatestMessagesActivity : AppCompatActivity() {



  

    companion object{
        var currentuser : User? = null
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)

        rv_latest_messages.adapter = adapter
        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserLoggedin()

        // set click listener that activatse on clicking the message
        ivinfobtLA.setOnClickListener {

            val intent = Intent(this, InfoActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }

        ivnewmsgbtn.setOnClickListener {
            val intent = Intent(this, NewMessageActivity::class.java)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }

        adapter.setOnItemClickListener { item, view ->

           val intent = Intent(this , ChatLogActivity::class.java)

            val row = item as LatestMessagesRow
      //      row.ChatPartnerUser
           intent.putExtra(NewMessageActivity.USER_KEY, row.ChatPartnerUser)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
        }




    }

    val latestMessagesMap = HashMap<String , ChatLogActivity.ChatMessage>()



    private fun refreshRecyclerViewMessages()
    {
        adapter.clear()
        latestMessagesMap.values.forEach {
            adapter.add(LatestMessagesRow(it))

        }
    }



    private fun listenForLatestMessages() {
        val toId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId")

        ref.addChildEventListener(object  : ChildEventListener {
            override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage

                //refresh the messages view
                refreshRecyclerViewMessages()



            }

            override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                val chatMessage = snapshot.getValue(ChatLogActivity.ChatMessage::class.java) ?: return
                latestMessagesMap[snapshot.key!!] = chatMessage

                //refresh the messages view
                refreshRecyclerViewMessages()



            }
            //useless childs
            override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
            override fun onCancelled(error: DatabaseError) {}
            override fun onChildRemoved(snapshot: DataSnapshot) {}

        })
    }

    //latest messages class
    class LatestMessagesRow (val chatMessage : ChatLogActivity.ChatMessage): Item<GroupieViewHolder>(){
        override fun getLayout(): Int {
            return R.layout.latest_message_row
        }

        //amking a user object
        var ChatPartnerUser: User? = null

        override fun bind(viewHolder: GroupieViewHolder, position: Int) {
                viewHolder.itemView.tvLatest_Message.text = chatMessage.typedmessage

                val ChatPartnerID :String

                if (chatMessage.toId== FirebaseAuth.getInstance().uid)
                {
                    ChatPartnerID = chatMessage.fromId
                }else{
                    ChatPartnerID = chatMessage.toId
                }

                val ref = FirebaseDatabase.getInstance().getReference("/users/$ChatPartnerID")
                ref.addListenerForSingleValueEvent(object : ValueEventListener{
                    override fun onDataChange(snapshot: DataSnapshot) {
                        ChatPartnerUser =  snapshot.getValue(User::class.java)
                        viewHolder.itemView.tvusername_latest_message.text = ChatPartnerUser?.username

                      val   targetImageView = viewHolder.itemView.ivLatest_Message_row
                        Picasso.get().load(ChatPartnerUser?.profileImageUrl).into(targetImageView)


                    }
                    override fun onCancelled(error: DatabaseError) {

                    }
                })


        }
    }
    val adapter = GroupAdapter<GroupieViewHolder>()
//    private fun setupDummyRows() {
//
//        adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//
//    }

    private fun fetchCurrentUser() {
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object : ValueEventListener{

            override fun onDataChange(snapshot: DataSnapshot) {
                currentuser = snapshot.getValue(User::class.java)
                Log.d("LatestMessages","Current User ${currentuser?.username}" )
            }
            override fun onCancelled(error: DatabaseError) {

            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){

            R.id.menu_new_message->{
                val intent = Intent(this , NewMessageActivity::class.java)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
            }

            R.id.menu_sign_out->{
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this,RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
                overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu ,menu)
        return super.onCreateOptionsMenu(menu)
    }



    private fun verifyUserLoggedin(){
        val uid = FirebaseAuth.getInstance().uid
        if (uid==null){
            val intent = Intent(this,RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
            overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)

        }
    }
}

