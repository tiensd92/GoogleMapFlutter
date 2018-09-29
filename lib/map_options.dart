import 'camera_position.dart';
import 'location.dart';
import 'map_view_type.dart';

class MapOptions {
  /// Allows the app to receive location updates.
  final bool showUserLocation;
  /// show/hide the button to center the map on the user location.
  ///
  /// Requires showUserLocation to be true.
  final bool showMyLocationButton;
  /// show/hide the compass button on the map.
  ///
  /// Normally is not visible all the time. Becomes visible when the orientation
  /// of the map is changes through gesture.
  final bool showCompassButton;

  final CameraPosition initialCameraPosition;
  static const CameraPosition _defaultCamera =
      const CameraPosition(const Location(45.5329661, -122.7059508), 12.0);
  MapViewType mapViewType;

  MapOptions(
      {this.showUserLocation: false,
      this.showMyLocationButton: false,
      this.showCompassButton: false,
      this.initialCameraPosition: _defaultCamera,
      this.mapViewType: MapViewType.normal});

  Map<String, dynamic> toMap() {
    return {
      "showUserLocation": showUserLocation,
      "showMyLocationButton": showMyLocationButton,
      "showCompassButton": showCompassButton,
      "cameraPosition": initialCameraPosition.toMap(),
      "mapViewType": getMapTypeName(mapViewType)
    };
  }

  String getMapTypeName(MapViewType mapType) {
    String mapTypeName = "normal";
    switch (mapType) {
      case MapViewType.none:
        mapTypeName = "none";
        break;
      case MapViewType.satellite:
        mapTypeName = "satellite";
        break;
      case MapViewType.terrain:
        mapTypeName = "terrain";
        break;
      case MapViewType.hybrid:
        mapTypeName = "hybrid";
        break;
      case MapViewType.normal:
        mapTypeName = "normal";
        break;
    }
    return mapTypeName;
  }
}
