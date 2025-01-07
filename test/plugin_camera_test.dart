// import 'package:flutter_test/flutter_test.dart';
// import 'package:plugin_camera/plugin_camera.dart';
// import 'package:plugin_camera/plugin_camera_platform_interface.dart';
// import 'package:plugin_camera/plugin_camera_method_channel.dart';
// import 'package:plugin_platform_interface/plugin_platform_interface.dart';
//
// class MockPluginCameraPlatform
//     with MockPlatformInterfaceMixin
//     implements PluginCameraPlatform {
//
//   @override
//   Future<String?> getPlatformVersion() => Future.value('42');
// }
//
// void main() {
//   final PluginCameraPlatform initialPlatform = PluginCameraPlatform.instance;
//
//   test('$MethodChannelPluginCamera is the default instance', () {
//     expect(initialPlatform, isInstanceOf<MethodChannelPluginCamera>());
//   });
//
//   test('getPlatformVersion', () async {
//     MyFilterCamera pluginCameraPlugin = MyFilterCamera();
//     MockPluginCameraPlatform fakePlatform = MockPluginCameraPlatform();
//     PluginCameraPlatform.instance = fakePlatform;
//
//     expect(await pluginCameraPlugin.getPlatformVersion(), '42');
//   });
// }
