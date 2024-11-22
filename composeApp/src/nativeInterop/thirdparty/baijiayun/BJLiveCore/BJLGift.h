//
//  BJLGift.h
//  BJLiveCore
//
//  Created by MingLQ on 2016-12-06.
//  Copyright © 2016 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@protocol BJLGift <NSObject>

@property (nonatomic, readonly) BJLGiftType type;
@property (nonatomic, readonly) NSString *price, *name;

@end

@protocol BJLReceivedGift <BJLGift>

@property (nonatomic, readonly) BJLUser *fromUser, *toUser;

@end

#pragma mark -

@interface BJLGift: NSObject <BJLGift>

+ (instancetype)giftWithType:(BJLGiftType)type
                       price:(NSString *)price
                        name:(NSString *)name;

@end

NS_ASSUME_NONNULL_END
