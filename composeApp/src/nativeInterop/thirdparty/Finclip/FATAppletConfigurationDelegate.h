//
//  FATAppletConfigurationDelegate.h
//  FinApplet
//
//  Created by 王兆耀 on 2022/9/3.
//  Copyright © 2022 finogeeks. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FATAppletMenuProtocol.h"
#import "FATConstant.h"
#import "FATAppletInfo.h"
#import "FATAppletConfig.h"
#import "FATAppletRequest.h"

NS_ASSUME_NONNULL_BEGIN

@protocol FATAppletConfigurationDelegate <NSObject>

@optional

#pragma mark-- 小程序配置

/// 给小程序设置配置项,使用场景：需要给某个特定小程序设置特殊配置项
/// @param appletInfo 小程序信息
- (FATAppletConfig *)getConfigWithAppletInfo:(FATAppletInfo *)appletInfo;

/// 小程序灰度扩展参数
/// @param appletId 小程序id
- (NSDictionary *)grayExtensionWithAppletId:(NSString *)appletId;

/// 设置小程序的Cookie
/// @param appletId 小程序id
- (NSDictionary *)getCookieWithAppletInfo:(NSString *)appletId;

/// 设置小程序的Cookie
/// @param appletInfo 小程对象
/// 示例
/// NSMutableDictionary *cookieProperties = [NSMutableDictionary dictionary];
/// [cookieProperties setObject:@"Hm_apvt_0d885a47c55a66238287cbd00b79a117" forKey:NSHTTPCookieName];
/// [cookieProperties setObject:@"finogeeks" forKey:NSHTTPCookieValue];
/// [cookieProperties setObject:@".myqcloud.com" forKey:NSHTTPCookieDomain];
/// [cookieProperties setObject:@"finogeeks.com" forKey:NSHTTPCookieOriginURL];
/// [cookieProperties setObject:@"/" forKey:NSHTTPCookiePath];
/// [cookieProperties setObject:@"0" forKey:NSHTTPCookieVersion];
/// [cookieProperties setObject:[NSDate dateWithTimeIntervalSinceNow:36000] forKey:NSHTTPCookieExpires];
/// return [cookieProperties];
- (NSArray *)getCookieArrayWithAppletInfo:(FATAppletInfo *)appletInfo;

#pragma mark - JS SDK
/// 验证 js-sdk config信息 (未实现该代理方法则默认为验证通过)
/// @param  appletInfo   小程序信息
/// @param  config  验证信息
/// @param  completion  执行完后的回调，验证通过则code返回FATExtensionCodeSuccess，失败返回FATExtensionCodeFailure
- (void)applet:(FATAppletInfo *)appletInfo
 onJSSDKConfig:(NSDictionary *)config
    completion:(void (^)(FATExtensionCode code, NSDictionary *result))completion;

#pragma mark - H5 hook events
/**
 小程序H5中发出的request包含body时会触发该事件
 requestInfo 中会包含
 bodyType: body内容的类型
 requestHref: request做在页面的链接
 requestId：请求id，每次请求都会构造，后续拦截请求时，可在query参数中通过FinClipHookBridge-RequestId匹配
 requestUrl：发起request时，请求的地址。
 value: request的body内容。
 示例
 {
     bodyType = String;
     requestHref = "http://aaronly.gitee.io/aymj";
     requestId = 16499170263357297;
     requestUrl = "https://www.finclip.com/api/v1/mop/mop-fast-trial-manager/web/visits/statistics";
     value = "{\"type\":\"download_click\"}";
 };
 */
- (void)applet:(FATAppletInfo *)appletInfo hookRequestInfo:(NSDictionary *)requestInfo;



/// web-view组件加载第一个网页的时候会触发这个函数，开发者可以回调 decisionHandler 来决定是否进行正常加载
/// - Parameters:
///   - appletInfo: 小程序信息
///   - urlString: 要加载的网页链接
///   - decisionHandler: 是否允许加载的回调
///
- (void)applet:(FATAppletInfo *)appletInfo decidePolicyForFirstURL:(NSString *)urlString decisionHandler:(void (^)(BOOL))decisionHandler;

@end

NS_ASSUME_NONNULL_END
