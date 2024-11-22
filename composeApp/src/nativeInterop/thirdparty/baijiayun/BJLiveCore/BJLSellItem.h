//
//  BJLSellItem.h
//  BJLiveCore
//
//  Created by 凡义 on 2020/11/24.
//  Copyright © 2020 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

/**
 直播带货商品
 */

@interface BJLSellItem: NSObject

@property (nonatomic, copy, readonly) NSString *goodID, *name, *coverURL;
@property (nonatomic, copy, readonly) NSString *orderIndex;
@property (nonatomic, copy, readonly) NSString *buyURL, *describeContent;

@property (nonatomic, copy, readonly) NSString *creatTime;
@property (nonatomic, assign, readonly) CGFloat originalPrice;
@property (nonatomic, assign, readonly) CGFloat discountPrice;
@property (nonatomic, assign, readonly) BOOL isDiscountPriceHide;

// 商品是否上架, 需要根据上下架的信令更新
@property (nonatomic, assign, readwrite) BOOL isOnShelf;

// 是否是当前正在讲解的商品
@property (nonatomic, assign, readwrite) BOOL inExplaining;

@end

typedef NS_ENUM(NSInteger, BJLRecommendItemType) {
    BJLRecommendItemType_Image,
    BJLRecommendItemType_Link
};

@interface BJLRecommendItem: NSObject

@property (nonatomic, copy, readonly) NSString *cardID, *roomID, *partnerID;
@property (nonatomic, copy, readonly) NSString *name, *imageURLString, *dec;
@property (nonatomic, copy, readonly) NSString *linkURLString;

@property (nonatomic, assign, readonly) BJLRecommendItemType type;
@property (nonatomic, copy, readonly) NSString *creatTime;
@property (nonatomic, copy, readonly) NSString *updateTime;

@end

NS_ASSUME_NONNULL_END
