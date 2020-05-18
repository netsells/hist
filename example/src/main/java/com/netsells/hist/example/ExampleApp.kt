package com.netsells.hist.example

import android.app.Application
import com.netsells.hist.BuildConfig
import com.netsells.hist.HistTree
import timber.log.Timber

class ExampleApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Timber.plant(
            Timber.DebugTree(),
            HistTree(
                "Hist Example",
                BuildConfig.VERSION_NAME,
                BuildConfig.VERSION_CODE,
                if (BuildConfig.DEBUG) "debug" else "production",
                "http://your.logstash.url",
                5001
            )
        )
    }
}