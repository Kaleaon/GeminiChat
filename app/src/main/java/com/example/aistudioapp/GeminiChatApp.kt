package com.example.aistudioapp

import android.app.Application
import com.example.aistudioapp.di.ServiceLocator

class GeminiChatApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ServiceLocator.initialize(this)
    }
}
