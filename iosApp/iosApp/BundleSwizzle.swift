//
//  BundleSwizzle.swift
//  iosApp
//
//  Created by vicky Leu on 2024/12/9.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import iosMath
import ObjectiveC.runtime

extension Bundle {
    static let swizzleBundleForClass: Void = {
        guard let originalMethod = class_getClassMethod(Bundle.self, #selector(Bundle.init(for:))),
              let swizzledMethod = class_getClassMethod(Bundle.self, #selector(swizzled_bundle(for:))) else {
            print("Swizzling failed: Unable to find methods.")
            return
        }
        
        method_exchangeImplementations(originalMethod, swizzledMethod)
    }()
    
    @objc
    private class func swizzled_bundle(for aClass: AnyClass) -> Bundle {
        let className = NSStringFromClass(aClass)
//        if className.hasSuffix("MTFont") { // 匹配类名尾部
//            let frameworkBundlePath = Bundle.main.bundlePath + "/Frameworks/iosMath.framework/" //mathFonts.bundle
//            // 使用 FileManager 检查文件是否存在
//            if FileManager.default.fileExists(atPath: frameworkBundlePath) {
//                if let frameworkBundle = Bundle(path: frameworkBundlePath) {
//                    print("Framework bundle path: \(frameworkBundle.bundlePath)")
////                    return Bundle.main
//                    return frameworkBundle
//                }
//            }
//        }
        
        // 调用原始实现
        return swizzled_bundle(for: aClass)
    }
}

extension MTFont {

    // 用于Swizzling的静态属性
    static let swizzle: Void = {
        let originalSelector = NSSelectorFromString("fontBundle")  // 类方法需要使用字符串的形式
        let swizzledSelector = #selector(MTFont.swizzled_fontBundle)

        // 获取类方法
        guard let originalMethod = class_getClassMethod(MTFont.self, originalSelector),
              let swizzledMethod = class_getClassMethod(MTFont.self, swizzledSelector) else {
            return
        }
        
        // 交换方法实现
        method_exchangeImplementations(originalMethod, swizzledMethod)
    }()

    // 定义新的 fontBundle 方法 (swizzled 版本)
    @objc class func swizzled_fontBundle() -> Bundle? {
        print("Swizzled fontBundle called.")
        let mathFontsBundlePath = Bundle.main.bundlePath + "/Frameworks/iosMath.framework/mathFonts.bundle"
        if FileManager.default.fileExists(atPath: mathFontsBundlePath) {
            return Bundle(path: mathFontsBundlePath)
        }
        // 如果没有找到自定义的 bundle，则返回原始的 bundle
        return self.swizzled_fontBundle() // 调用已交换的原始方法
    }
}
