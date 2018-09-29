package com.dino.googlemapflutter

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.maps.GoogleMap
import io.flutter.app.FlutterFragmentActivity
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.PluginRegistry.Registrar

class GoogleMapFlutterPlugin : MethodCallHandler {
    private var mapFragment: TouchSupportMapFragment? = null
    private lateinit var mapView: MapView
    private val mapTypeMapping: HashMap<String, Int> = hashMapOf(
            "none" to GoogleMap.MAP_TYPE_NONE,
            "normal" to GoogleMap.MAP_TYPE_NORMAL,
            "satellite" to GoogleMap.MAP_TYPE_SATELLITE,
            "terrain" to GoogleMap.MAP_TYPE_TERRAIN,
            "hybrid" to GoogleMap.MAP_TYPE_HYBRID
    )

    companion object {
        val PermissionRequest: Int = 1
        lateinit var channel: MethodChannel
        const val REQUEST_GOOGLE_PLAY_SERVICES = 1000
        lateinit var registrar: Registrar

        @JvmStatic
        fun registerWith(registrar: Registrar): Unit {
            GoogleMapFlutterPlugin.registrar = registrar
            (GoogleMapFlutterPlugin.registrar.activity() as? FlutterFragmentActivity)?.flutterView?.enableTransparentBackground()
            channel = MethodChannel(registrar.messenger(), "com.dino.googlemapflutter")
            channel.setMethodCallHandler(GoogleMapFlutterPlugin())

            if (ContextCompat.checkSelfPermission(registrar.context(), Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                val array = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION)
                ActivityCompat.requestPermissions(registrar.activity(), array, PermissionRequest)
            }
        }

        fun getDensity(context: Context): Float {
            return context.resources.displayMetrics.density
        }

        fun convertPixtoDip(context: Context, pixel: Int): Int {
            if (pixel == 0) return 0

            val scale = getDensity(context)
            return (pixel * scale).toInt()
        }
    }

    override fun onMethodCall(call: MethodCall, result: Result): Unit {
        when {
            call.method == "show" -> {
                (GoogleMapFlutterPlugin.registrar.activity() as? FlutterFragmentActivity)?.let { fragmentActivity ->
                    val code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(fragmentActivity)

                    if (GoogleApiAvailability.getInstance().showErrorDialogFragment(fragmentActivity, code, REQUEST_GOOGLE_PLAY_SERVICES)) {
                        return
                    }

                    val mapOptions = call.argument<Map<String, Any>>("mapOptions")
                    val padding = call.argument<Map<String, Double>>("padding")
                    val cameraDict = mapOptions["cameraPosition"] as Map<String, Any>
                    MapView.initialCameraPosition = MapView.getCameraPosition(cameraDict)
                    MapView.showUserLocation = mapOptions["showUserLocation"] as Boolean
                    MapView.showMyLocationButton = mapOptions["showMyLocationButton"] as Boolean
                    MapView.showCompassButton = mapOptions["showCompassButton"] as Boolean

                    if (mapOptions["mapViewType"] != null) {
                        val mappedMapType: Int? = mapTypeMapping.get(mapOptions["mapViewType"])

                        if (mappedMapType != null) {
                            MapView.mapViewType = mappedMapType
                        }
                    }

                    val topMargin = convertPixtoDip(fragmentActivity, padding["top"]!!.toInt())
                    val bottomMargin = convertPixtoDip(fragmentActivity, padding["bottom"]!!.toInt())
                    val leftMargin = convertPixtoDip(fragmentActivity, padding["left"]!!.toInt())
                    val rightMargin = convertPixtoDip(fragmentActivity, padding["right"]!!.toInt())

                    val frameLayout = FrameLayout(fragmentActivity)
                    frameLayout.id = R.id.map_fragment
                    fragmentActivity.addContentView(frameLayout, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))

                    mapView = MapView(fragmentActivity)
                    mapView.setPadding(leftMargin, topMargin, rightMargin, bottomMargin)
                    mapFragment = TouchSupportMapFragment()
                    mapFragment?.getMapAsync(mapView)

                    val transaction = fragmentActivity.supportFragmentManager.beginTransaction()
                    transaction.replace(R.id.map_fragment, mapFragment)
                    transaction.commit()
                    fragmentActivity.flutterView.bringToFront()

                    fragmentActivity.flutterView.setOnTouchListener { v, event ->
                        val bmp = fragmentActivity.flutterView.bitmap
                        mapFragment?.mTouchView?.dispatchTouchEvent(event)

                        v.onTouchEvent(event)
                    }
                } ?: run {
                    result.notImplemented()
                    return
                }
            }
            call.method == "dismiss" -> {
                //mapActivity?.finish()
                result.success(true)
                return
            }
            call.method == "getZoomLevel" -> {
                val zoom = mapView.zoomLevel
                result.success(zoom)
            }
            call.method == "getCenter" -> {
                val center = mapView.target
                result.success(mapOf("latitude" to center.latitude,
                        "longitude" to center.longitude))
            }
            call.method == "setCamera" -> {
                mapView.handleSetCamera(call.arguments as Map<String, Any>)
                result.success(true)
            }
            call.method == "zoomToAnnotations" -> {
                mapView.handleZoomToAnnotations(call.arguments as Map<String, Any>)
                result.success(true)
            }
            call.method == "zoomToPolylines" -> {
                mapView.handleZoomToPolylines(call.arguments as Map<String, Any>)
                result.success(true)
            }
            call.method == "zoomToPolygons" -> {
                mapView.handleZoomToPolygons(call.arguments as Map<String, Any>)
                result.success(true)
            }
            call.method == "zoomToFit" -> {
                mapView.zoomToFit(call.arguments as Int)
                result.success(true)
            }
            call.method == "getVisibleMarkers" -> {
                val visibleMarkerIds = mapView.visibleMarkers
                result.success(visibleMarkerIds)
            }
            call.method == "clearAnnotations" -> {
                mapView.clearMarkers()
                result.success(true)
            }
            call.method == "setAnnotations" -> {
                mapView.handleSetAnnotations(call.arguments as List<Map<String, Any>>)
                result.success(true)
            }
            call.method == "addAnnotation" -> {
                mapView.handleAddAnnotation(call.arguments as Map<String, Any>)
            }
            call.method == "removeAnnotation" -> {
                mapView.handleRemoveAnnotation(call.arguments as Map<String, Any>)
            }
            call.method == "getVisiblePolylines" -> {
                val visiblePolylineIds = mapView.visiblePolyline
                result.success(visiblePolylineIds)
            }
            call.method == "clearPolylines" -> {
                mapView.clearPolylines()
                result.success(true)
            }
            call.method == "setPolylines" -> {
                mapView.handleSetPolylines(call.arguments as List<Map<String, Any>>)
                result.success(true)
            }
            call.method == "addPolyline" -> {
                mapView.handleAddPolyline(call.arguments as Map<String, Any>)
            }
            call.method == "removePolyline" -> {
                mapView.handleRemovePolyline(call.arguments as Map<String, Any>)
            }
            call.method == "getVisiblePolygons" -> {
                val visiblePolygonIds = mapView.visiblePolygon
                result.success(visiblePolygonIds)
            }
            call.method == "clearPolygons" -> {
                mapView.clearPolygons()
                result.success(true)
            }
            call.method == "setPolygons" -> {
                mapView.handleSetPolygons(call.arguments as List<Map<String, Any>>)
                result.success(true)
            }
            call.method == "addPolygon" -> {
                mapView.handleAddPolygon(call.arguments as Map<String, Any>)
            }
            call.method == "removePolygon" -> {
                mapView.handleRemovePolygon(call.arguments as Map<String, Any>)
            }
            else -> result.notImplemented()
        }
    }
}
