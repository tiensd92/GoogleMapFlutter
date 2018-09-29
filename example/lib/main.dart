import 'package:flutter/material.dart';
import 'package:googlemapflutter/map_options.dart';
import 'package:googlemapflutter/map_view.dart';

void main() => runApp(new MyApp());

class MyApp extends StatefulWidget {
  @override
  _MyAppState createState() => new _MyAppState();
}

class _MyAppState extends State<MyApp> {

  @override
  void initState() {
    super.initState();
}

  @override
  Widget build(BuildContext context) {
    return new MaterialApp(
      home: new Scaffold(
		backgroundColor: Colors.transparent,
        appBar: new AppBar(
          title: const Text('Plugin example app'),
        ),
        body: MapView(
          onMapReady: (){
            print("Map Ready");
          },
          mapOptions: MapOptions(showCompassButton: true, showMyLocationButton: true, showUserLocation: true),
        ),
      ),
    );
  }
}
