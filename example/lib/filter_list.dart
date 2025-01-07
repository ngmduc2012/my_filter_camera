import 'dart:convert';
import 'package:flutter/material.dart';
import 'package:my_filter_camera/my_filter_camera.dart';

class FilterList extends StatefulWidget {
  const FilterList({super.key, required this.controller});
  final CameraViewController controller;

  @override
  State<FilterList> createState() => _FilterListState();
}

class _FilterListState extends State<FilterList> {
  List<ImageData> _imageDataList = [];

  @override
  void initState() {
    super.initState();
    _fetchFilteredImages();
  }

  Future<void> _fetchFilteredImages() async {
    final imageDataList = await widget.controller.applyImageSample();
    setState(() {
      _imageDataList = imageDataList;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.maxFinite,
      height: 300,
      child: ListView.builder(
        itemCount: _imageDataList.length,
        scrollDirection: Axis.horizontal,
        itemBuilder: (context, index) {
          final imageData = _imageDataList[index];
          return Column(
            children: [
              ElevatedButton(
                onPressed: () async {
                  await widget.controller.updateFilter(filterType: imageData.filterType.code);
                },
                child: Text(imageData.filterType.name),
              ),
              Image.memory(
                base64Decode(imageData.base64),
                width: 100,
                height: 100,
              ),
            ],
          );
        },
      ),
    );
  }
}
