//
//  BJLSellActivityModel.h
//  BJLiveCore
//
//  Created by 凡义 on 2024/5/24.
//  Copyright © 2024 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJLSellActivityModel : NSObject

@property (nonatomic, readonly) BOOL show;
@property (nonatomic, readonly) BOOL isKeepShow;
@property (nonatomic, readonly) BOOL isShowVirtualRecord;
@property (nonatomic, readonly) NSString *activityID;
@property (nonatomic, readonly) NSString *activityName;
@property (nonatomic, readonly) NSString *activityURLString;
@property (nonatomic, readonly) NSString *activityImage;
@property (nonatomic, readonly) NSInteger duration;
@property (nonatomic, readonly) NSInteger frequency;

@end

NS_ASSUME_NONNULL_END
