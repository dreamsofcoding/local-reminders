package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import org.koin.android.ext.android.inject

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    // Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSelectLocationBinding
    private var map: GoogleMap? = null
    private var selectedLatLng: LatLng? = null
    private var selectedLocationName: String? = null
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST = 1001

    var userLatLng = LatLng(52.5124494, 13.3742682)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val layoutId = R.layout.fragment_select_location
        binding = DataBindingUtil.inflate(inflater, layoutId, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(binding.root.context)

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.normal_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_NORMAL; true
        }

        R.id.hybrid_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_HYBRID; true
        }

        R.id.satellite_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_SATELLITE; true
        }

        R.id.terrain_map -> {
            map?.mapType = GoogleMap.MAP_TYPE_TERRAIN; true
        }

        else -> super.onOptionsItemSelected(item)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap
        binding.saveLocationButton.isEnabled = false

        enableLocation()

        googleMap.setOnMapClickListener { latLng ->
            map?.clear()
            map?.addMarker(MarkerOptions().position(latLng).title("Custom Location"))
            selectedLatLng = latLng
            selectedLocationName = "Lat: ${latLng.latitude}, Lng: ${latLng.longitude}"
            enableSaveButton()
        }
        googleMap.setOnPoiClickListener { poi ->
            map?.clear()
            map?.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))
            selectedLatLng = poi.latLng
            selectedLocationName = poi.name
            enableSaveButton()
        }

        binding.saveLocationButton.setOnClickListener {
            if (selectedLatLng != null && selectedLocationName != null) {
                _viewModel.latitude.value = selectedLatLng!!.latitude
                _viewModel.longitude.value = selectedLatLng!!.longitude
                _viewModel.reminderSelectedLocationStr.value = selectedLocationName
                _viewModel.selectedPOI.value =
                    poiFromSelection(selectedLatLng!!, selectedLocationName!!)
                _viewModel.navigationCommand.value = NavigationCommand.Back
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please select a location first",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun enableSaveButton() {
        binding.saveLocationButton.isEnabled = true
    }

    private fun poiFromSelection(latLng: LatLng, name: String) =
        com.google.android.gms.maps.model.PointOfInterest(latLng, name, name)

    private fun enableLocation() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.lastLocation
                .addOnSuccessListener { location ->
                    if (location != null) {
                        userLatLng = LatLng(location.latitude, location.longitude)
                    }
                    map?.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                }

            map?.isMyLocationEnabled = true
            map?.uiSettings?.isMyLocationButtonEnabled = true

        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                LOCATION_PERMISSION_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                enableLocation()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Location permission denied. Cannot display user location on map.",
                    Toast.LENGTH_LONG
                ).show()
                binding.saveLocationButton.isEnabled = false
            }
        } else {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT)
                .show()
        }
    }

    override fun onResume() {
        super.onResume()
        enableLocation()
    }
}