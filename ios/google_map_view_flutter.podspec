#
# To learn more about a Podspec see http://guides.cocoapods.org/syntax/podspec.html
#
Pod::Spec.new do |s|
  s.name             = 'google_map_view_flutter'
  s.version          = '0.0.1'
  s.summary          = 'Google Map Flutter with native sdk'
  s.description      = <<-DESC
Google Map Flutter with native sdk
                       DESC
  s.homepage         = 'http://example.com'
  s.license          = { :file => '../LICENSE' }
  s.author           = { 'Dino' => 'tiensd92@gmail.com' }
  s.source           = { :path => '.' }
  s.source_files = 'Classes/**/*'
  s.public_header_files = 'Classes/**/*.h'
  s.dependency 'Flutter'
  s.dependency 'GoogleMaps'
  s.dependency 'GooglePlaces'
  s.ios.deployment_target = '8.0'
  s.static_framework = true
end