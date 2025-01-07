import 'package:flutter/material.dart';

class MyLifeCircleWidget extends StatefulWidget {

  /*
  Learn more: https://docs.flutter.dev/get-started/flutter-for/android-devs#how-do-i-listen-to-android-activity-lifecycle-events
   */

  final Widget child;

  final Function()? onResumed;
  final Function()? onInactive;
  final Function()? onPaused;
  final Function()? onDetached;
  final Function()? onHidden;

  const MyLifeCircleWidget({super.key, required this.child, this.onResumed, this.onInactive, this.onPaused, this.onDetached, this.onHidden});

  @override
  State<MyLifeCircleWidget> createState() => _MyLifeCircleWidgetState();
}

class _MyLifeCircleWidgetState extends State<MyLifeCircleWidget>  with WidgetsBindingObserver {

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addObserver(this);
  }

  @override
  void dispose() {
    WidgetsBinding.instance.removeObserver(this);
    super.dispose();
  }

  @override
  void didChangeAppLifecycleState(AppLifecycleState state) {
    switch (state) {
      case AppLifecycleState.resumed:
        widget.onResumed?.call();
      case AppLifecycleState.inactive:
        widget.onInactive?.call();
      case AppLifecycleState.paused:
        widget.onPaused?.call();
      case AppLifecycleState.detached:
        widget.onDetached?.call();
      case AppLifecycleState.hidden:
        widget.onHidden?.call();
    }
  }


  @override
  Widget build(BuildContext context) {
    return widget.child;
  }
}
