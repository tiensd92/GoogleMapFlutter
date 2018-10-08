#import "GoogleMapViewFlutterPlugin.h"
#import <google_map_view_flutter/google_map_view_flutter-Swift.h>

@implementation GoogleMapViewFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftGoogleMapViewFlutterPlugin registerWithRegistrar:registrar];
}
@end
