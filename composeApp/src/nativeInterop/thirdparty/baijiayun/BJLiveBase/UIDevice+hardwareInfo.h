//
//  UIDevice+hardwareInfo.h
//  BJLiveBase
//
//  Created by HuXin on 2022/2/14.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIDevice (hardwareInfo)
+ (NSString *)bjl_deviceModel;
+ (NSString *)bjl_deviceModelName;
+ (void)bjl_updateDeviceOrientationMask:(UIInterfaceOrientationMask)deviceOrientationMask errorHandler:(nullable void (^)(NSError *error))errorHandler NS_SWIFT_NAME(bjl_updateDeviceOrientationMask(_:errorHandler:)) API_AVAILABLE(ios(16.0));
+ (void)bjl_updateDeviceOrientation:(UIInterfaceOrientation)deviceOrientation errorHandler:(nullable void (^)(NSError *error))errorHandler NS_SWIFT_NAME(bjl_updateDeviceOrientation(_:errorHandler:));
@end

NS_ASSUME_NONNULL_END
