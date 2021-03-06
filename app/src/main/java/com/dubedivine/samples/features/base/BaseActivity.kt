package com.dubedivine.samples.features.base

import android.os.Bundle
import android.support.v4.util.LongSparseArray
import android.support.v4.view.GravityCompat
import android.support.v7.app.AppCompatActivity
import android.view.MenuItem
import butterknife.ButterKnife
import com.dubedivine.samples.MvpStarterApplication
import com.dubedivine.samples.injection.component.ActivityComponent
import com.dubedivine.samples.injection.component.ConfigPersistentComponent
import com.dubedivine.samples.injection.component.DaggerConfigPersistentComponent
import com.dubedivine.samples.injection.module.ActivityModule
import kotlinx.android.synthetic.main.activity_main.*
import timber.log.Timber
import java.util.concurrent.atomic.AtomicLong

/**
 * Abstract activity that every other Activity in this application must implement. It provides the
 * following functionality:
 * - Handles creation of Dagger components and makes sure that instances of
 * ConfigPersistentComponent are kept across configuration changes.
 * - Set up and handles a GoogleApiClient instance that can be used to access the Google sign in
 * api.
 * - Handles signing out when an authentication error event is received.
 */
abstract class BaseActivity : AppCompatActivity() {

    private var mActivityComponent: ActivityComponent? = null
    private var mActivityId: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(layout)
        ButterKnife.bind(this) //setting the view to be this one
        // Create the ActivityComponent and reuses cached ConfigPersistentComponent if this is
        // being called after a configuration change.
        mActivityId = savedInstanceState?.getLong(KEY_ACTIVITY_ID) ?: NEXT_ID.getAndIncrement()
        val configPersistentComponent: ConfigPersistentComponent
        if (sComponentsArray.get(mActivityId) == null) {
            Timber.i("Creating new ConfigPersistentComponent id=%d", mActivityId)
            configPersistentComponent = DaggerConfigPersistentComponent.builder()
                    .applicationComponent(MvpStarterApplication[this].component)
                    .build()
            sComponentsArray.put(mActivityId, configPersistentComponent)
        } else {
            Timber.i("Reusing ConfigPersistentComponent id=%d", mActivityId)
            configPersistentComponent = sComponentsArray.get(mActivityId)
        }
        mActivityComponent = configPersistentComponent.activityComponent(ActivityModule(this))
        mActivityComponent?.inject(this)
    }

    abstract val layout: Int

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLong(KEY_ACTIVITY_ID, mActivityId)
    }

    override fun onDestroy() {
        if (!isChangingConfigurations) {
            Timber.i("Clearing ConfigPersistentComponent id=%d", mActivityId)
            sComponentsArray.remove(mActivityId)
        }
        super.onDestroy()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    fun activityComponent(): ActivityComponent {
        return mActivityComponent as ActivityComponent
    }

    /*
    * fast logging function
    * */
    fun l(msg : String) {
        Timber.i(msg)
    }


    // static class dude
    companion object {
        private val KEY_ACTIVITY_ID = "KEY_ACTIVITY_ID"
        private val NEXT_ID = AtomicLong(0)
        private val sComponentsArray = LongSparseArray<ConfigPersistentComponent>()

    }

}
