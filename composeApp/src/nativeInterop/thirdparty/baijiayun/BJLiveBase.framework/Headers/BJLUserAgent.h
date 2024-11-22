//
//  BJLUserAgent.h
//  M9Dev
//
//  Created by MingLQ on 2017-08-28.
//  Copyright (c) 2017 MingLQ <minglq.9@gmail.com>. Released under the MIT license.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJLUserAgent: NSObject

@property (nonatomic, readonly) NSString *sdkUserAgent;
- (NSString *)userAgentWithDefault:(nullable NSString *)defaultUserAgent;

- (void)registerSDK:(NSString *)name version:(NSString *)version;
- (void)registerSDK:(NSString *)name version:(NSString *)version description:(nullable NSString *)description;

+ (instancetype)defaultInstance;

@end

NS_ASSUME_NONNULL_END
