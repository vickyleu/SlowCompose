//
//  BJVPlayinfoItem.h
//  BJVideoPlayerCore
//
//  Created by 凡义 on 2023/10/13.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//


#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

NS_ASSUME_NONNULL_BEGIN

// 点播专辑、回放合集model
@interface BJVPlayinfoItem : NSObject<BJLYYModel, NSCopying>

@property (nonatomic, strong, readonly) NSString *itemID;

/**
 点播videoID
 */
@property (nonatomic, strong, readonly) NSString *videoID;

/**
 回放roomID
 */
@property (nonatomic, strong, readonly) NSString *roomID;

/**
 回放sessionID
 */
@property (nonatomic, strong, readonly) NSString *sessionID;

/**
 播放token
 */
@property (nonatomic, strong, readonly) NSString *token;

/**
 裁剪后的回放视频的version
 #discussion  -1: 裁剪后的主版本,  0: 裁剪前的原始视频.
 #discussion 默认 clipedVersion 为 -1
 */
@property (nonatomic, assign, readonly) NSInteger clipedVersion;

/**
 回放名称
 */
@property (nonatomic, strong, readonly) NSString *name;

/**
 回放时长
 
 #discussion xx:xx:xx
 */
@property (nonatomic, strong, readonly) NSString *length;

/**
 回放封面图
 */
@property (nonatomic, strong, readonly, nullable) NSString *prefaceUrl;

@property (nonatomic, strong, readonly) NSString *createTime;

/**
 回放视频是否过期
 */
@property (nonatomic, readonly) BOOL isExpired;

@end

NS_ASSUME_NONNULL_END
