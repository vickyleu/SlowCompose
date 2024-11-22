//
//  BJLSellVM+swift.h
//  BJLiveCore
//
//  Created by ney on 2022/2/25.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import "BJLSellVM.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJLSellVM (swift)
- (id<BJLObservation>)sw_didReceiveSellGoodsUpdate:(BJLControlObserving (^)(void))block NS_SWIFT_NAME(sw_didReceiveSellGoodsUpdate(_:));
- (id<BJLObservation>)sw_didReceiveSellGoodsOnshelfStateUpdate:(BJLControlObserving (^)(void))block NS_SWIFT_NAME(sw_didReceiveSellGoodsOnshelfStateUpdate(_:));
- (id<BJLObservation>)sw_didReceiveShowShopping:(BJLControlObserving (^)(BOOL showShopping))block NS_SWIFT_NAME(sw_didReceiveShowShopping(_:));
- (id<BJLObservation>)sw_didReceiveShowPrice:(BJLControlObserving (^)(BOOL showPrice))block NS_SWIFT_NAME(sw_didReceiveShowPrice(_:));
- (id<BJLObservation>)sw_didReceiveEnableStudentSpeakApply:(BJLControlObserving (^)(BOOL enable))block NS_SWIFT_NAME(sw_didReceiveEnableStudentSpeakApply(_:));
- (id<BJLObservation>)sw_didReceiveStreamerLikeCountUpdate:(BJLControlObserving (^)(NSInteger likeCount))block NS_SWIFT_NAME(sw_didReceiveStreamerLikeCountUpdate(_:));
- (id<BJLObservation>)sw_didReceiveRecommendCard:(BJLControlObserving (^)(NSString *_Nullable cardID))block NS_SWIFT_NAME(sw_didReceiveRecommendCard(_:));
- (id<BJLObservation>)sw_didReceiveUserInWithName:(BJLControlObserving (^)(NSString *userName))block NS_SWIFT_NAME(sw_didReceiveUserInWithName(_:));
- (id<BJLObservation>)sw_didReceiveStopActivity:(BJLControlObserving (^)(void))block NS_SWIFT_NAME(sw_didReceiveStopActivity(_:));
- (id<BJLObservation>)sw_didReceiveActivityInfoChangeWithUserNameGoodsNameActivityModel:(BJLControlObserving (^)(NSString *_Nullable username, NSString *_Nullable goodsName, BJLSellActivityModel *model))block NS_SWIFT_NAME(sw_didReceiveActivityInfoChangeWithUserNameGoodsNameActivityModel(_:));
@end

NS_ASSUME_NONNULL_END
