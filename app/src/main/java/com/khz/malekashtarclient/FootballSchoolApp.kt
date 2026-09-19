package com.khz.malekashtarclient

import android.app.Application
import com.khz.malekashtarclient.core.di.AppContainer

/**
 * Application اصلی — ظرف DI دستی در اینجا نگه‌داری می‌شود.
 *
 * دسترسی از Composable:
 *   val container = (LocalContext.current.applicationContext as FootballSchoolApp).container
 *
 * یا راحت‌تر:
 *   import com.khz.malekashtarclient.core.util.LocalAppContainer
 *   val container = LocalAppContainer
 */
class FootballSchoolApp : Application() {

    lateinit var container: AppContainer

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
