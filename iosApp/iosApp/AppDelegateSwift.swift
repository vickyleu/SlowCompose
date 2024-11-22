//
//  AppDelegate.swift
//  iosApp
//
//  Created by vicky Leu on 2024/2/20.
//  Copyright © 2024 orgName. All rights reserved.
//

import Foundation
import UIKit
import ComposeApp
import AVFoundation
//import UoocOnlines
//@available(iOS, deprecated: 14.0)
@main
class AppDelegateSwift: UIResponder, UIApplicationDelegate {

    lazy var window: UIWindow? = UIWindow(frame: UIScreen.main.bounds)

    private let delegateImplKt =  AppDelegateImpl()

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        
//        UOCChatWebviewController.Type.self
//        UOCQrcodeConductionController.Type.self

        // 静音模式下，也能播放声音
        do {
            try AVAudioSession.sharedInstance().setCategory(AVAudioSession.Category.playback)
            try AVAudioSession.sharedInstance().setActive(true)

        } catch let error {
            debugPrint(error.localizedDescription)
        }
        let viewController = MainViewControllerKt.MainViewController(delegateImpl: delegateImplKt)
        viewController.view.frame = UIScreen.main.bounds
        viewController.view.backgroundColor=UIColor.white
        window?.rootViewController = viewController
        window?.makeKeyAndVisible()
        return true
    }
}
