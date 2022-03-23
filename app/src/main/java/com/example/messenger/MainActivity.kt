package com.example.messenger

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.fragment.app.Fragment
import com.example.fragments.ChatFragment
import com.example.fragments.ExploreFragment
import com.example.fragments.FriendsFragment
import com.example.messenger.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationBarView
import com.google.android.material.navigation.NavigationView

class MainActivity : AppCompatActivity() {

    lateinit var binding:ActivityMainBinding

    lateinit var chatFragment:ChatFragment
    lateinit var friendsFragment:FriendsFragment
    lateinit var exploreFragment: ExploreFragment

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        chatFragment = ChatFragment()
        friendsFragment = FriendsFragment()
        exploreFragment = ExploreFragment()

        onClickItemNavbar()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
        }
        else
        {

        }

    }

    fun onClickItemNavbar(){
        binding.buChat.setOnClickListener {
            setFragment(chatFragment)
            binding.buChat.setImageResource(R.drawable.ic_chat_selected)
            binding.buFriends.setImageResource(R.drawable.ic_friends_unselected)
            binding.buExplore.setImageResource(R.drawable.ic_expolre_unselected)
        }

        binding.buFriends.setOnClickListener {
            setFragment(friendsFragment)
            binding.buChat.setImageResource(R.drawable.ic_chat_unselected)
            binding.buFriends.setImageResource(R.drawable.ic_friends_selected)
            binding.buExplore.setImageResource(R.drawable.ic_expolre_unselected)
        }

        binding.buExplore.setOnClickListener {
            setFragment(exploreFragment)
            binding.buChat.setImageResource(R.drawable.ic_chat_unselected)
            binding.buFriends.setImageResource(R.drawable.ic_friends_unselected)
            binding.buExplore.setImageResource(R.drawable.ic_expolre_selected)
        }
    }

    private fun setFragment(fragment: Fragment) {
        val fr= supportFragmentManager.beginTransaction()
        fr.replace(R.id.fragment_container ,fragment)
        fr.commit()
    }


}