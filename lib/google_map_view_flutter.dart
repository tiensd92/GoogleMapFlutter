
import 'package:flutter/services.dart';
import 'map_controller.dart';
import 'indoor_building.dart';
import 'camera_position.dart';
import 'polygon.dart';
import 'polyline.dart';
import 'location.dart';
import 'marker.dart';

typedef LocationCallBack = void Function(Location);
typedef MarkerCallBack = void Function(Marker);
typedef AnnotationCallBack =  void Function(Map<Marker, Location>);
typedef PolylineCallBack = void Function(Polyline);
typedef PolygonCallBack = void Function(Polygon);
typedef CameraPositionCallBack = void Function(CameraPosition);
typedef IndoorBuildingCallBack = void Function(IndoorBuilding);
typedef IndoorLevelCallBack = void Function(IndoorLevel);
typedef ReadyCallBack = void Function(MapController);

class GoogleMapViewFlutter {
  static const MethodChannel _channel = const MethodChannel('com.dino.googlemap.viewflutter');
  static get channel => _channel;
}