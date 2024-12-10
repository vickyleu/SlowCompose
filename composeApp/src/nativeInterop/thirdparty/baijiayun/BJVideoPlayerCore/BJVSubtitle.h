//
//  BJVSubtitle.h
//  BJVideoPlayerCore
//
//  Created by xijia dai on 2019/12/20.
//  Copyright © 2019 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface BJVSubtitle: NSObject

@property (nonatomic, readonly) NSTimeInterval startTime; // format second . millisecond, Accurate to thousands
@property (nonatomic, readonly) NSTimeInterval endTime; // format second . millisecond, Accurate to thousands
@property (nonatomic, readonly) NSString *content; // maybe muti rows, separated by '\n'

@end

@interface BJVSubtitleInfo: NSObject <BJLYYModel>

@property (nonatomic, readonly) NSString *name;
@property (nonatomic, readonly) NSString *ID;
@property (nonatomic, readonly) NSString *enurlString;// 英文字母
@property (nonatomic, readonly) NSString *zhurlString;// 中文字幕
@property (nonatomic, readonly) NSString *urlString;//非双语字幕的默认字幕
@property (nonatomic, readonly) BOOL isDefaultSubtitle;
@property (nonatomic, nullable, readonly) NSArray<BJVSubtitle *> *subtitles;
@property (nonatomic, nullable, readonly) NSArray<BJVSubtitle *> *ensubtitles;
@property (nonatomic, nullable, readonly) NSArray<BJVSubtitle *> *zhsubtitles;

@end

NS_ASSUME_NONNULL_END
