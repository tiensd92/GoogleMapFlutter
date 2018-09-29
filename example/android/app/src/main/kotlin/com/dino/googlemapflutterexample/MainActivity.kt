package com.dino.googlemapflutterexample

import android.os.Bundle

import io.flutter.plugins.GeneratedPluginRegistrant

class MainActivity() : io.flutter.app.FlutterFragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        GeneratedPluginRegistrant.registerWith(this)
    }
}
