import CryptoKit
import Foundation

// @objc public class KCrypto : NSObject {
//     @objc(sha256:) public class func sha256(data: Data) -> Data {
//         return SHA256
//             .hash(data: data)
//             .data
//     }
// }

@objc public class KCrypto : NSObject {
    @objc(sha256:) public class func sha256(string: String) -> Data {
        return SHA256
            .hash(data: string.data(using: .ascii)!)
            .data
    }
}

extension Digest {
    var bytes: [UInt8] { Array(makeIterator()) }
    var data: Data { Data(bytes) }
}