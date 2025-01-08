part of my_filter_camera;

/// Camera args for [CameraView].
class CameraArguments {
  /// The texture id.
  final int? textureId;

  /// Size of the texture.
  final Size size;

  final bool hasTorch;

  final String? webId;

  /// Create a [CameraArguments].
  CameraArguments({
    this.textureId,
    required this.size,
    required this.hasTorch,
    this.webId,
  });
}

enum ListenStateEnum {
  torchState,
  faceAndroid,
  barcodeMac,
  barcodeWeb;

  static ListenStateEnum getEnum(String value) {
    return ListenStateEnum.values
        .firstWhere((e) => e.toString().split('.').last == value);
  }
}

class ListenState {
  final ListenStateEnum name;
  final dynamic data;

  ListenState({
    required this.name,
    this.data,
  });

  factory ListenState.fromMap(Map<dynamic, dynamic> json) {
    return ListenState(
      name: ListenStateEnum.getEnum(json['name']),
      data: json['data'],
    );
  }

  Map<dynamic, dynamic> toMap() {
    return {
      'name': name.toString().split('.').last,
      'data': data,
    };
  }
}

class CameraTurnOnResponse {
  bool userAccepted;

  CameraTurnOnResponse({
    required this.userAccepted,
  });

  factory CameraTurnOnResponse.fromMap(Map<dynamic, dynamic> json) {
    return CameraTurnOnResponse(
      userAccepted: json['user_accepted'],
    );
  }
}

class CameraRunningResponse {
  bool isCameraRunning;

  CameraRunningResponse({
    required this.isCameraRunning,
  });

  factory CameraRunningResponse.fromMap(Map<dynamic, dynamic> json) {
    return CameraRunningResponse(
      isCameraRunning: json['isCameraRunning'],
    );
  }
}

enum ErrorPlatform {
  fbp,
  android,
  apple,
}

enum FbpErrorCode {
  success,
  timeout,
  androidOnly,
  applePlatformOnly,
  createBondFailed,
  removeBondFailed,
  deviceIsDisconnected,
  serviceNotFound,
  characteristicNotFound,
  adapterIsOff,
  connectionCanceled,
  userRejected
}

class FlutterBluePlusException implements Exception {
  /// Which platform did the error occur on?
  final ErrorPlatform platform;

  /// Which function failed?
  final String function;

  /// note: depends on platform
  final int? code;

  /// note: depends on platform
  final String? description;

  FlutterBluePlusException(
      this.platform, this.function, this.code, this.description);

  @override
  String toString() {
    String sPlatform = platform.toString().split('.').last;
    return 'FlutterBluePlusException | $function | $sPlatform-code: $code | $description';
  }

  @Deprecated('Use function instead')
  String get errorName => function;

  @Deprecated('Use code instead')
  int? get errorCode => code;

  @Deprecated('Use description instead')
  String? get errorString => description;
}

enum PluginFilterEnum {
  NORMAL(
    9,
    "Original",
  ),
  GRAYSCALE(0, "GrayScale"),
  MONOCHROME(1, "MonoChrome"),
  SEPIA(2, "Sepia"),
  SKETCH(
    3,
    "Sketch",
  ),
  SMOOTH_TOON(4, "Smooth Toon"),
  VIGNETTE(
    5,
    "Vignette",
  ),
  BULGE_DISTORTION(
    6,
    "Bulge Distortion zoom",
  ),
  BULGE_DISTORTION2(
    7,
    "Bulge Distortion",
  ),
  SWIRL(
    8,
    "Swirl",
  ),
  LUMINANCE(
    27,
    "Luminance",
  ),
  BLEND_ALPHA(
    28,
    "Blend Alpha",
  ),
  HUE(
    29,
    "Hue",
  );

  const PluginFilterEnum(this.code, this.name);

  final int code;
  final String name;
}

class ImageData {
  final PluginFilterEnum
      filterType; // Thay đổi kiểu tên từ String thành FilterType
  final String base64;

  ImageData({
    required this.filterType,
    required this.base64,
  });

  // Hàm để chuyển enum thành tên String nếu cần
  String get filterName => filterType.toString().split('.').last;
}

/*class ListenState2 {
  ListenStateEnum2 state;

  ListenState2({
    required this.state
  });

  Map<dynamic, dynamic> toMap() {
    final Map<dynamic, dynamic> data = {};
    data['state'] = state;
    return data;
  }

  factory ListenState2.fromMap(Map<dynamic, dynamic> json) {
    return ListenState2(
      state: ListenStateEnum2.getEnum(json['state']),
    );
  }
}

enum ListenStateEnum2 {
  working(0),
  free(1);

  const ListenStateEnum2( this.code);

  final int code;

  static ListenStateEnum2 getEnum(int code){
    try {
      return ListenStateEnum2.values.firstWhere((element) => element.code == code,);
    } catch (e) {
      return ListenStateEnum2.working;
    }
  }

}*/
