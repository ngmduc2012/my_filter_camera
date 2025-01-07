import 'package:flutter/material.dart';

Size toSize(Map data) {
  final width = data['width'] as double;
  final height = data['height'] as double;
  return Size(width, height);
}