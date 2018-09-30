import 'dart:async';
import 'map_view.dart';
import 'camera_position.dart';
import 'location.dart';
import 'marker.dart';
import 'polygon.dart';
import 'polyline.dart';
import 'google_map_view_flutter.dart';

class MapController {
  ListenerController listenerController;
  GetListener getListener;
  MapView mMap;

  MapController(MapView controller) {
	this.listenerController = controller;
	this.getListener = controller;
  }

  void setMarkers(List<Marker> annotations) {
    listenerController?.setMarkers(annotations);
  }

  void clearAnnotations() {
    listenerController?.clearedAnnotations();
  }

  void addMarker(Marker marker) {
    listenerController?.clearedAnnotations();
  }

  void removeMarker(Marker marker) {
    listenerController?.removedMarker(marker);
  }

  void setPolylines(List<Polyline> polylines) {
    listenerController?.setPolylines(polylines);
  }

  void clearPolylines() {
    listenerController?.clearedPolylines();
  }

  void addPolyline(Polyline polyline) {
    listenerController?.addedPolyline(polyline);
  }

  void removePolyline(Polyline polyline) {
    listenerController?.removedPolyline(polyline);
  }

  void setPolygons(List<Polygon> polygons) {
    listenerController?.setPolygons(polygons);
  }

  void clearPolygons() {
    listenerController?.clearedPolygons();
  }

  void addPolygon(Polygon polygon) {
    listenerController?.addedPolygon(polygon);
  }

  void removePolygon(Polygon polygon) {
    listenerController?.removedPolygon(polygon);
  }

  void zoomToFit({int padding: 50}) {
    GoogleMapViewFlutter.channel.invokeMethod('zoomToFit', padding);
  }

  void zoomToAnnotations(List<String> annotationIds, {double padding: 50.0}) {
    GoogleMapViewFlutter.channel.invokeMethod('zoomToAnnotations',
        {"annotations": annotationIds, "padding": padding});
  }

  void zoomToPolylines(List<String> polylines, {double padding: 50.0}) {
    GoogleMapViewFlutter.channel.invokeMethod(
        'zoomToPolylines', {"polylines": polylines, "padding": padding});
  }

  void zoomToPolygons(List<String> polygonsIds, {double padding: 50.0}) {
    GoogleMapViewFlutter.channel.invokeMethod(
        'zoomToPolygons', {"polygons": polygonsIds, "padding": padding});
  }

  void setCameraPosition(CameraPosition cameraPosition) {
    GoogleMapViewFlutter.channel.invokeMethod("setCamera", cameraPosition.toMap());
  }

  Future<Location> get centerLocation async {
    Map locationMap = await GoogleMapViewFlutter.channel.invokeMethod("getCenter");

    return new Location(locationMap["latitude"], locationMap["longitude"]);
  }

  Future<double> get zoomLevel async {
    return await GoogleMapViewFlutter.channel.invokeMethod("getZoomLevel");
  }

  Future<List<Marker>> get visibleAnnotations async {
    if (this.getListener == null) {
      throw AssertionError('Need set Map Controller');
    }

    List<dynamic> ids =
        await GoogleMapViewFlutter.channel.invokeMethod("getVisibleMarkers");
    var annotations = <Marker>[];

    for (var id in ids) {
      var annotation = this.getListener.getAnnotations()[id];
      annotations.add(annotation);
    }

    return annotations;
  }

  Future<List<Polyline>> get visiblePolyLines async {
    if (this.getListener == null) {
      throw AssertionError('Need set Map Controller');
    }

    List<dynamic> ids =
        await GoogleMapViewFlutter.channel.invokeMethod("getVisiblePolylines");
    var polylines = <Polyline>[];

    for (var id in ids) {
      var polyline = this.getListener.getPolylines()[id];
      polylines.add(polyline);
    }

    return polylines;
  }

  Future<List<Polygon>> get visiblePolygons async {
    if (this.getListener == null) {
      throw AssertionError('Need set Map Controller');
    }

    List<dynamic> ids =
        await GoogleMapViewFlutter.channel.invokeMethod("getVisiblePolygons");
    var polygons = <Polygon>[];

    for (var id in ids) {
      var polygon = this.getListener.getPolygons()[id];
      polygons.add(polygon);
    }

    return polygons;
  }
}

abstract class ListenerController {
  void setMarkers(List<Marker> annotations);

  void clearedAnnotations();

  void addedMarker(Marker marker);

  void removedMarker(Marker marker);

  void setPolylines(List<Polyline> polylines);

  void clearedPolylines();

  void addedPolyline(Polyline polyline);

  void removedPolyline(Polyline polyline);

  void setPolygons(List<Polygon> polygons);

  void clearedPolygons();

  void addedPolygon(Polygon polygon);

  void removedPolygon(Polygon polygon);
}
