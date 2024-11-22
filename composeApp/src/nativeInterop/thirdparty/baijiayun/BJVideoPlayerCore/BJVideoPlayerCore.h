//
//  BJVideoPlayerCore.h
//  BaijiaYun
//
//  Created by MingLQ on 2018-04-17.
//

#import <Foundation/Foundation.h>

#if __has_include("BJVPlayerManager.h")
#import "BJVPlayerManager.h"
#endif

#if __has_include("BJVRoom.h")
#import "BJVRoom.h"
#endif

NS_ASSUME_NONNULL_BEGIN

FOUNDATION_EXPORT NSString *BJVideoPlayerCoreName(void);
FOUNDATION_EXPORT NSString *BJVideoPlayerCoreVersion(void);

NS_ASSUME_NONNULL_END
