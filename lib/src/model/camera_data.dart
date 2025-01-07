import 'rot.dart';

class CameraData {
  final int? size;
  final int? guidID;
  final String? faceImage;
  final String? flippedFaceImage;
  final int? eKYCID;
  final Rot? rot;

  CameraData(
      {
        this.size,
        this.guidID,
        this.faceImage,
        this.flippedFaceImage,
        this.eKYCID,
        this.rot});

  /// Create a [face] from native data.
  CameraData.fromNative(
      Map data,
      )   :
        size = data['size'] as int?,
        guidID = data['guidID'] as int?,
        faceImage = data['faceImage'] as String?,
        flippedFaceImage = data['flippedFaceImage'] as String?,
        eKYCID = data['eKYCID'] as int?,
        rot = Rot.fromNative(data['rot'] as Map? ?? {});
}
