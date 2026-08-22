package com.discord2.app

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.discord2.app.ui.AddFriendFragment
import com.discord2.app.ui.FriendsFragment
import com.discord2.app.ui.ProfileFragment
import com.discord2.app.ui.RequestsFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottomNav)

        if (savedInstanceState == null) {
            openFragment(FriendsFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_friends -> {
                    openFragment(FriendsFragment())
                    true
                }
                R.id.nav_requests -> {
                    openFragment(RequestsFragment())
                    true
                }
                R.id.nav_add -> {
                    openFragment(AddFriendFragment())
                    true
                }
                R.id.nav_profile -> {
                    openFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }

    private fun openFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
}
