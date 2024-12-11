package org.uooc.compose.network

import io.ktor.client.engine.darwin.ChallengeHandler
import kotlinx.cinterop.alloc
import kotlinx.cinterop.cValue
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.ptr
import kotlinx.cinterop.value
import platform.CoreFoundation.CFErrorCopyDescription
import platform.CoreFoundation.CFErrorCopyFailureReason
import platform.CoreFoundation.CFErrorCopyRecoverySuggestion
import platform.CoreFoundation.CFErrorCopyUserInfo
import platform.CoreFoundation.CFErrorRefVar
import platform.Foundation.CFBridgingRelease
import platform.Foundation.NSOperatingSystemVersion
import platform.Foundation.NSProcessInfo
import platform.Foundation.NSURLAuthenticationChallenge
import platform.Foundation.NSURLAuthenticationMethodServerTrust
import platform.Foundation.NSURLCredential
import platform.Foundation.NSURLSession
import platform.Foundation.NSURLSessionAuthChallengeCancelAuthenticationChallenge
import platform.Foundation.NSURLSessionAuthChallengeDisposition
import platform.Foundation.NSURLSessionAuthChallengePerformDefaultHandling
import platform.Foundation.NSURLSessionAuthChallengeUseCredential
import platform.Foundation.NSURLSessionTask
import platform.Foundation.credentialForTrust
import platform.Foundation.serverTrust
import platform.Security.SecTrustCopyCertificateChain
import platform.Security.SecTrustEvaluate
import platform.Security.SecTrustEvaluateWithError
import platform.Security.SecTrustRef
import platform.Security.SecTrustResultTypeVar
import platform.Security.SecTrustSetAnchorCertificates
import platform.Security.errSecSuccess
import platform.Security.kSecTrustResultInvalid
import platform.Security.kSecTrustResultProceed
import platform.Security.kSecTrustResultUnspecified

/**
 * Challenge handler which trusts whatever certificate the server presents
 * This needs to be used in combination with plist additions:
 * <code>
 *     <key>NSAppTransportSecurity</key>
 *     <dict>
 *         <key>NSExceptionDomains</key>
 *         <dict>
 *             <key>example.com</key>
 *             <dict>
 *                 <key>NSExceptionAllowsInsecureHTTPLoads</key>
 *                 <true/>
 *             </dict>
 *         </dict>
 *     </dict>
 * </code>
 * Supporting links:
 * - https://developer.apple.com/documentation/bundleresources/information_property_list/nsexceptionallowsinsecurehttploads
 * - https://developer.apple.com/documentation/foundation/url_loading_system/handling_an_authentication_challenge/performing_manual_server_trust_authentication
 */

/**
 * Evaluates trust for the specified certificate and policies.
 */
internal fun SecTrustRef.trustIsValid(): Boolean {
    var isValid = false

    val version = cValue<NSOperatingSystemVersion> {
        majorVersion = 12
        minorVersion = 0
        patchVersion = 0
    }
    if (NSProcessInfo().isOperatingSystemAtLeastVersion(version)) {
        memScoped {
            val result = alloc<CFErrorRefVar>()
            // https://developer.apple.com/documentation/security/2980705-sectrustevaluatewitherror
            isValid = SecTrustEvaluateWithError(this@trustIsValid, result.ptr)
        }
    } else {
        // https://developer.apple.com/documentation/security/1394363-sectrustevaluate
        memScoped {
            val result = alloc<SecTrustResultTypeVar>()
            result.value = kSecTrustResultInvalid
            val status = SecTrustEvaluate(this@trustIsValid, result.ptr)
            if (status == errSecSuccess) {
                isValid = result.value == kSecTrustResultUnspecified ||
                    result.value == kSecTrustResultProceed
            }
        }
    }

    return isValid
}