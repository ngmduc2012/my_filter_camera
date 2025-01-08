import 'dart:async';

import 'package:extended_image/extended_image.dart';
import 'package:flutter/material.dart';
import 'package:my_filter_camera/my_filter_camera.dart';
import 'package:shared_preferences/shared_preferences.dart';

void main() {
  runApp(const MaterialApp(home: CameraTestScreen()));
}

class CameraTestScreen extends StatefulWidget {
  const CameraTestScreen({super.key});

  @override
  _CameraTestScreenState createState() => _CameraTestScreenState();
}

class _CameraTestScreenState extends State<CameraTestScreen> {
  final CameraViewController controller = CameraViewController();

  bool isStarted = false;

  @override
  void initState() {
    super.initState();

    // Subscribe to the video data state from the camera controller

    controller.applyImageSample();
    _loadPreferences();
  }

  // Initialize video player with the recorded video file
  double _currentSliderValue = 0;
  Future<void> _loadPreferences() async {
    // final prefs = await SharedPreferences.getInstance();
    // final int? brightnessValue = prefs.getInt('brightnessValue');
  }

  Future<void> _setPreferences({required int brightnessValue}) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt('brightnessValue', brightnessValue);
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.transparent,
      body: Stack(
        children: [
          Positioned.fill(
            child: CameraView(
              autoDispose: false,
              controller: controller,
              onDetect: (data, args) {
                setState(() {});
              },
            ),
          ),
          // Bottom control panel
          Align(
            alignment: Alignment.bottomCenter,
            child: Container(
              height: 200,
              color: Colors.black.withOpacity(0.4),
              child: Column(
                mainAxisSize: MainAxisSize.min,
                children: [
                  Slider(
                    value: _currentSliderValue,
                    max: 50,
                    min: -50,
                    divisions: 101,
                    label: _currentSliderValue.round().toString(),
                    onChanged: (double value) {
                      setState(() {
                        _currentSliderValue = value;
                        controller.adjustBrightness(value: value.toInt());
                        _setPreferences(brightnessValue: value.toInt());
                      });
                    },
                  ),
                  // Filter selection and controls
                  Container(
                    height: 50,
                    child: ListView.builder(
                      itemCount: PluginFilterEnum.values.length,
                      scrollDirection: Axis.horizontal,
                      itemBuilder: (context, index) {
                        return ElevatedButton(
                          onPressed: () async {
                            await controller.updateFilter(
                              filterType: PluginFilterEnum.values[index].code,
                            );
                          },
                          child: Text(PluginFilterEnum.values[index].name),
                        );
                      },
                    ),
                  ),
                  // Record, stop, and camera switch controls
                  Row(
                    mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                    children: [
                      IconButton(
                        color: Colors.white,
                        icon: ValueListenableBuilder(
                          valueListenable: controller.cameraFacingState,
                          builder: (context, state, child) {
                            switch (state as CameraFacing) {
                              case CameraFacing.front:
                                return const Icon(Icons.camera_front);
                              case CameraFacing.back:
                                return const Icon(Icons.camera_rear);
                            }
                          },
                        ),
                        iconSize: 32.0,
                        onPressed: () =>
                            controller.switchCamera(),
                      ),
                      IconButton(
                        icon: const Icon(
                          Icons.camera_alt_outlined,
                          size: 40,
                          color: Colors.white,
                        ),
                        onPressed: () async {
                          final xFile = await controller.capture();
                          final utf8 = await xFile.readAsBytes();
                          showDialog(
                              context: context,
                              builder: (BuildContext context) {
                                return AlertDialog(
                                  title: const Text('Image preview'),
                                  content: ExtendedImage.memory(
                                    utf8,
                                    fit: BoxFit.cover,
                                    filterQuality: FilterQuality.high,
                                  ),
                                  actions: <Widget>[
                                    TextButton(
                                      child: const Text('Close'),
                                      onPressed: () {
                                        Navigator.of(context)
                                            .pop(); // Đóng dialog
                                      },
                                    ),
                                  ],
                                );
                              });
                        },
                      ),
                    ],
                  ),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  @override
  void dispose() {
    controller.dispose();
    super.dispose();
  }
}
