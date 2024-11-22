//
//  BJVTokenManager.h
//  BJVideoPlayerCore
//
//  Created by ney on 2023/6/19.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BJVDownloadManager.h"

NS_ASSUME_NONNULL_BEGIN
@protocol BJVRequestTokenDelegate <NSObject>

@optional
- (void)requestTokenWithVideoID:(NSString *)videoID
                     completion:(void (^)(NSString *_Nullable token, NSError *_Nullable error))completion;
- (void)requestTokenWithClassID:(NSString *)classID
                      sessionID:(nullable NSString *)sessionID
                     completion:(void (^)(NSString *_Nullable token, NSError *_Nullable error))completion;

@end

@interface BJVTokenManager: NSObject

@property (class, nonatomic, weak, nullable) id<BJVRequestTokenDelegate> tokenDelegate;

@end
NS_ASSUME_NONNULL_END
