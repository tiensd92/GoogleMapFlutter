import 'package:flutter/material.dart';
import 'package:google_map_view_flutter/map_options.dart';
import 'package:google_map_view_flutter/map_view.dart';

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
        body: Container(
            width: double.infinity,
            height: double.infinity,
            child: Column(
              children: <Widget>[
                Flexible(
                    child: MapView(
                        onMapReady: (controller) {
                            print("Map Ready");
                        },
                        mapOptions: MapOptions(
                                showCompassButton: true,
                                showMyLocationButton: true,
                                showUserLocation: true),
                    ),
                ),
                Container(
                  color: Colors.red,
                  height: 100.0,
                )
              ],
            )),
      ),
    );
  }
}
