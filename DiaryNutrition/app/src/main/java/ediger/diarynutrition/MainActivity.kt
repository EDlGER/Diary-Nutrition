package ediger.diarynutrition

import android.annotation.SuppressLint
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.drawerlayout.widget.DrawerLayout.DrawerListener
import androidx.fragment.app.viewModels
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.ui.NavigationUI
import com.android.billingclient.api.*
import com.google.android.gms.ads.MobileAds
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.progressindicator.LinearProgressIndicator
import ediger.diarynutrition.billing.BillingClientLifecycle
import ediger.diarynutrition.billing.BillingViewModel
import ediger.diarynutrition.diary.DiaryFragment
import ediger.diarynutrition.inapputil.IabHelper
import ediger.diarynutrition.intro.IntroActivity
import ediger.diarynutrition.settings.SettingsViewModel
import ediger.diarynutrition.util.hideKeyboard
import java.util.Locale

class MainActivity : AppCompatActivity() {

    lateinit var appBar: AppBarLayout

    lateinit var navigationView: NavigationView

    private lateinit var drawerLayout: DrawerLayout

    private lateinit var billingClientLifecycle: BillingClientLifecycle
    private val billingViewModel: BillingViewModel by viewModels()

    private val settingsViewModel: SettingsViewModel by viewModels()

    // TODO: redundant
    private var isAdsRemoved = false

    private var lastBackPress = 0L
    private var backPressToast: Toast? = null

    @SuppressLint("SourceLockedOrientationActivity")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO: Suppressed
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //First app start
        val isFirstRun = PreferenceHelper.getValue(KEY_FIRST_RUN, Boolean::class.javaObjectType, true)

        //startIntroActivity
        if (isFirstRun) {
            startActivity(
                    Intent(this, IntroActivity::class.java)
            )
        }

        // Init language
        val lang = PreferenceHelper.getValue(KEY_LANGUAGE, String::class.java, "")
        if (lang.isEmpty()) PreferenceHelper.setValue(KEY_LANGUAGE, Locale.getDefault().language)

        //Restore database after install if necessary
        settingsViewModel.restoreIfNecessary()

        //Ads
        isAdsRemoved = getSharedPreferences(PREF_FILE_PREMIUM, MODE_PRIVATE)
                .getBoolean(PREF_PREMIUM, false)
        MobileAds.initialize(applicationContext)

        // Billing
        billingSetup()

        setContentView(R.layout.activity_main)
        setupNavigation()
    }

    private fun setupNavigation() {
        appBar = findViewById(R.id.app_bar_layout)

        setSupportActionBar(findViewById<Toolbar>(R.id.toolbar))

        drawerLayout = findViewById(R.id.drawer_layout)
        drawerLayout.addDrawerListener(object : DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
                hideKeyboard(currentFocus)
            }
            override fun onDrawerOpened(drawerView: View) { }
            override fun onDrawerClosed(drawerView: View) { }
            override fun onDrawerStateChanged(newState: Int) { }
        })

        navigationView = findViewById(R.id.nav_view)
        val navController = findNavController(R.id.nav_host_fragment)

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        NavigationUI.setupWithNavController(navigationView, navController)

        navigationView.menu.findItem(R.id.nav_rate).setOnMenuItemClickListener {
            val uri = Uri.parse("market://details?id=${this.packageName}")
            val uriOld = Uri.parse("http://play.google.com/store/apps/details?id=${this.packageName}")
            // Go to market
            try {
                startActivity(
                        Intent(Intent.ACTION_VIEW, uri).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or
                                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
                        }
                )
            } catch (e: ActivityNotFoundException) {
                startActivity(
                        Intent(Intent.ACTION_VIEW, uriOld)
                )
            }
            false
        }
    }

    private fun billingSetup() {
        billingClientLifecycle = (application as AppContext).billingClientLifecycle
        lifecycle.addObserver(billingClientLifecycle)

        billingViewModel.buyEvent.observe(this) {
            it?.let {
                billingClientLifecycle.launchBillingFlow(this, it)
            }
        }

        billingViewModel.openPlayStoreSubscriptionsEvent.observe(this) {
            val sku = it
            val url = if (sku == null) {
                PLAY_STORE_SUBSCRIPTION_URL
            } else {
                String.format(PLAY_STORE_SUBSCRIPTION_DEEPLINK_URL, sku, packageName)
            }
            val intent = Intent(Intent.ACTION_VIEW)
            intent.data = Uri.parse(url)
            startActivity(intent)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return NavigationUI.navigateUp(
                Navigation.findNavController(this, R.id.nav_host_fragment), drawerLayout)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else if (navigationView.menu.findItem(R.id.nav_diary).isChecked) {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastBackPress > 3000) {
                backPressToast = Toast.makeText(this, R.string.message_backpress,
                        Toast.LENGTH_LONG)
                backPressToast?.show()

                lastBackPress = currentTime
            } else {
                backPressToast?.cancel()
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    override fun onContextMenuClosed(menu: Menu) {
        super.onContextMenuClosed(menu)
        val activeFragment = supportFragmentManager
                .primaryNavigationFragment
                ?.childFragmentManager
                ?.primaryNavigationFragment
        if (activeFragment is DiaryFragment) {
            activeFragment.onContextMenuClosed()
        }
    }

    fun toggleProgress(toggle: Boolean) {
        val progress: LinearProgressIndicator = findViewById(R.id.progress)
        when (toggle) {
            true -> progress.show()
            false -> progress.hide()
        }
    }

    companion object {
        const val TAG = "DiaryNutrition"
    }
}