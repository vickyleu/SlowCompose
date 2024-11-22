//
//  BJLNetworking+BaijiaYun.h
//  BJLiveBase
//
//  Created by MingLQ on 2017-09-05.
//  Copyright © 2017 MingLQ <minglq.9@gmail.com>. Released under the MIT license.
//

#import "BJLNetworking.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJLNetworking (BaijiaYun)

+ (instancetype)bjl_internalManagerWithBaseURL:(nullable NSURL *)url;
+ (instancetype)bjl_internalManagerWithBaseURL:(nullable NSURL *)url requestHeader:(nullable NSDictionary *)header;

@end

#define BJLInternalNetworking [BJLNetworking bjl_internalManager]

#pragma mark -

typedef NS_ENUM(NSInteger, BJLResponseCode) {
    BJLResponseCodeSuccess = 0,
    BJLResponseCodeFailure = 1
};

@interface BJLJSONResponse : NSObject <BJLResponse>

@property (nonatomic, readonly) BJLResponseCode code;
@property (nonatomic, readonly, nullable) NSString *message;

@property (nonatomic, readonly) NSTimeInterval timestamp;
@property (nonatomic, readonly, nullable) NSDictionary *data;

@end

@interface BJLMutableJSONResponse : BJLJSONResponse

// success, responseObject: readonly, dynamic

@property (nonatomic, readwrite) BJLResponseCode code;
@property (nonatomic, readwrite, nullable) NSString *message;

@property (nonatomic, readwrite) NSTimeInterval timestamp;
@property (nonatomic, readwrite, nullable) NSDictionary *data;
@property (nonatomic, readwrite, nullable) NSError *error;

@end

NS_ASSUME_NONNULL_END
