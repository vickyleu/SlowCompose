//
//  BJLSGiftItem.h
//  BJLiveBase
//
//  Created by xyp on 2020/7/28.
//  Copyright © 2020 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BJLYYModel.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, BJLSGiftType) {
    BJLSGiftType_666,
    BJLSGiftType_heart,
    BJLSGiftType_fabulous,
    BJLSGiftType_car,
    BJLSGiftType_flower,
    BJLSGiftType_rocket,
    BJLSGiftType_rainbow,
    BJLSGiftType_star
};

@interface BJLSGiftItem: NSObject <BJLYYModel>

@property (nonatomic) NSString *userName;
@property (nonatomic) NSString *userId;
@property (nonatomic) BJLSGiftType giftID;
@property (nonatomic) NSString *iconCover;
@property (nonatomic) NSString *imgCover;
@property (nonatomic) NSString *price;

// 特殊星标, 礼物界面会有星标提醒, 且礼物会在视频中间大图展示
@property (nonatomic) BOOL specialStar;

+ (BOOL)shouldShowRewardAnimationWith:(BJLSGiftType)giftID;
@end

NS_ASSUME_NONNULL_END
