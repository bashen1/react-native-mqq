require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name          = package['name']
  s.version       = package['version']
  s.summary       = package['description']
  s.description   = package['description']
  s.homepage      = package['homepage']
  s.license       = package['license']
  s.author        = package['author']
  s.platform      = :ios, "9.0"
  s.source        = { :git => "https://github.com/bashen1/react-native-qq.git", :tag => "master" }
  s.source_files  = "ios/RCTQQAPI/RCTQQAPI.{h,m}"
  s.requires_arc  = true
  s.vendored_frameworks = "ios/RCTQQAPI/*.xcframework"
  s.libraries = 'iconv', 'sqlite3', 'c++', 'z'

  s.dependency "React-Core"

end

