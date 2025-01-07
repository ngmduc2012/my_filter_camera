class Rot {
  final double? rotX;
  final double? rotY;
  final double? rotZ;

  Rot({
    this.rotX,
    this.rotY,
    this.rotZ,
  });

  /// Create a [face] from native data.
  Rot.fromNative(Map data, )
      : rotX = data['rotX'] as double?,
        rotY = data['rotY'] as double?,
        rotZ = data['rotZ'] as double?;
}
