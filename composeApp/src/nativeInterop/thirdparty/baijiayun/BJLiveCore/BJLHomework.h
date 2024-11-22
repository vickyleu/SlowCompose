//
//  BJLHomework.h
//  BJLiveCore
//
//  Created by 凡义 on 2020/8/25.
//  Copyright © 2020 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface BJLHomework: NSObject

@property (nonatomic, readonly, copy) NSString *fileID;
@property (nonatomic, readonly, copy) NSString *fileExtension;
@property (nonatomic, readonly, copy, nullable) NSString *homeworkID;
@property (nonatomic, readonly, copy) NSString *fileName;
@property (nonatomic, readonly) BOOL isRelatedFile;
@property (nonatomic, readonly) BOOL isAnimate;
@property (nonatomic, readonly) BOOL canPreview;
@property (nonatomic, readonly) CGFloat byteSize;

@property (nonatomic, readonly, copy) NSString *fromUserNumber, *fromUserName;
@property (nonatomic, readonly) BJLUserRole fromUserRole;
@property (nonatomic, readonly) NSTimeInterval lastTimeInterval;

+ (instancetype)homeworkWithUploadResponseData:(NSDictionary *)responseData;

@end

NS_ASSUME_NONNULL_END
