package xyz.sentrionic.harmony.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.android.synthetic.main.activity_auth.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.progress_bar
import xyz.sentrionic.harmony.R
import xyz.sentrionic.harmony.ui.BaseActivity
import xyz.sentrionic.harmony.ui.auth.AuthActivity
import xyz.sentrionic.harmony.ui.main.account.BaseAccountFragment
import xyz.sentrionic.harmony.ui.main.account.ChangePasswordFragment
import xyz.sentrionic.harmony.ui.main.account.UpdateAccountFragment
import xyz.sentrionic.harmony.ui.main.create_story.BaseCreateStoryFragment
import xyz.sentrionic.harmony.ui.main.story.BaseStoryFragment
import xyz.sentrionic.harmony.ui.main.story.ViewStoryFragment
import xyz.sentrionic.harmony.util.BottomNavController
import xyz.sentrionic.harmony.util.setUpNavigation

class MainActivity : BaseActivity(),
    BottomNavController.NavGraphProvider,
    BottomNavController.OnNavigationGraphChanged,
    BottomNavController.OnNavigationReselectedListener {

    private lateinit var bottomNavigationView: BottomNavigationView

    private val bottomNavController by lazy(LazyThreadSafetyMode.NONE) {
        BottomNavController(
            this,
            R.id.main_nav_host_fragment,
            R.id.nav_home,
            this,
            this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setupActionBar()
        bottomNavigationView = findViewById(R.id.bottom_navigation_view)
        bottomNavigationView.setUpNavigation(bottomNavController, this)
        if (savedInstanceState == null) {
            bottomNavController.onNavigationItemSelected()
        }


        subscribeObservers()
    }

    private fun setupActionBar() {
        setSupportActionBar(tool_bar)
    }

    private fun subscribeObservers() {
        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Log.d(TAG, "MainActivity, subscribeObservers: ViewState: ${authToken}")
            if (authToken == null || authToken.account_pk == -1 || authToken.token == null) {
                navAuthActivity()
                finish()
            }
        })
    }

    private fun navAuthActivity() {
        val intent = Intent(this, AuthActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun displayProgressBar(bool: Boolean) {
        progress_bar.visibility = if (bool) View.VISIBLE else View.GONE
    }

    override fun getNavGraphId(itemId: Int) = when(itemId) {
        R.id.nav_home -> {
            R.navigation.nav_story
        }
        R.id.nav_search -> {
            R.navigation.nav_search
        }
        R.id.nav_create_story -> {
            R.navigation.nav_create_story
        }
        R.id.nav_account -> {
            R.navigation.nav_account
        }
        else -> {
            R.navigation.nav_story
        }
    }

    override fun onGraphChange() {
        cancelActiveJobs()
        expandAppBar()
        (this as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(true)
    }

    override fun onReselectNavItem(
        navController: NavController,
        fragment: Fragment
    ) = when(fragment) {

        is ViewStoryFragment -> {
            navController.navigate(R.id.action_viewStoryFragment_to_storyFragment)
        }

        is UpdateAccountFragment -> {
            navController.navigate(R.id.action_updateAccountFragment_to_home)
        }

        is ChangePasswordFragment -> {
            navController.navigate(R.id.action_changePasswordFragment_to_home)
        }

        else -> {
            // do nothing
        }
    }

    override fun onBackPressed() = bottomNavController.onBackPressed()

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId) {
            android.R.id.home -> onBackPressed()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun expandAppBar() {
        findViewById<AppBarLayout>(R.id.app_bar).setExpanded(true)
    }

    private fun cancelActiveJobs() {
        val fragments = bottomNavController.fragmentManager
            .findFragmentById(bottomNavController.containerId)
            ?.childFragmentManager
            ?.fragments
        if (fragments != null) {
            for (fragment in fragments) {
                if (fragment is BaseAccountFragment) {
                    fragment.cancelActiveJobs()
                }
                if (fragment is BaseStoryFragment) {
                    fragment.cancelActiveJobs()
                }
                if (fragment is BaseCreateStoryFragment) {
                    fragment.cancelActiveJobs()
                }
            }
        }
        displayProgressBar(false)
    }
}