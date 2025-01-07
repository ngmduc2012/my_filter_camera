import Flutter
import UIKit

// MARK: FlutterBlueDsgPlugin
public class MyFilterCamera: NSObject, FlutterPlugin {

    // MARK: I.Step 1: Setup method
    var talking = TalkingWithFlutter.shared

    public static func register(with registrar: FlutterPluginRegistrar) {
        let instance = MyFilterCamera()
        instance.talking.methodChannel = FlutterMethodChannel.init(name: "my_filter_camera/methods", binaryMessenger: registrar.messenger())

        registrar.addMethodCallDelegate(instance, channel: instance.talking.methodChannel!)
    }

    // FlutterResult return a result.
    public func handle(_ call: FlutterMethodCall, result: @escaping FlutterResult) {
        Utils.log( call.method)
        switch call.method {

        case "exTalk":
            exTalk(call: call, result: result)

        default:
            result(FlutterMethodNotImplemented)
        }
    }

    func exTalk(call: FlutterMethodCall ,result: @escaping FlutterResult) {
        let args = call.arguments as? NSDictionary
        let copies = args?["ex_message"] as? Int
        Utils.log( "copies \(copies) ")

        talking.onListenStateChanged(state: talking.FREE)

        result(NSNumber(value: talking.WORKING))
    }

}
