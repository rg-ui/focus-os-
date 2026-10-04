package com.focusos.app

import android.app.Application
import com.focusos.app.data.repository.FocusOsRepository
import com.focusos.app.data.supabase.SupabaseClient
import com.focusos.app.util.NotificationHelper

class FocusOsApp : Application() {

    lateinit var repository: FocusOsRepository
        private set

    lateinit var supabaseClient: SupabaseClient
        private set

    override fun onCreate() {
        super.onCreate()
        instance = this
        repository = FocusOsRepository(this)
        supabaseClient = SupabaseClient()
        NotificationHelper.createNotificationChannels(this)
    }

    companion object {
        lateinit var instance: FocusOsApp
            private set
    }
}
