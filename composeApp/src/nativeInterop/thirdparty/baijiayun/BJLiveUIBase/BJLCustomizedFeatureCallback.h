//
//  BJLCustomizedFeatureCallback.h
//  BJLiveUIBase-BJLiveUI
//
//  Created by ney on 2023/10/30.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <Webkit/WKWebView.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJLCustomizedFeatureCallback: NSObject
// 自定义打开网页
@property (nonatomic, copy) void (^customWebVCWebviewDidInit) (UIViewController *vc, WKWebView *webview);
@property (nonatomic, copy) void (^customWebVCWebviewWillLoadRequest) (UIViewController *vc, WKWebView *webview, NSURLRequest *request);
@end

NS_ASSUME_NONNULL_END
