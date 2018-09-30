package com.dino.googlemap.viewflutter

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Build
import android.support.v4.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.*
import io.flutter.app.FlutterFragmentActivity

class MapView() : OnMapReadyCallback {
    var googleMap: GoogleMap? = null
    lateinit var activity: FlutterFragmentActivity
    var markerIdLookup = HashMap<String, Marker>()
    var polylineIdLookup = HashMap<String, Polyline>()
    var polygonIdLookup = HashMap<String, Polygon>()
    var top: Int = 0
    var left: Int = 0
    var right: Int = 0
    var bottom: Int = 0

    constructor(activity: FlutterFragmentActivity) : this() {
        this.activity = activity
    }

    companion object {
        var attachTouch : Boolean = false
        var showUserLocation: Boolean = false
        var showMyLocationButton: Boolean = false
        var showCompassButton: Boolean = false
        lateinit var initialCameraPosition: CameraPosition
        var mapViewType: Int = GoogleMap.MAP_TYPE_NORMAL

        fun getCameraPosition(map: Map<String, Any>): CameraPosition {
            val latitude = map["latitude"] as Double
            val longitude = map["longitude"] as Double
            val zoom = map["zoom"] as Double

            return CameraPosition(LatLng(latitude, longitude), zoom.toFloat(), 0.0f, 0.0f)
        }

        fun mapTapped(latLng: LatLng) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("mapTapped",
                    mapOf("latitude" to latLng.latitude,
                            "longitude" to latLng.longitude))
        }

        fun mapLongTapped(latLng: LatLng) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("mapLongTapped",
                    mapOf("latitude" to latLng.latitude,
                            "longitude" to latLng.longitude))
        }

        fun annotationTapped(id: String) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("annotationTapped", id)
        }

        fun annotationDragStart(id: String, latLng: LatLng) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("annotationDragStart", mapOf(
                    "id" to id,
                    "latitude" to latLng.latitude,
                    "longitude" to latLng.longitude
            ))
        }

        fun annotationDragEnd(id: String, latLng: LatLng) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("annotationDragEnd", mapOf(
                    "id" to id,
                    "latitude" to latLng.latitude,
                    "longitude" to latLng.longitude
            ))
        }

        fun annotationDrag(id: String, latLng: LatLng) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("annotationDrag", mapOf(
                    "id" to id,
                    "latitude" to latLng.latitude,
                    "longitude" to latLng.longitude
            ))
        }

        fun polylineTapped(id: String) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("polylineTapped", id)
        }

        fun polygonTapped(id: String) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("polygonTapped", id)
        }

        fun cameraPositionChanged(pos: CameraPosition?) {
            pos?.let { posCurrent ->
                GoogleMapViewFlutterPlugin.channel.invokeMethod("cameraPositionChanged", mapOf(
                        "latitude" to posCurrent.target.latitude,
                        "longitude" to posCurrent.target.longitude,
                        "zoom" to posCurrent.zoom,
                        "bearing" to posCurrent.bearing,
                        "tilt" to posCurrent.tilt
                ))
            }
        }

        fun locationDidUpdate(loc: Location) {
            var verticalAccuracy = 0.0f
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                verticalAccuracy = loc.verticalAccuracyMeters
            GoogleMapViewFlutterPlugin.channel.invokeMethod("locationUpdated", mapOf(
                    "latitude" to loc.latitude,
                    "longitude" to loc.longitude,
                    "time" to loc.time,
                    "altitude" to loc.altitude,
                    "speed" to loc.speed,
                    "bearing" to loc.bearing,
                    "horizontalAccuracy" to loc.accuracy,
                    "verticalAccuracy" to verticalAccuracy
            ))
        }

        fun infoWindowTapped(id: String) {
            GoogleMapViewFlutterPlugin.channel.invokeMethod("infoWindowTapped", id)
        }

        fun getAssetFileDecriptor(asset: String): AssetFileDescriptor {
            val assetManager = GoogleMapViewFlutterPlugin.registrar.context().assets
            val key = GoogleMapViewFlutterPlugin.registrar.lookupKeyForAsset(asset)
            return assetManager.openFd(key)
        }

        fun indoorBuildingActivated(indoorBuilding: IndoorBuilding?) {
            if (indoorBuilding == null) {
                GoogleMapViewFlutterPlugin.channel.invokeMethod("indoorBuildingActivated", null)
            } else {
                GoogleMapViewFlutterPlugin.channel.invokeMethod("indoorBuildingActivated", mapOf(
                        "underground" to indoorBuilding.isUnderground,
                        "defaultIndex" to indoorBuilding.defaultLevelIndex,
                        "levels" to mappingIndoorLevels(indoorBuilding.levels)))
            }
        }

        fun indoorLevelActivated(level: IndoorLevel?) {
            if (level == null) {
                GoogleMapViewFlutterPlugin.channel.invokeMethod("indoorLevelActivated", null)
            } else {
                GoogleMapViewFlutterPlugin.channel.invokeMethod("indoorLevelActivated", mappingIndoorLevel(level))
            }
        }

        fun mappingIndoorLevels(levels: List<IndoorLevel>): List<Map<String, Any>> {
            val list = mutableListOf<Map<String, Any>>()
            levels.forEach { level -> list.add(mappingIndoorLevel(level)) }
            return list
        }

        fun mappingIndoorLevel(level: IndoorLevel): Map<String, Any> {
            return mapOf(
                    "name" to level.name,
                    "shortName" to level.shortName
            )
        }
    }

    fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        this.top = top
        this.left = left
        this.right = right
        this.bottom = bottom
    }

    @SuppressLint("MissingPermission")
    override fun onMapReady(p0: GoogleMap?) {
        googleMap = p0 //To change body of created functions use File | Settings | File Templates.
        googleMap?.mapType = mapViewType
        googleMap?.setPadding(left, top, right, bottom)
        googleMap?.uiSettings?.isCompassEnabled = showCompassButton

        if (showUserLocation) {
            if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                googleMap?.isMyLocationEnabled = showMyLocationButton
                googleMap?.uiSettings?.isMyLocationButtonEnabled = showMyLocationButton
            }
        }

        googleMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(p0: Marker?) {
                if (p0 != null) {
                    val id: String = p0.tag as String
                    annotationDragEnd(id, p0.position)
                }
            }

            override fun onMarkerDragStart(p0: Marker?) {
                if (p0 != null) {
                    val id: String = p0.tag as String
                    annotationDragStart(id, p0.position)
                }
            }

            override fun onMarkerDrag(p0: Marker?) {
                if (p0 != null) {
                    val id: String = p0.tag as String
                    annotationDrag(id, p0.position)
                }
            }
        })

        googleMap?.setOnMapClickListener { latLng ->
            mapTapped(latLng)
        }

        googleMap?.setOnMapLongClickListener { latLng ->
            mapLongTapped(latLng)
        }

        googleMap?.setOnMarkerClickListener { marker ->
            annotationTapped(marker.tag as String)
            false
        }

        googleMap?.setOnPolylineClickListener { polyline ->
            polylineTapped(polyline.tag as String)
        }

        googleMap?.setOnPolygonClickListener { polygon ->
            polygonTapped(polygon.tag as String)
        }

        googleMap?.setOnCameraMoveListener {
            val pos = googleMap?.cameraPosition
            cameraPositionChanged(pos)
        }

        googleMap?.setOnMyLocationChangeListener {
            val loc = googleMap?.myLocation ?: return@setOnMyLocationChangeListener
            locationDidUpdate(loc)
        }

        googleMap?.setOnInfoWindowClickListener { marker ->
            infoWindowTapped(marker.tag as String)
        }

        googleMap?.setOnIndoorStateChangeListener(object : GoogleMap.OnIndoorStateChangeListener {
            override fun onIndoorBuildingFocused() {
                indoorBuildingActivated(googleMap?.focusedBuilding)
            }

            override fun onIndoorLevelActivated(building: IndoorBuilding?) {
                if (building == null || building.activeLevelIndex < 0) {
                    indoorLevelActivated(null)
                } else {
                    indoorLevelActivated(building.levels.get(building.activeLevelIndex))
                }
            }
        })

        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(initialCameraPosition))
        GoogleMapViewFlutterPlugin.channel.invokeMethod("onMapReady" , null)
    }

    fun handleSetCamera(map: Map<String, Any>) {
        val lat = map["latitude"] as Double
        val lng = map["longitude"] as Double
        val zoom = map["zoom"] as Double
        val bearing = map["bearing"] as Double
        val tilt = map["tilt"] as Double
        setCamera(LatLng(lat, lng), zoom.toFloat(), bearing.toFloat(), tilt.toFloat())
    }

    fun handleZoomToAnnotations(map: Map<String, Any>) {
        val ids = map["annotations"] as List<String>
        val padding = map["padding"] as Double
        zoomToAnnotations(ids, padding.toFloat())
    }

    fun handleZoomToPolylines(map: Map<String, Any>) {
        val ids = map["polylines"] as List<String>
        val padding = map["padding"] as Double
        zoomToPolylines(ids, padding.toFloat())
    }

    fun handleZoomToPolygons(map: Map<String, Any>) {
        val ids = map["polygons"] as List<String>
        val padding = map["padding"] as Double
        zoomToPolygons(ids, padding.toFloat())
    }

    fun handleSetAnnotations(annotations: List<Map<String, Any>>) {
        val mapAnnoations = ArrayList<MapAnnotation>()
        for (a in annotations) {
            val mapAnnotation = MapAnnotation.fromMap(a)
            if (mapAnnotation != null) {
                mapAnnoations.add(mapAnnotation)
            }
        }

        setAnnotations(mapAnnoations)
    }

    fun handleAddAnnotation(map: Map<String, Any>) {
        MapAnnotation.fromMap(map)?.let {
            addMarker(it)
        }
    }

    fun handleRemoveAnnotation(map: Map<String, Any>) {
        MapAnnotation.fromMap(map)?.let {
            removeMarker(it)
        }
    }

    fun handleSetPolylines(polylines: List<Map<String, Any>>) {
        val mapPolylines = ArrayList<MapPolyline>()
        for (a in polylines) {
            val mapPolyline = MapPolyline.fromMap(a)
            if (mapPolyline != null) {
                mapPolylines.add(mapPolyline)
            }
        }

        setPolylines(mapPolylines)
    }

    fun handleAddPolyline(map: Map<String, Any>) {
        MapPolyline.fromMap(map)?.let {
            addPolyline(it)
        }
    }

    fun handleRemovePolyline(map: Map<String, Any>) {
        MapPolyline.fromMap(map)?.let {
            removePolyline(it)
        }
    }

    fun handleSetPolygons(polygons: List<Map<String, Any>>) {
        val mapPolygons = ArrayList<MapPolygon>()
        for (a in polygons) {
            val mapPolygon = MapPolygon.fromMap(a)

            if (mapPolygon != null) {
                mapPolygons.add(mapPolygon)
            }
        }

        setPolygons(mapPolygons)
    }

    fun handleAddPolygon(map: Map<String, Any>) {
        MapPolygon.fromMap(map)?.let {
            addPolygon(it)
        }
    }

    fun handleRemovePolygon(map: Map<String, Any>) {
        MapPolygon.fromMap(map)?.let {
            removePolygon(it)
        }
    }

    val zoomLevel: Float
        get() {
            return googleMap?.cameraPosition?.zoom ?: 0.0.toFloat()
        }

    val target: LatLng
        get() = googleMap?.cameraPosition?.target ?: LatLng(0.0,
                0.0)

    fun setCamera(target: LatLng, zoom: Float, bearing: Float, tilt: Float) {
        val cameraPosition = CameraPosition.Builder()
                .target(target)
                .zoom(zoom)
                .bearing(bearing)
                .tilt(tilt)
                .build()
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    fun setAnnotations(annotations: List<MapAnnotation>) {
        val map = this.googleMap ?: return
        clearMarkers()
        for (annotation in annotations) {
            val marker = createMarkerForAnnotation(annotation, map)
            markerIdLookup[annotation.identifier] = marker
        }
    }

    fun clearMarkers() {
        markerIdLookup.forEach {
            it.value.remove()
        }
        markerIdLookup.clear()
    }

    fun addMarker(annotation: MapAnnotation) {
        val map = this.googleMap ?: return
        val existingMarker = markerIdLookup[annotation.identifier]
        if (existingMarker != null) return
        val marker = createMarkerForAnnotation(annotation, map)
        markerIdLookup[annotation.identifier] = marker
    }

    fun removeMarker(annotation: MapAnnotation) {
        this.googleMap ?: return
        val existingMarker = markerIdLookup[annotation.identifier] ?: return
        markerIdLookup.remove(annotation.identifier)
        existingMarker.remove()
    }

    fun setPolylines(polyLines: List<MapPolyline>) {
        val map = this.googleMap ?: return
        clearPolylines()
        for (mapPolyline in polyLines) {
            val polyline = createPolyline(mapPolyline, map)
            polylineIdLookup[mapPolyline.identifier] = polyline
        }
    }

    fun clearPolylines() {
        polylineIdLookup.forEach {
            it.value.remove()
        }
        polylineIdLookup.clear()
    }

    fun addPolyline(mapPolyline: MapPolyline) {
        val map = this.googleMap ?: return
        val existingLine = polylineIdLookup[mapPolyline.identifier]
        if (existingLine != null) return
        val polyline = createPolyline(mapPolyline, map)
        polylineIdLookup[mapPolyline.identifier] = polyline
    }

    fun removePolyline(mapPolyline: MapPolyline) {
        this.googleMap ?: return
        val existingLine = polylineIdLookup[mapPolyline.identifier] ?: return
        polylineIdLookup.remove(mapPolyline.identifier)
        existingLine.remove()
    }

    fun setPolygons(mapPolygons: List<MapPolygon>) {
        val map = this.googleMap ?: return
        clearPolygons()
        for (mapPolygon in mapPolygons) {
            val polygon = createPolygon(mapPolygon, map)
            polygonIdLookup[mapPolygon.identifier] = polygon
        }
    }

    fun clearPolygons() {
        polygonIdLookup.forEach {
            it.value.remove()
        }
        polygonIdLookup.clear()
    }

    fun addPolygon(mapPolygon: MapPolygon) {
        val map = this.googleMap ?: return
        val existingFigure = polygonIdLookup[mapPolygon.identifier]
        if (existingFigure != null) return
        val polygon = createPolygon(mapPolygon, map)
        polygonIdLookup[mapPolygon.identifier] = polygon
    }

    fun removePolygon(mapPolygon: MapPolygon) {
        this.googleMap ?: return
        val existingFigure = polygonIdLookup[mapPolygon.identifier] ?: return
        polygonIdLookup.remove(mapPolygon.identifier)
        existingFigure.remove()
    }

    val visibleMarkers: List<String>
        get() {
            val map = this.googleMap ?: return emptyList()
            val region = map.projection.visibleRegion
            val visibleIds = ArrayList<String>()
            for (marker in markerIdLookup.values) {
                if (region.latLngBounds.contains(marker.position)) {
                    visibleIds.add(marker.tag as String)
                }
            }
            return visibleIds
        }

    val visiblePolyline: List<String>
        get() {
            val map = this.googleMap ?: return emptyList()
            val region = map.projection.visibleRegion
            val visibleIds = ArrayList<String>()
            for (polyline in polylineIdLookup.values) {
                for (point in polyline.points) {
                    if (region.latLngBounds.contains(point)) {
                        visibleIds.add(polyline.tag as String)
                        break
                    }
                }
            }
            return visibleIds
        }

    val visiblePolygon: List<String>
        get() {
            val map = this.googleMap ?: return emptyList()
            val region = map.projection.visibleRegion
            val visibleIds = ArrayList<String>()
            for (polygon in polygonIdLookup.values) {
                for (point in polygon.points) {
                    if (region.latLngBounds.contains(point)) {
                        visibleIds.add(polygon.tag as String)
                        break
                    }
                }
            }
            return visibleIds
        }


    fun zoomToFit(padding: Int) {
        val map = this.googleMap ?: return
        val bounds = LatLngBounds.Builder()
        var count = 0
        for (marker in markerIdLookup.values) {
            bounds.include(marker.position)
            count++
        }
        for (polyline in polylineIdLookup.values) {
            for (point in polyline.points) {
                bounds.include(point)
                count++
            }
        }
        for (polygon in polygonIdLookup.values) {
            for (point in polygon.points) {
                bounds.include(point)
                count++
            }
        }
        if (map.isMyLocationEnabled && map.myLocation != null) {
            if (count == 0) {
                map.animateCamera(CameraUpdateFactory.newLatLngZoom(
                        LatLng(map.myLocation.latitude,
                                map.myLocation.longitude), 12.toFloat()))
                return
            }
            bounds.include(LatLng(map.myLocation.latitude,
                    map.myLocation.longitude))
        }
        try {
            map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(), padding))
        } catch (e: Exception) {
        }
    }

    fun zoomToAnnotations(annoationIds: List<String>, padding: Float) {
        val map = this.googleMap ?: return
        if (annoationIds.size == 1) {
            val marker = markerIdLookup[annoationIds.first()] ?: return
            map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(marker.position,
                            18.toFloat()))
            return
        }
        val bounds = LatLngBounds.Builder()
        for (id in annoationIds) {
            val marker = markerIdLookup[id] ?: continue
            bounds.include(marker.position)
        }
        try {
            map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(),
                            padding.toInt()))
        } catch (e: Exception) {
        }
    }

    fun zoomToPolylines(polylineIds: List<String>, padding: Float) {
        val map = this.googleMap ?: return
        if (polylineIds.size == 1) {
            val polyline = polylineIdLookup[polylineIds.first()] ?: return
            map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(polyline.points.first(),
                            18.toFloat()))
            return
        }
        val bounds = LatLngBounds.Builder()
        for (id in polylineIds) {
            val polyline = polylineIdLookup[id] ?: continue
            for (point in polyline.points) {
                bounds.include(point)
            }
        }
        try {
            map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(),
                            padding.toInt()))
        } catch (e: Exception) {
        }
    }

    fun zoomToPolygons(polygonIds: List<String>, padding: Float) {
        val map = this.googleMap ?: return
        if (polygonIds.size == 1) {
            val polygon = polygonIdLookup[polygonIds.first()] ?: return
            map.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(polygon.points.first(),
                            18.toFloat()))
            return
        }
        val bounds = LatLngBounds.Builder()
        for (id in polygonIds) {
            val polygon = polygonIdLookup[id] ?: continue
            for (point in polygon.points) {
                bounds.include(point)
            }
        }
        try {
            map.animateCamera(
                    CameraUpdateFactory.newLatLngBounds(bounds.build(),
                            padding.toInt()))
        } catch (e: Exception) {
        }
    }

    fun createMarkerForAnnotation(annotation: MapAnnotation, map: GoogleMap): Marker {
        val markerOptions = MarkerOptions()
                .position(annotation.coordinate)
                .title(annotation.title)
                .draggable(annotation.draggable)
                .rotation(annotation.rotation.toFloat())
        if (annotation is ClusterAnnotation) {
            markerOptions.snippet(annotation.clusterCount.toString())
        }
        var bitmap: Bitmap? = null
        if (annotation.icon != null) {
            try {
                val assetFileDescriptor: AssetFileDescriptor = getAssetFileDecriptor(annotation.icon.asset)
                val fd = assetFileDescriptor.createInputStream()
                bitmap = BitmapFactory.decodeStream(fd)
                var width = annotation.icon.width
                var height = annotation.icon.height
                if (width == 0.0)
                    width = bitmap.width.toDouble()
                if (height == 0.0)
                    height = bitmap.height.toDouble()
                bitmap = Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), false)
            } catch (exception: Exception) {
                exception.printStackTrace()
            }
        }

        if (bitmap != null) {
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(bitmap))
        } else {
            markerOptions.icon(BitmapDescriptorFactory.defaultMarker(
                    annotation.colorHue))
        }

        val marker = map.addMarker(markerOptions)
        marker.tag = annotation.identifier
        return marker
    }

    fun createPolyline(mapPolyline: MapPolyline, map: GoogleMap): Polyline {
        val polyline: Polyline = map
                .addPolyline(PolylineOptions()
                        .color(mapPolyline.color)
                        .visible(true)
                        .clickable(true)
                        .jointType(mapPolyline.jointType)
                        .width(mapPolyline.width)
                        .addAll(mapPolyline.points))
        polyline.tag = mapPolyline.identifier
        return polyline
    }

    fun createPolygon(mapPolygon: MapPolygon, map: GoogleMap): Polygon {
        val polygonOptions = PolygonOptions()
                .strokeColor(mapPolygon.strokeColor)
                .fillColor(mapPolygon.fillColor)
                .visible(true)
                .clickable(true)
                .strokeWidth(mapPolygon.strokeWidth)
                .strokeJointType(mapPolygon.jointType)
                .addAll(mapPolygon.points)
        if (mapPolygon.holes.isNotEmpty())
            for (hole in mapPolygon.holes) {
                polygonOptions.addHole(hole.points)
            }
        val polygon: Polygon = map.addPolygon(polygonOptions)
        polygon.tag = mapPolygon.identifier
        return polygon
    }
}