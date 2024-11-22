//
//  BJVUser.h
//  BJVideoPlayerCore
//
//  Created by xyp on 2021/7/3.
//  Copyright © 2021 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

NS_ASSUME_NONNULL_BEGIN

/** 用户 */
@interface BJVUser: BJLUser

@end

NS_ASSUME_NONNULL_END
