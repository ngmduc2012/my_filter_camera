part of my_filter_camera;

/// The facing of a camera.
enum CameraFacing {
  /// Front facing camera.
  front,

  /// Back facing camera.
  back,
}

enum MobileScannerState { undetermined, authorized, denied }

/// The state of torch.
enum TorchState {
  /// Torch is off.
  off,

  /// Torch is on.
  on,
}

enum Ratio { ratio_4_3, ratio_16_9 }

class CameraViewController {
  /// STEP I | setup method

  static const String nameMethod = 'my_filter_camera';
  static const MethodChannel _methods = MethodChannel('$nameMethod/methods');
  final StreamController<MethodCall> _methodStream =
      StreamController.broadcast();
  late final ValueNotifier<CameraFacing> cameraFacingState;
  final ValueNotifier<CameraArguments?> args = ValueNotifier(null);
  final ValueNotifier<TorchState> torchState = ValueNotifier(TorchState.off);
  static int? _controllerHashcode;

  // StreamSubscription? events;

  final Ratio? ratio;
  final bool? torchEnabled;
  final bool? faceDetected;
  final bool? iseKYC;
  CameraFacing facing;
  bool hasTorch = false;
  bool autoResume;
  late StreamController<CameraData> facesController;

  Stream<CameraData> get faces => facesController.stream;

  CameraViewController({
    this.facing = CameraFacing.front,
    this.ratio,
    this.torchEnabled,
    this.faceDetected,
    this.iseKYC,
    this.autoResume = true,
    // this.formats,
  }) {
    // In case a new instance is created before calling dispose()
    if (_controllerHashcode != null) {
      stop();
    }
    _controllerHashcode = hashCode;

    cameraFacingState = ValueNotifier(facing);
    facesController = StreamController.broadcast(
        // onListen: () => setAnalyzeMode(AnalyzeMode.barcode.index),
        // onCancel: () => setAnalyzeMode(AnalyzeMode.none.index),
        );
  }

  // 1.1: Create stream method
  bool _initialized = false;

  Future<dynamic> _initFlutterBluePlus() async {
    if (_initialized) {
      return;
    }
    _initialized = true;
    _methods.setMethodCallHandler((call) async {
      _updateMethodStream(call);
    });
  }

  /// Disposes the MobileScannerController and closes all listeners.
  void dispose() {
    if (hashCode == _controllerHashcode) {
      stop();
      // events?.cancel();
      // events = null;
      _controllerHashcode = null;
    }

    facesController.close();
  }

  // 1.2: invoke a platform method
  Future<dynamic> _invokeMethod(String method, [dynamic arguments]) async {
    dynamic out;
    print("_invokeMethod | $method | $arguments");
    try {
      _initFlutterBluePlus();
      out = await _methods.invokeMethod(method, arguments);
    } catch (e) {
      rethrow;
    }
    return out;
  }

  // 1.3: Update Stream _methodStream on flutter
  void _updateMethodStream(MethodCall methodCall) {
    _methodStream.add(methodCall);
  }

  /// STEP II | Talk (and get) to native
  //1. CHECK PERMISSION
  // Future<bool> checkPermission() async {
  //   try {
  //     final bool hasPermission = await _invokeMethod('checkPermission');
  //     return hasPermission;
  //   } on PlatformException catch (error) {
  //     debugPrint('${error.code}: ${error.message}');
  //     return false;
  //   }
  // }
  Future<bool> checkPermission() async {
    try {
      // Lắng nghe kết quả từ stream
      var responseStream = cameraTurnOnResponseStream;

      // Bắt đầu lắng nghe trước khi yêu cầu quyền để không bỏ lỡ phản hồi
      Future<CameraTurnOnResponse> futureResponse = responseStream.first;

      // Yêu cầu quyền từ native
       await _invokeMethod('checkPermission');

      // Chờ phản hồi từ stream
      CameraTurnOnResponse response = await futureResponse;

      if (!response.userAccepted) {
        debugPrint('Camera permission denied.');
        return false;
      }

      return true;
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
      return false;
    }
  }

  bool isStarting = false;

  Future<void> start() async {
    if (isStarting) {
      throw Exception('face_detection: Called start() while already starting.');
    }
    isStarting = true;

    final bool hasPermission = await checkPermission();
    if (!hasPermission) {
      debugPrint('Permissions not granted.');
      isStarting = false;
      return;
    }
    cameraFacingState.value = facing;
    final Map<dynamic, dynamic> arguments = {};
    arguments['facing'] = facing.index;
    if (ratio != null) {
      arguments['ratio'] = (ratio == Ratio.ratio_4_3) ? 1 : 0;
    }
    if (torchEnabled != null) arguments['torch'] = torchEnabled;
    if (faceDetected != null) arguments['faceDetected'] = faceDetected;
    if (iseKYC != null) arguments['iseKYC'] = iseKYC;

    Map<dynamic, dynamic>? startResult;
    try {
      startResult = await _invokeMethod('start', arguments);
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
      isStarting = false;
      return;
    }

    if (startResult == null) {
      isStarting = false;
      throw PlatformException(code: 'INITIALIZATION ERROR');
    }

    hasTorch = startResult['torchable'] as bool? ?? false;

    if (kIsWeb) {
      args.value = CameraArguments(
        webId: startResult['ViewID'] as String?,
        size: Size(
          startResult['videoWidth'] as double? ?? 0,
          startResult['videoHeight'] as double? ?? 0,
        ),
        hasTorch: hasTorch,
      );
    } else {
      args.value = CameraArguments(
        textureId: startResult['textureId'] as int?,
        size: toSize(startResult['size'] as Map? ?? {}),
        hasTorch: hasTorch,
      );
    }
    isStarting = false;
  }

  Future<void> stop() async {
    try {
      await _invokeMethod('stop');
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
    }
  }

  /// Switches the torch on or off.
  ///
  /// Only works if torch is available.

  Future<void> switchCamera() async {
    try {
      await _invokeMethod('stop');
    } on PlatformException catch (error) {
      debugPrint(
        '${error.code}: camera is stopped! Please start before switching camera.',
      );
      return;
    }
    facing =
        facing == CameraFacing.back ? CameraFacing.front : CameraFacing.back;
    await start();
  }

  Future<XFile> capture() async {
    try {
      final result = await _invokeMethod('capture');

      if (result != null && result is Map) {
        // Extract details from the result
        final String path = result['path'] as String;
        final String name = result['name'] as String;
        final String mimeType = result['mimeType'] as String;
        final int size = result['size'] as int;

        // Create and return an XFile
        return XFile(path, name: name, mimeType: mimeType, length: size);
      } else {
        throw PlatformException(
          code: 'CAPTURE_FAILED',
          message: 'Failed to capture image',
        );
      }
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
      rethrow;
    }
  }

  /// start video
  Future<void> startVideoRecording() async {
    try {
      await _invokeMethod('startVideoRecording');
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
    }
  }

  /// stop video);
  Future<XFile> stopVideoRecording() async {
    try {
      final result = await _invokeMethod('stopVideoRecording');
      if (result != null) {
        String path = result['path'];
        String name = result['name'];
        String mimeType = result['mimeType'];
        int size = result['size'];

        // Use the path to create an XFile
        XFile videoFile =
            XFile(path, mimeType: mimeType, name: name, length: size);
        print('Video saved at: ${videoFile.path}');
        return videoFile;
      }
    } on PlatformException catch (e) {
      print('Failed to stop recording: ${e.message}');
    }
    throw Exception('Failed to stop recording and retrieve video file');
  }

  /// pause video
  Future<void> pauseVideoRecording() async {
    try {
      await _invokeMethod('pauseVideoRecording');
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
    }
  }

  /// resume video
  Future<void> resumeVideoRecording() async {
    try {
      await _invokeMethod('resumeVideoRecording');
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
    }
  }

  //updateFilter
  Future<String> updateFilter({
    required int filterType,
  }) async {
    final Map<dynamic, dynamic> data = {};
    data['filterType'] = filterType;
    try {
      final result = await _invokeMethod("updateFilter", data);
      return result;
    } catch (e) {
      throw Exception("Failed to update filter: $e");
    }
  }

  Future<List<ImageData>> applyImageSample() async {
    try {
      final result = await _invokeMethod('applyImageSample');

      if (result != null && result is Map) {
        final List<dynamic> images = result['images'];

        List<ImageData> imageDataList = [];

        for (var imageData in images) {
          final String code = imageData['code'] as String;
          final String base64Image = imageData['image'] as String;

          // Tạo đối tượng ImageData với Enum
          final filterType = PluginFilterEnum.values.firstWhere(
              (e) => e.code == int.parse(code),
              orElse: () => PluginFilterEnum.NORMAL);

          imageDataList
              .add(ImageData(filterType: filterType, base64: base64Image));
        }

        return imageDataList;
      } else {
        throw PlatformException(
          code: 'FILTERS_FAILED',
          message: 'Failed to apply filters',
        );
      }
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
      rethrow;
    }
  }

  Future<int> adjustBrightness({required int value}) async {
    try {
      final Map<dynamic, dynamic> arguments = {};
      arguments['brightnessValue'] = value;
      final result = await _invokeMethod('adjustBrightness', arguments);
      return result;
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
      rethrow;
    }
  }

  Future<String> removeDir() async {
    try {
      final result = await _invokeMethod('removeDir');
      return result;
    } on PlatformException catch (error) {
      debugPrint('${error.code}: ${error.message}');
      rethrow;
    }
  }

  //
  // /// Checks if the MobileScannerController is bound to the correct MobileScanner object.
  // void ensure(String name) {
  //   final message =
  //       'MobileScannerController.$name called after MobileScannerController.dispose\n'
  //       'MobileScannerController methods should not be used after calling dispose.';
  //   assert(hashCode == _controllerHashcode, message);
  // }

  /// 2.2 | Type 2 Native -> Flutter: Listen native
  Stream<ListenState> get listenState async* {
    yield* _methodStream.stream
        .where((m) => m.method == "OnSendResult")
        .map((m) => m.arguments)
        .map((args) => ListenState.fromMap(args));
  }

  Stream<String> get connectionState async* {
    yield* _methodStream.stream
        .where((m) => m.method == "OnConnectionStateChanged")
        .map((m) => m.arguments)
        .map((args) {
      print("OK");

      return "OK";
    });
  }

  Stream<CameraTurnOnResponse> get cameraTurnOnResponseStream {
    return _methodStream.stream
        .where((m) => m.method == "OnCameraTurnOnResponse")
        .map((m) => m.arguments)
        .map((args) => CameraTurnOnResponse.fromMap(args));
  }

  Stream<CameraRunningResponse> get cameraRunningResponseStream {
    return _methodStream.stream
        .where((m) => m.method == "onRunningCamera")
        .map((m) => m.arguments)
        .map((args) => CameraRunningResponse.fromMap(args));
  }

/*  /// STEP II | Talk (and get) to native
  /// 2.1 | Type 1 Flutter -> Native -> Flutter
  Future<int> exTalk({
    int exMessage = 1,
  }) async {
    final Map<dynamic, dynamic> data = {};
    data['ex_message'] = exMessage;
    return await _invokeMethod('exTalk', data);
  }

  /// 2.2 | Type 2 Native -> Flutter: Listen native
  Stream<ListenState2> get listenState2 async* {
    yield* _methodStream.stream
        .where((m) => m.method == "OnListenStateChanged")
        .map((m) => m.arguments)
        .map((args) => ListenState2.fromMap(args));
  }*/
}
