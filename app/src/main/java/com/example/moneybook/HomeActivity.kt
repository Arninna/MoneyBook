package com.example.moneybook

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.FrameLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.moneybook.databinding.ActivityHomeBinding
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener  {

    private lateinit var binding: ActivityHomeBinding

    private lateinit var bottomNavView: BottomNavigationView
    private lateinit var frameLayout: FrameLayout

    //Fragment
    private lateinit var dashBoardFragment: DashboardFragment
    private lateinit var incomeFragment: IncomeFragment
    private lateinit var expenseFragment: ExpenseFragment

    //Firebase per logout
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)


        val toolbar: Toolbar = findViewById(R.id.my_toolbar)
        toolbar.setTitle("Expense Manager")
        setSupportActionBar(toolbar)

        //Prendo sessione attiva dell'utente loggato
        firebaseAuth=FirebaseAuth.getInstance()

        //bottomNavView = findViewById(R.id.bottomNavigationBar)
        //frameLayout = findViewById(R.id.mainFrame)
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        val toggle: ActionBarDrawerToggle = ActionBarDrawerToggle(this,drawerLayout,
            toolbar,R.string.navigation_drawer_open,R.string.navigation_drawer_close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView: NavigationView = binding.navView
        navigationView.setNavigationItemSelectedListener(this)

        //inizializzo i fragment
        dashBoardFragment = DashboardFragment()
        incomeFragment = IncomeFragment()
        expenseFragment = ExpenseFragment()
        //fragment di default
        setFragment(dashBoardFragment)

        bottomNavView = findViewById(R.id.bottomNavigationBar)
        frameLayout = findViewById(R.id.mainFrame)
        bottomNavView.setOnNavigationItemSelectedListener { menuItem ->
                when(menuItem.itemId){
                    R.id.dashboard -> {
                        setFragment(dashBoardFragment)
                        bottomNavView.setBackgroundResource(R.color.dashboard_color)
                        true
                    }
                    R.id.income -> {
                        setFragment(incomeFragment)
                        bottomNavView.setBackgroundResource(R.color.income_color)
                        true
                    }
                    R.id.expense -> {
                        setFragment(expenseFragment)
                        bottomNavView.setBackgroundResource(R.color.expense_color)
                        true
                    }
                    else -> false
                }
        }
    }

    @SuppressLint("CommitTransaction")
    private fun setFragment(fragment: Fragment) {
        var fragmentTransaction: FragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.mainFrame,fragment)
        fragmentTransaction.commit()
    }


    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val drawerLayout: DrawerLayout =findViewById(R.id.drawer_layout)
        if(drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawer(GravityCompat.END)
        else
            super.onBackPressedDispatcher.onBackPressed()
        super.onBackPressed()
    }

    private fun displaySelectedListener(itemId: Int){
        var fragment: Fragment? = null
        when(itemId){
            R.id.dashboard ->{
                fragment = DashboardFragment()
            }
            R.id.income ->{
                fragment = IncomeFragment()
            }
            R.id.expense ->{
                fragment = ExpenseFragment()
            }
            R.id.statistics->{
                fragment = StatisticFragment()
            }
            R.id.logout ->{
                firebaseAuth.signOut()
                val intent = Intent(this,LoginActivity::class.java)
                startActivity(intent)
            }
        }
        if(fragment != null){
            supportFragmentManager.beginTransaction()
                .replace(R.id.mainFrame, fragment)
                .commit()
        }
        val drawerLayout: DrawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.closeDrawer(GravityCompat.START)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        displaySelectedListener(item.itemId)
        return true
    }
}