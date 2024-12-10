//
//  BJLAward.h
//  BJLiveCore
//
//  Created by xyp on 2020/7/30.
//  Copyright © 2020 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "../BJLiveBase/BJLYYModel.h"
NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, BJLAwardType) {
    BJLAwardTypeFree,  // 免费礼物
    BJLAwardTypeMoney, // 现金
    BJLAwardTypeGift, // 现金购买的礼物
    BJLAwardTypeScore  // 积分打赏（移动端暂不支持）
};

// 多种奖励配置
@interface BJLAwardBase: NSObject <BJLYYModel, NSCopying>

@property (nonatomic, readonly) BJLAwardType type;
@property (nonatomic, readonly) BOOL show; // 是否开启打赏

@end

@interface BJLAward: BJLAwardBase <BJLYYModel, NSCopying>

@property (nonatomic, readonly) BOOL enable;
@property (nonatomic, readonly, copy) NSString *key, *logo, *name, *picture;

+ (nullable instancetype)awardForKey:(NSString *)key;
+ (nullable instancetype)awardForName:(NSString *)name;

+ (NSArray<BJLAward *> *)allAwards;
+ (void)setAllAwards:(NSArray<BJLAward *> *)allAwards;

@end

@interface BJLMoneyAward: BJLAwardBase <BJLYYModel, NSCopying>

@property (nonatomic, readonly) NSArray<NSString *> *moneyList;
/// 打赏自定义最小金额
@property (nonatomic, readonly) NSString *minAwardMoney;

@end

@interface BJLAwardInfo: NSObject <BJLYYModel, NSCopying>

@property (nonatomic, readonly) NSString *key;
@property (nonatomic, readonly) NSString *imageURLString, *imageSVGURLString;
@property (nonatomic, readonly) NSString *name;
@property (nonatomic, readonly) NSString *price; // 礼物价格或者奖励的积分数量
@property (nonatomic, readonly) BOOL shouldFloatEffect; // 是否浮窗展示
@property (nonatomic, readonly) BOOL shouldShow; // 是否展示
@property (nonatomic, readonly) BOOL shouldShowSpecial; // 是否展示特效

@end

@interface BJLGiftAward: BJLAwardBase <BJLYYModel, NSCopying>

@property (nonatomic, readonly) NSArray<BJLAwardInfo *> *giftList;

@end

@interface BJLScoreAward: BJLGiftAward <BJLYYModel, NSCopying>

@property (nonatomic, readonly) NSArray<BJLAwardInfo *> *scoreList;

@end


@interface BJLAwardParse : NSObject

+ (nullable BJLAwardBase *)awardFromJSONDictionary:(NSDictionary *)dictionary error:(NSError *__autoreleasing *)error;

@end
NS_ASSUME_NONNULL_END
