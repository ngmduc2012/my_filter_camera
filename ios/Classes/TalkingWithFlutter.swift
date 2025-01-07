// Learn more: https://docs.swift.org/swift-book/documentation/the-swift-programming-language/classesandstructures#Comparing-Structures-and-Classes


import Flutter
import Foundation
import CoreBluetooth

class TalkingWithFlutter : NSObject{
    
    // share func
    static let shared = TalkingWithFlutter()
    
    override private init() {
        super.init()
    }
    
    deinit {
    }
    
    init(methodChannel: FlutterMethodChannel? = nil) {
        self.methodChannel = methodChannel
    }
    
    var methodChannel : FlutterMethodChannel?

    let WORKING = 0
    let FREE = 1
    func onListenStateChanged(state: Int){
        let response = [
            "state": state,
        ]
        if methodChannel != nil {
            methodChannel!.invokeMethod("OnListenStateChanged", arguments: response)
            Utils.log("OnListenStateChanged \(response)")
        }
    }  

}







