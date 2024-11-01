package ma.ensa.project

import android.Manifest
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.android.volley.DefaultRetryPolicy
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import org.json.JSONException

class MapActivity : AppCompatActivity(), OnMapReadyCallback {
    private var mMap: GoogleMap? = null
    private val zoomLevel = 15.0f
    private lateinit var requestQueue: RequestQueue
    private lateinit var locationManager: LocationManager
    private var isFirstLocationUpdate = true

    companion object {
        private const val SHOW_URL = "http://10.0.2.2/map_pizza/sourcefiles/showPosition.php"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        requestQueue = Volley.newRequestQueue(applicationContext)
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                200
            )
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as? SupportMapFragment
        mapFragment?.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap ?: run {
            Toast.makeText(this, "Error loading map", Toast.LENGTH_SHORT).show()
            return
        }

        mMap?.apply {
            uiSettings.isZoomControlsEnabled = true
            uiSettings.isMyLocationButtonEnabled = true
            uiSettings.isCompassEnabled = true
            uiSettings.isMapToolbarEnabled = true
        }

        try {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                mMap?.isMyLocationEnabled = true
                setupLocationUpdates()
                showPosition()
            }
        } catch (e: SecurityException) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            e.printStackTrace()
        }
    }

    private val locationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            updateMapLocation(location)
        }

        override fun onProviderEnabled(provider: String) {}

        override fun onProviderDisabled(provider: String) {
            if (provider == LocationManager.GPS_PROVIDER) {
                buildAlertMessageNoGps()
            }
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
    }

    private fun setupLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        locationManager.let {
            it.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                5000,
                10f,
                locationListener
            )
            it.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                5000,
                10f,
                locationListener
            )
        }

        locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)?.let { location ->
            updateMapLocation(location)
        }
    }

    private fun updateMapLocation(location: Location) {
        val currentLocation = LatLng(location.latitude, location.longitude)

        if (isFirstLocationUpdate) {
            mMap?.addMarker(
                MarkerOptions()
                    .position(currentLocation)
                    .title("Current Location")
            )
            mMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, zoomLevel))
            isFirstLocationUpdate = false
        }
    }

    private fun showPosition() {
        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, SHOW_URL, null,
            { response ->
                try {
                    val positions = response.getJSONArray("positions")
                    for (i in 0 until positions.length()) {
                        val position = positions.getJSONObject(i)
                        val latitude = position.getDouble("latitude")
                        val longitude = position.getDouble("longitude")
                        val date = position.getString("date")
                        val imei = position.optString("imei", "Unknown Device")

                        val location = LatLng(latitude, longitude)
                        val markerOptions = MarkerOptions()
                            .position(location)
                            .title("Position ${i + 1}")
                            .snippet("Date: $date\nIMEI: $imei")

                        mMap?.addMarker(markerOptions)

                        if (i == 0) {
                            mMap?.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    location,
                                    zoomLevel
                                )
                            )
                        }
                    }
                } catch (e: JSONException) {
                    e.printStackTrace()
                    Toast.makeText(
                        this,
                        "Error parsing positions: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            },
            { error ->
                Log.e("MapActivity", "Error fetching positions: ${error.networkResponse?.statusCode}")
                Toast.makeText(
                    this,
                    "Error fetching positions: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        ).apply {
            retryPolicy = DefaultRetryPolicy(
                30000,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )
        }

        requestQueue.add(jsonObjectRequest)
    }

    private fun buildAlertMessageNoGps() {
        AlertDialog.Builder(this)
            .setMessage("Your GPS seems to be disabled. Do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton("Yes") { _, _ ->
                startActivity(Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
                Toast.makeText(
                    this,
                    "GPS is required for better location accuracy",
                    Toast.LENGTH_LONG
                ).show()
            }
            .create()
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        locationManager.removeUpdates(locationListener)
    }
}
