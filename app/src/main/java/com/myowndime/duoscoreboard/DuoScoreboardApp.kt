package com.myowndime.duoscoreboard

import android.app.Application
import com.myowndime.duoscoreboard.di.AppContainer

class DuoScoreboardApp : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
