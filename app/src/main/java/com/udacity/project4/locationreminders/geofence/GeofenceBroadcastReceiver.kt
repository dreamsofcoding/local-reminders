package com.udacity.project4.locationreminders.geofence

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingEvent

/**
 * Triggered by the Geofence.  Since we can have many Geofences at once, we pull the request
 * ID from the first Geofence, and locate it within the cached data in our Room DB
 *
 * Or users can add the reminders and then close the app, So our app has to run in the background
 * and handle the geofencing in the background.
 * To do that you can use https://developer.android.com/reference/android/support/v4/app/JobIntentService to do that.
 *
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_GEOFENCE_EVENT =
            "com.udacity.project4.locationreminders.ACTION_GEOFENCE_EVENT"
        const val EXTRA_GEOFENCE_ID = "GEOFENCE_ID"
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != ACTION_GEOFENCE_EVENT) return

        val geofencingEvent = GeofencingEvent.fromIntent(intent)

        if (geofencingEvent == null) {
            return
        }

        if (geofencingEvent.geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            if (triggeringGeofences?.isNotEmpty() == true) {
                val geofenceId = triggeringGeofences[0].requestId

                val serviceIntent = Intent(context, GeofenceTransitionsJobIntentService::class.java).apply {
                    putExtra(EXTRA_GEOFENCE_ID, geofenceId)
                }
                GeofenceTransitionsJobIntentService.enqueueWork(context, serviceIntent)
            }
        }
    }
}