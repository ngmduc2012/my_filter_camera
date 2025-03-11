
[![GitHub](https://img.shields.io/badge/Nguyen_Duc-GitHub-black?logo=github)](https://github.com/ngmduc2012)
_[![Buy Me A Coffee](https://img.shields.io/badge/Donate-Buy_Me_A_Coffee-blue?logo=buymeacoffee)](https://www.buymeacoffee.com/ducmng12g)_
_[![PayPal](https://img.shields.io/badge/Donate-PayPal-blue?logo=paypal)](https://paypal.me/ngmduc)_
_[![Sponsor](https://img.shields.io/badge/Sponsor-Become_A_Sponsor-blue?logo=githubsponsors)](https://github.com/sponsors/ngmduc2012)_
_[![Support Me on Ko-fi](https://img.shields.io/badge/Donate-Ko_fi-red?logo=ko-fi)](https://ko-fi.com/I2I81AEJG8)_

# Camera with Filters Plugin for Flutter
A Flutter plugin for capturing photos and videos with advanced digital filters. This package provides a seamless interface for applying real-time effects to your camera feed and capturing moments with style and creativity.

## Features
- Capture photos and videos with advanced real-time filters.
- Apply a variety of pre-defined or custom digital effects programmatically.
- Adjustable camera configurations:  brightness.
- Support for front and rear cameras.
- Highly customizable and easy to integrate into your Flutter project.

| Features              | Android | iOS |
|:----------------------|:-------:|:---:|
| Ask permissions       |    ✅    |  ❌  |
| Enable/disable audio  |    ✅    |  ❌  |
| Take photos           |    ✅    |  ❌  |
| Photo live filters    |    ✅    |  ❌  |
| Exposure level        |    ✅    |  ❌  |
| Device flash support  |    ✅    |  ❌  |
| Live switching camera |    ✅    |  ❌  |


## Installation and usage
[Example](https://github.com/ngmduc2012/my_bluetooth/blob/master/example/lib/main.dart)
### Add the package to your pubspec.yaml
```yaml
dependencies:
  my_filter_camera: ^latest_version
```
### Set up platform 
- Android
Add permission to your `AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.CAMERA"/>
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.RECORD_AUDIO" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
```
### Import the package to your project
```dart
import 'package:my_filter_camera/my_filter_camera.dart';
```
And then, you can use camera function.

## Initialize camera
```dart
final CameraViewController controller= CameraViewController();
```
- Display camera preview widget
```dart
CameraView(
  autoDispose: false,
  controller: controller,
  onDetect: (data, args) {
    setState(() {});
  },
),
```
- To start camera, use `controller.start()` function, and use `controller.stop` to stop camera. 
- `controller.switch()` change front and rear camera direction
## Filter types
![Filter type](docs/filter_demo.gif)
- Update filter type:
```dart
await controller.updateFilter(filterType: PluginFilterEnum.values[index].code,);
```
- You can find all filter list in `PluginFilterEnum` 

## Capture Image
![Capture Image](docs/capture.gif)
- To capture realtime image, use:
```dart
final xFile = await controller.capture();
```

## Exposure level
- To adjust Camera lever:
```dart
controller.adjustBrightness(value: value.toInt());
```
## Important note!!!!
Remember stop or dispose camera when not using camera anymore to avoid increase ram memory
```dart
 @override
void dispose() {
  controller.dispose();
  super.dispose();
}
//or
controller.stop();

```
## Contribute
Any comments please contact us [ThaoDoan](https://github.com/mia140602) and [DucNguyen](https://github.com/ngmduc2012)
