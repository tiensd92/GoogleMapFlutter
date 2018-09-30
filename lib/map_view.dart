import 'dart:async';
import 'dart:core';

import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter/widgets.dart';
import 'indoor_building.dart';
import 'camera_position.dart';
import 'marker.dart';
import 'polygon.dart';
import 'polyline.dart';
import 'location.dart';
import 'map_options.dart';
import 'map_controller.dart';
import 'google_map_view_flutter.dart';

class MapView extends StatefulWidget
    implements ListenerController, GetListener {
  final double width;
  final double height;
  final EdgeInsets padding;
  Color backgroundColor = Colors.white;
  bool isReady = false;
  bool isLoading = false;
  _MapView state;
  final MapOptions mapOptions;
  MapController mapController;
  final LocationCallBack locationUpdated;
  final ReadyCallBack onMapReady;
  final MarkerCallBack annotationTapped;
  final AnnotationCallBack annotationDragStart;
  final AnnotationCallBack annotationDragEnd;
  final AnnotationCallBack annotationDrag;
  final PolylineCallBack polylineTapped;
  final PolygonCallBack polygonTapped;
  final MarkerCallBack infoWindowTapped;
  final LocationCallBack mapTapped;
  final LocationCallBack mapLongTapped;
  final CameraPositionCallBack cameraPositionChanged;
  final IndoorBuildingCallBack indoorBuildingActivated;
  final IndoorLevelCallBack indoorLevelActivated;
  Map<String, Marker> _annotations = {};
  Map<String, Polyline> _polylines = {};
  Map<String, Polygon> _polygons = {};

  List<Marker> get markers => _annotations.values.toList(growable: false);

  List<Polyline> get polylines => _polylines.values.toList(growable: false);

  List<Polygon> get polygons => _polygons.values.toList(growable: false);

  MapView(
      {@required this.mapOptions,
      this.padding,
      this.width,
      this.height,
      this.backgroundColor,
      this.locationUpdated,
      this.onMapReady,
      this.annotationTapped,
      this.annotationDragStart,
      this.annotationDragEnd,
      this.annotationDrag,
      this.polylineTapped,
      this.polygonTapped,
      this.infoWindowTapped,
      this.mapTapped,
      this.mapLongTapped,
      this.cameraPositionChanged,
      this.indoorBuildingActivated,
      this.indoorLevelActivated}) {
    GoogleMapViewFlutter.channel.setMethodCallHandler(_handleMethod);
  }

  void dismiss() {
    _annotations.clear();
    _polylines.clear();
    _polygons.clear();
    GoogleMapViewFlutter.channel.invokeMethod('dismiss');
  }

  @override
  void setMarkers(List<Marker> annotations) {
    _annotations.clear();
    annotations.forEach((a) => _annotations[a.id] = a);
    GoogleMapViewFlutter.channel.invokeMethod('setAnnotations',
        annotations.map((a) => a.toMap()).toList(growable: false));
  }

  @override
  void clearedAnnotations() {
    GoogleMapViewFlutter.channel.invokeMethod('clearAnnotations');
    _annotations.clear();
  }

  @override
  void removedMarker(Marker marker) {
    if (!_annotations.containsKey(marker.id)) {
      return;
    }

    _annotations.remove(marker.id);
    GoogleMapViewFlutter.channel
        .invokeMethod('removeAnnotation', marker.toMap());
  }

  @override
  void setPolylines(List<Polyline> polylines) {
    _polylines.clear();
    polylines.forEach((a) => _polylines[a.id] = a);
    GoogleMapViewFlutter.channel.invokeMethod('setPolylines',
        polylines.map((a) => a.toMap()).toList(growable: false));
  }

  @override
  void addedMarker(Marker marker) {
    if (_annotations.containsKey(marker.id)) {
      return;
    }

    _annotations[marker.id] = marker;
    GoogleMapViewFlutter.channel.invokeMethod('addAnnotation', marker.toMap());
  }

  @override
  void clearedPolylines() {
    GoogleMapViewFlutter.channel.invokeMethod('clearPolylines');
    _polylines.clear();
  }

  @override
  void addedPolyline(Polyline polyline) {
    if (_polylines.containsKey(polyline.id)) {
      return;
    }

    _polylines[polyline.id] = polyline;
    GoogleMapViewFlutter.channel.invokeMethod('addPolyline', polyline.toMap());
  }

  @override
  void removedPolyline(Polyline polyline) {
    if (!_polylines.containsKey(polyline.id)) {
      return;
    }

    _polylines.remove(polyline.id);
    GoogleMapViewFlutter.channel
        .invokeMethod('removePolyline', polyline.toMap());
  }

  @override
  void setPolygons(List<Polygon> polygons) {
    _polygons.clear();
    polygons.forEach((a) => _polygons[a.id] = a);
    GoogleMapViewFlutter.channel.invokeMethod(
        'setPolygons', polygons.map((a) => a.toMap()).toList(growable: false));
  }

  @override
  void clearedPolygons() {
    GoogleMapViewFlutter.channel.invokeMethod('clearPolygons');
    _polygons.clear();
  }

  @override
  void addedPolygon(Polygon polygon) {
    if (_polygons.containsKey(polygon.id)) {
      return;
    }
    _polygons[polygon.id] = polygon;
    GoogleMapViewFlutter.channel.invokeMethod('addPolygon', polygon.toMap());
  }

  @override
  void removedPolygon(Polygon polygon) {
    if (!_polygons.containsKey(polygon.id)) {
      return;
    }

    _polygons.remove(polygon.id);
    GoogleMapViewFlutter.channel.invokeMethod('removePolygon', polygon.toMap());
  }

  @override
  List<Marker> getAnnotations() {
    return markers;
  }

  @override
  List<Polygon> getPolygons() {
    return polygons;
  }

  @override
  List<Polyline> getPolylines() {
    return polylines;
  }

  Future<dynamic> _handleMethod(MethodCall call) async {
    switch (call.method) {
      case 'onMapReady':
        state?.onReady();
        mapController = MapController(this);
        onMapReady?.call(mapController);

        break;
      case 'locationUpdated':
        Map args = call.arguments;
        locationUpdated?.call(Location.fromMapFull(args));

        break;
      case 'annotationTapped':
        String id = call.arguments;
        var annotation = _annotations[id];

        if (annotation != null) {
          annotationTapped?.call(annotation);
        }

        break;
      case 'annotationDragStart':
        String id = call.arguments['id'];
        var annotation = _annotations[id];
        var latitude = call.arguments['latitude'];
        var longitude = call.arguments['longitude'];

        if (annotation != null) {
          Map<Marker, Location> map = new Map();
          map.putIfAbsent(annotation, () => new Location(latitude, longitude));
          annotationDragStart?.call(map);
        }

        break;
      case 'annotationDragEnd':
        String id = call.arguments['id'];
        var annotation = _annotations[id];
        var latitude = call.arguments['latitude'];
        var longitude = call.arguments['longitude'];

        if (annotation != null) {
          Map<Marker, Location> map = new Map();
          map.putIfAbsent(annotation, () => new Location(latitude, longitude));
          annotationDragEnd?.call(map);
        }

        break;
      case 'annotationDrag':
        String id = call.arguments['id'];
        var annotation = _annotations[id];
        var latitude = call.arguments['latitude'];
        var longitude = call.arguments['longitude'];

        if (annotation != null) {
          Map<Marker, Location> map = new Map();
          map.putIfAbsent(annotation, () => new Location(latitude, longitude));
          annotationDrag?.call(map);
        }

        break;
      case 'polylineTapped':
        String id = call.arguments;
        var polyline = _polylines[id];

        if (polyline != null) {
          polylineTapped?.call(polyline);
        }

        break;
      case 'polygonTapped':
        String id = call.arguments;
        var polygon = _polygons[id];

        if (polygon != null) {
          polygonTapped?.call(polygon);
        }

        break;
      case 'infoWindowTapped':
        String id = call.arguments;
        var annotation = _annotations[id];

        if (annotation != null) {
          infoWindowTapped?.call(annotation);
        }

        break;
      case 'mapTapped':
        Map locationMap = call.arguments;
        Location location = new Location.fromMap(locationMap);
        mapTapped?.call(location);

        break;
      case 'mapLongTapped':
        Map locationMap = call.arguments;
        Location location = Location.fromMap(locationMap);
        mapLongTapped?.call(location);

        break;
      case 'cameraPositionChanged':
        cameraPositionChanged?.call(CameraPosition.fromMap(call.arguments));

        break;
      case 'indoorBuildingActivated':
        if (call.arguments == null) {
          indoorBuildingActivated?.call(null);
        } else {
          List<IndoorLevel> levels = [];
          for (var value in call.arguments['levels']) {
            levels.add(IndoorLevel(value['name'], value['shortName']));
          }

          indoorBuildingActivated?.call(IndoorBuilding(
              call.arguments['underground'],
              call.arguments['defaultLevelIndex'],
              levels));
        }

        break;
      case 'indoorLevelActivated':
        if (call.arguments == null) {
          indoorLevelActivated?.call(null);
        } else {
          indoorLevelActivated?.call(
              IndoorLevel(call.arguments['name'], call.arguments['shortName']));
        }

        break;
    }

    return null;
  }

  @override
  State<MapView> createState() {
    state = _MapView();

    return state;
  }
}

class _MapView extends State<MapView> {
  @override
  void initState() {
    super.initState();

    Timer(Duration(seconds: 1), () {
      if (widget.isLoading) return;

      RenderBox renderBox = context.findRenderObject();
      Offset offset = renderBox.globalToLocal(Offset.zero);
      Size screenSize = MediaQuery.of(context).size;
      Size widgetSize = context.size;
      double top = offset.dy.abs();
      double left = offset.dx.abs();
      double bottom = screenSize.height - widgetSize.height - top;
      double right = screenSize.width - widgetSize.width - left;

      widget.isLoading = true;
      show(EdgeInsets.only(left: left, top: top, right: right, bottom: bottom));
    });
  }

  @override
  void dispose() {
    onDismiss();
    widget.dismiss();
    super.dispose();
  }

  void onReady() {
    setState(() {
      widget.isLoading = false;
      widget.isReady = true;
    });
  }

  void onDismiss() {
    setState(() {
      widget.isLoading = false;
      widget.isReady = false;
    });
  }

  Future<dynamic> show(EdgeInsets edgeInsets) {
    List<Map> actions = [];

    Map<String, double> padding = {
      'top': edgeInsets.top,
      'left': edgeInsets.left,
      'right': edgeInsets.right,
      'bottom': edgeInsets.bottom
    };

    print(widget.mapOptions.toMap());

    return GoogleMapViewFlutter.channel.invokeMethod('show', {
      'mapOptions': widget.mapOptions.toMap(),
      'actions': actions,
      'padding': padding
    });
  }

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTapUp: (detail) {
        print('onTapUp');
      },
      onTapDown: (detail) {
        print('onTapDown');
      },
      onPanUpdate: (detail) {
        print('onPanUpdate');
      },
      onPanStart: (detail) {
        print('onPanStart');
      },
      onPanCancel: () {
        print('onPanCancel');
      },
      onPanDown: (detail) {
        print('onPanDown');
      },
      onPanEnd: (detail) {
        print('onPanEnd');
      },
      onTapCancel: () {
        print('onTapCancel');
      },
      child: Container(
        padding: widget.padding,
        width: widget.width,
        height: widget.height,
        color: widget.isReady ? Colors.transparent : widget.backgroundColor,
      ),
    );
  }
}

abstract class GetListener {
  List<Marker> getAnnotations();

  List<Polyline> getPolylines();

  List<Polygon> getPolygons();
}
