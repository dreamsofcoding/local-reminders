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

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("GEOFENCE_DBG", "Receiver got intent: ${intent.action}")
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        if (geofencingEvent == null) {
            Log.e("GEOFENCE_DBG", "GeofencingEvent was null")
            return
        }

        if (geofencingEvent.hasError()) {
            Log.e("GEOFENCE", "Geofence error code: ${geofencingEvent.errorCode}")
            return
        }
        Log.d("GEOFENCE", "Geofence transition received: ${geofencingEvent.triggeringGeofences?.map{it.requestId}}")


//        if (geofencingEvent.geofenceTransition ==
//            Geofence.GEOFENCE_TRANSITION_DWELL
////            Geofence.GEOFENCE_TRANSITION_ENTER
//            ) {
//            GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
//        }
        GeofenceTransitionsJobIntentService.enqueueWork(context, intent)
    }
}