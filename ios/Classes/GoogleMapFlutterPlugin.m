#import "GoogleMapFlutterPlugin.h"
#import <googlemapflutter/googlemapflutter-Swift.h>

@implementation GooglMapFlutterPlugin
+ (void)registerWithRegistrar:(NSObject<FlutterPluginRegistrar>*)registrar {
  [SwiftGoogleMapFlutterPlugin registerWithRegistrar:registrar];
}
@end
