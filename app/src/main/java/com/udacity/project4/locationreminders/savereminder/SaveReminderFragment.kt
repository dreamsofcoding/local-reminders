package com.udacity.project4.locationreminders.savereminder

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.GeofencingConstants.GEOFENCE_DEFAULT_RADIUS_IN_METERS
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject
import java.util.UUID

class SaveReminderFragment : BaseFragment() {

    // Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fine = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val background = permissions[Manifest.permission.ACCESS_BACKGROUND_LOCATION] == true
        if (fine && background) {
            onSaveClicked()
        } else {
            Toast.makeText(requireContext(), getString(R.string.permission_denied_explanation), Toast.LENGTH_SHORT).show()
        }
    }

    private lateinit var geofencingClient: GeofencingClient

    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(binding.root.context, GeofenceBroadcastReceiver::class.java).apply {
            action = "ACTION_GEOFENCE_EVENT"
            `package` = binding.root.context.applicationContext.packageName
        }
        PendingIntent.getBroadcast(
            binding.root.context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        geofencingClient = LocationServices.getGeofencingClient(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_save_reminder
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        setDisplayHomeAsUpEnabled(true)
        binding.viewModel = _viewModel
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this

        binding.selectLocation.setOnClickListener {
            navigateToSelectLocation()
        }

        binding.saveReminder.setOnClickListener {
            if (hasLocationPermissions()) {
                onSaveClicked()
            } else {
                requestForegroundAndBackgroundLocationPermissions()
            }
        }

        binding.radiusSlider.value = _viewModel.geofenceRadius.value ?: GEOFENCE_DEFAULT_RADIUS_IN_METERS

        binding.radiusSlider.addOnChangeListener { slider, value, fromUser ->
            _viewModel.geofenceRadius.value = value
            binding.radiusValue.text = "${value.toInt()} m"
        }
    }

    private fun requestForegroundAndBackgroundLocationPermissions() {
        val permissionsArray = arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION
        )
        permissionLauncher.launch(permissionsArray)
    }

    @SuppressLint("MissingPermission")
    private fun onSaveClicked() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        when {
            title.isNullOrBlank() -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.err_enter_title), Toast.LENGTH_SHORT
                ).show()
            }

            location.isNullOrBlank() || latitude == null || longitude == null -> {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.err_select_location), Toast.LENGTH_SHORT
                ).show()
            }
        }

        if (latitude == null || longitude == null) return

        val id = UUID.randomUUID().toString()
        val reminder = ReminderDataItem(
            title,
            description ?: "",
            location,
            latitude,
            longitude,
            id
        )

        addGeofenceForReminder(reminder)
        _viewModel.validateAndSaveReminder(reminder)

    }

    @RequiresPermission(Manifest.permission.ACCESS_FINE_LOCATION)
    private fun addGeofenceForReminder(reminder: ReminderDataItem) {

        if (!hasLocationPermissions()) {
            requestForegroundAndBackgroundLocationPermissions()
            return
        }

        val geofence = Geofence.Builder()
            .setRequestId(reminder.id)
            .setCircularRegion(
                reminder.latitude!!,
                reminder.longitude!!,
                _viewModel.geofenceRadius.value ?: GEOFENCE_DEFAULT_RADIUS_IN_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(
                Geofence.GEOFENCE_TRANSITION_ENTER or Geofence.GEOFENCE_TRANSITION_DWELL)
            .build()

        val request = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient
            .addGeofences(request, geofencePendingIntent)
            .addOnSuccessListener {
                Toast.makeText(
                    binding.root.context,
                    "Geofence added for ${reminder.location}", Toast.LENGTH_SHORT
                ).show()
                Log.d("GEOFENCE_DBG", "Geofence added: id=${reminder.id}, " +
                        "lat=${reminder.latitude}, lon=${reminder.longitude}, " +
                        "radius=${_viewModel.geofenceRadius.value}")
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    binding.root.context,
                    "Failed to add geofence: ${e.message}", Toast.LENGTH_LONG
                ).show()
                Log.e("GEOFENCE_DBG", "addGeofences failed", e)
            }
    }

    private fun navigateToSelectLocation() {
        val directions = SaveReminderFragmentDirections
            .actionSaveReminderFragmentToSelectLocationFragment()
        _viewModel.navigationCommand.value = NavigationCommand.To(directions)
    }

    override fun onDestroy() {
        super.onDestroy()
        _viewModel.onClear()
    }

    private fun hasLocationPermissions(): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val background = ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_BACKGROUND_LOCATION
            ) == PackageManager.PERMISSION_GRANTED

        return fine && background
    }
}