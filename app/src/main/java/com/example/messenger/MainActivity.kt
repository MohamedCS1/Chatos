package com.example.messenger

import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.fragments.ChatFragment
import com.example.fragments.ExploreFragment
import com.example.fragments.FriendsFragment
import com.example.messenger.databinding.ActivityMainBinding
import com.example.pojo.User
import com.example.sharedPreferences.AppSharedPreferences
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import de.hdodenhof.circleimageview.CircleImageView

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding

    lateinit var chatFragment:ChatFragment
    lateinit var friendsFragment:FriendsFragment
    lateinit var exploreFragment: ExploreFragment

    lateinit var navHeaderPhotoProfile:CircleImageView

    lateinit var appPref:AppSharedPreferences

    private val fireStore:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }

    private val currentUserDocRef:DocumentReference get() = fireStore.document("users/${appPref.getCurrentUserUID()}")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        appPref = AppSharedPreferences()

        appPref.PrefManager(this)

        val navView = findViewById<NavigationView>(R.id.nav_view).getHeaderView(0)

        navHeaderPhotoProfile = navView.findViewById(R.id.nav_header_photoProfile)

        val navHeadEmail = navView.findViewById<TextView>(R.id.nav_header_email)

        navHeadEmail.text = appPref.getUserEmail()

        currentUserDocRef.get().addOnSuccessListener {
            appPref.insertCurrentUserName(it.toObject(User::class.java)!!.name)
        }
        binding.buSearch.setOnClickListener {
            startActivity(Intent(this ,SearchActivity::class.java))
            overridePendingTransition(0 ,0)
        }


        retrieveImageFromStorage()

        chatFragment = ChatFragment()
        friendsFragment = FriendsFragment()
        exploreFragment = ExploreFragment()

        onClickItemNavbar()

        binding.buMenu.setOnClickListener {
            binding.drawerLayout.open()
        }

        binding.profileImage.setOnClickListener {
            startActivity(Intent(this ,ProfileActivity::class.java))
        }

    }

    override fun onBackPressed() {
        if (binding.drawerLayout.isOpen)
        {
            binding.drawerLayout.close()
            return
        }
        super.onBackPressed()
    }


    fun retrieveImageFromStorage()
    {

        Glide.with(this).load(appPref.getProfileImagePath()).apply(RequestOptions.overrideOf(600,600)).placeholder(R.drawable.ic_photo_placeholder).into(navHeaderPhotoProfile)
        Glide.with(this).load(appPref.getProfileImagePath()).apply(RequestOptions.overrideOf(600,600)).placeholder(R.drawable.ic_photo_placeholder).into(binding.profileImage)
    }

    fun onClickItemNavbar(){

        setFragment(chatFragment)
        binding.buChat.setImageResource(R.drawable.ic_chat_selected)
        binding.buFriends.setImageResource(R.drawable.ic_friends_unselected)
        binding.buExplore.setImageResource(R.drawable.ic_explore_unselected)
        binding.tvTitleToolbar.text = "Chats"

        binding.buChat.setOnClickListener {
            setFragment(chatFragment)
            binding.buChat.setImageResource(R.drawable.ic_chat_selected)
            binding.buFriends.setImageResource(R.drawable.ic_friends_unselected)
            binding.buExplore.setImageResource(R.drawable.ic_explore_unselected)
            binding.tvTitleToolbar.text = "Chats"
        }

        binding.buFriends.setOnClickListener {
            setFragment(friendsFragment)
            binding.buChat.setImageResource(R.drawable.ic_chat_unselected)
            binding.buFriends.setImageResource(R.drawable.ic_friends_selected)
            binding.buExplore.setImageResource(R.drawable.ic_explore_unselected)
            binding.tvTitleToolbar.text = "Friends"
        }

        binding.buExplore.setOnClickListener {
            setFragment(exploreFragment)
            binding.buChat.setImageResource(R.drawable.ic_chat_unselected)
            binding.buFriends.setImageResource(R.drawable.ic_friends_unselected)
            binding.buExplore.setImageResource(R.drawable.ic_explore_selected)
            binding.tvTitleToolbar.text = "People You May Know"
        }
    }

    override fun onStart() {
        retrieveImageFromStorage()
        super.onStart()
    }

    private fun setFragment(fragment: Fragment) {
        val fr= supportFragmentManager.beginTransaction()
        fr.replace(R.id.fragment_container ,fragment)
        fr.commit()
    }


}