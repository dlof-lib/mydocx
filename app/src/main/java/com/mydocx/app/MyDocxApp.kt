package com.mydocx.app

import android.app.Application

class MyDocxApp : Application() {
    override fun onCreate() {
        super.onCreate()
        // Global init hook (crash reporting, etc. can be wired here later).
    }
}
