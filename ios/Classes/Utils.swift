import Foundation

struct Utils {
    
    enum LogLevel : Int {
        case lnone = 0
        case lerror = 1
        case lwarning = 2
        case linfo = 3
        case ldebug = 4
        case lverbose = 5
    }
    
    static func log(_ message: Any, level: LogLevel = LogLevel.ldebug, tag: String = "") {
        switch level {
        case LogLevel.ldebug:
            debugPrint(printString(message, tag: tag))
        default:
            debugPrint(printString(message, tag: tag))
        }
    }
    
    static func printString(_ message: Any, tag: String = "") -> String{
        return "[MyFilterCamera-iOS] |\(tag == "" ? "" : " \(tag) |") \(message)"
    }
    
    // Learn more: https://docs.swift.org/swift-book/documentation/the-swift-programming-language/thebasics#Debugging-with-Assertions
    static func myAssert(_ condition: Bool, _ message: String){
        assert(condition, message)
    }
    static func myAssertionFailure(_ message: String){
        assertionFailure(message)
    }
    
    // Learn more: https://docs.swift.org/swift-book/documentation/the-swift-programming-language/thebasics#Enforcing-Preconditions
    static func myPrecondition(_ condition: Bool, _ message: String){
        precondition(condition, message)
    }
    static func myPreconditionFailure(_ message: String){
        preconditionFailure(message)
    }
}

extension Data {
    var myToStringFromUtf8: String {
        return self.map { String(format: "0x%02hhx", $0) }.joined(separator: " ")
    }
}

// Learn more: https://docs.swift.org/swift-book/documentation/the-swift-programming-language/stringsandcharacters#Substrings
extension String {
    func mySubstrings(stringStop: Character) -> String {
        let index = self.firstIndex(of: stringStop) ?? self.endIndex
        let beginning = self[..<index]
        
        return String(beginning)
    }
}




