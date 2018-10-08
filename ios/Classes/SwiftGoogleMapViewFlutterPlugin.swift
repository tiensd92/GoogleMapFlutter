import Flutter
import UIKit
import GoogleMaps

public class SwiftGoogleMapViewFlutterPlugin: NSObject, FlutterPlugin {
    public static func register(with registrar: FlutterPluginRegistrar) {
        if let rootView = UIApplication.shared.delegate?.window??.rootViewController?.view {
            rootView.backgroundColor = UIColor.clear
            rootView.layer.backgroundColor = UIColor.clear.cgColor
        }
        
        let channel = FlutterMethodChannel(name: "com.dino.googlemap.viewflutter", binaryMessenger: registrar.messenger())
        let instance = SwiftGoogleMapViewFlutterPlugin()
        registrar.addMethodCallDelegate(instance, channel: channel)
    }
    
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        if call.method == "show" {
            if let viewController = UIApplication.shared.delegate?.window??.rootViewController as? FlutterViewController, let arguments = call.arguments as? Dictionary<String, Any> {
                var currentMapView = viewController.view.subviews.first(where: {String.init(describing: $0).contains("GMSMapView")})
                let mapOptions : Dictionary<String, Any> = arguments["mapOptions"] as! Dictionary<String, Any>
                let padding = arguments["padding"] as! Dictionary<String, Double>
                
                /*let showUserLocation = arguments["showUserLocation"] as! Bool
                 let showMyLocationButton = arguments["showMyLocationButton"] as! Bool
                 let showCompassButton = arguments["showCompassButton"] as! Bool*/
                
                if currentMapView == nil {
                    let mapController = UIViewController.init()
                    let camera = GMSCameraPosition.camera(withLatitude: -33.86, longitude: 151.20, zoom: 6.0)
                    let mapView = GMSMapView.map(withFrame: CGRect.zero, camera: camera)
                    
                    viewController.addChildViewController(mapController)
                    viewController.view.addSubview(mapController.view)
                    
                    mapController.view = mapView
                    currentMapView = mapController.view
                    
                    let topConstraint = NSLayoutConstraint(item: mapController.view, attribute: .top, relatedBy: .equal,
                                                           toItem: viewController.view, attribute: .top,
                                                           multiplier: 1.0, constant: CGFloat(padding["top"]!))
                    let bottomConstraint = NSLayoutConstraint(item: mapController.view, attribute: .bottom, relatedBy: .equal,
                                                           toItem: viewController.view, attribute: .bottom,
                                                           multiplier: 1.0, constant: CGFloat(padding["bottom"]!))
                    let leftConstraint = NSLayoutConstraint(item: mapController.view, attribute: .leading, relatedBy: .equal,
                                                           toItem: viewController.view, attribute: .leading,
                                                           multiplier: 1.0, constant: CGFloat(padding["left"]!))
                    let rightConstraint = NSLayoutConstraint(item: mapController.view, attribute: .trailing, relatedBy: .equal,
                                                           toItem: viewController.view, attribute: .trailing,
                                                           multiplier: 1.0, constant: CGFloat(padding["right"]!))
                    
                    viewController.view.addConstraints([topConstraint, bottomConstraint, leftConstraint, rightConstraint])
                    mapController.view.layer.zPosition = 1
                    
                    viewController.view.layoutIfNeeded()
                    mapView.layoutIfNeeded()
                    
                    viewController.view.subviews.forEach {
                        uiView in
                        print(String.init(describing: uiView))
                        
                        /*if !String.init(describing: uiView).contains("GMSMapView") {
                         uiView.bringSubview(toFront: mapController.view)
                         }*/
                    }
                }
                
                let mapView = currentMapView as! GMSMapView
                
                if let cameraDict = arguments["cameraPosition"] as? Dictionary<String, Any> {
                    mapView.moveCamera(GMSCameraUpdate.setCamera(SwiftGoogleMapViewFlutterPlugin.getCameraPosition(map: cameraDict)))
                }
                
                /*mapView.padding = UIEdgeInsets.init(top: CGFloat(padding["top"]!), left: CGFloat(padding["left"]!), bottom: CGFloat(padding["bottom"]!), right: CGFloat(padding["right"]!))*/
            }
        }
    }
    
    static func getCameraPosition(map: Dictionary<String, Any>) -> GMSCameraPosition {
        let latitude = map["latitude"] as! Double
        let longitude = map["longitude"] as! Double
        let zoom = map["zoom"] as! Double
        
        return GMSCameraPosition.init(target: CLLocationCoordinate2D.init(latitude: latitude, longitude: longitude), zoom: Float(zoom), bearing: 0, viewingAngle: 0)
    }
}
