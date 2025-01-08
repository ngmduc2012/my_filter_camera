import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:my_filter_camera/my_filter_camera.dart';

class CameraView extends StatefulWidget {
  const CameraView({
    super.key,
    this.controller,
    required this.onDetect,
    this.fit = BoxFit.cover,
    this.viewType = 'custom_camera_view',
    this.autoDispose = true,
  });

  final CameraViewController? controller;
  final bool autoDispose;

  /// Function that gets called when a Barcode is detected.
  ///
  /// [data] The barcode object with all information about the scanned code.
  /// [args] Information about the state of the MobileScanner widget
  final Function(CameraData data, CameraArguments? args) onDetect;

  /// Handles how the widget should fit the screen.
  final BoxFit fit;
  final String viewType;

  @override
  State<CameraView> createState() => _CameraViewState();
}

class _CameraViewState extends State<CameraView> with WidgetsBindingObserver {
  late CameraViewController controller;

  @override
  void initState() {
    super.initState();

    WidgetsBinding.instance.addObserver(this);
    controller = widget.controller ?? CameraViewController();
    if (!controller.isStarting) controller.start();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        if (!controller.isStarting && controller.autoResume) {
          controller.start();
        }
        break;
      case AppLifecycleState.inactive:
      case AppLifecycleState.paused:
      case AppLifecycleState.detached:
        controller.stop();
        break;
      case AppLifecycleState.hidden:
      // TODO: Handle this case.
    }
  }

  // String? lastScanned;
  @override
  Widget build(BuildContext context) {
    return ValueListenableBuilder(
      valueListenable: controller.args,
      builder: (context, value, child) {
        value = value;
        if (value == null) {
          return const ColoredBox(color: Colors.transparent);
        } else {
          // widget.onDetect( value! as CameraArguments);
          return SizedBox(
            width: MediaQuery.of(context).size.width,
            height: MediaQuery.of(context).size.height,
            child: SizedBox(
              width: value.size.width,
              height: value.size.height,
              child: kIsWeb
                  ? HtmlElementView(viewType: value.webId!)
                  : AndroidView(
                      viewType: widget.viewType,
                      layoutDirection: TextDirection.ltr,
                      creationParams: null,
                      creationParamsCodec: const StandardMessageCodec(),
                    ),
              //    : Texture(textureId: value.textureId!)
              //     : Column(children: [
              //   Container(
              //     height: 600,
              //       width: 600,
              //       child: Texture(textureId: value.textureId!)),
              //   Container(
              //     height: 600,
              //     width: 600,
              //     child: const AndroidView(
              //                 viewType: 'custom_camera_view',
              //                 layoutDirection: TextDirection.ltr,
              //                 creationParams: null,
              //                 creationParamsCodec: StandardMessageCodec(),
              //               ),
              //   )
              // ],)
            ),
          );
        }
      },
    );
  }

  @override
  void didUpdateWidget(covariant CameraView oldWidget) {
    super.didUpdateWidget(oldWidget);
    if (oldWidget.controller == null) {
      if (widget.controller != null) {
        controller.dispose();
        controller = widget.controller!;
      }
    } else {
      if (widget.controller == null) {
        controller = CameraViewController();
      } else if (oldWidget.controller != widget.controller) {
        controller = widget.controller!;
      }
    }
  }

  @override
  void dispose() {
    super.dispose();
    if (widget.autoDispose) {
      controller.dispose();
    }
  }
}
