//
//  BJVMessageVM.h
//  BJPlayerManagerCore
//
//  Created by HuangJie on 2018/5/11.
//  Copyright © 2018年 BaijiaYun. All rights reserved.
//

#import "BJVBaseVM.h"
#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface BJVMessageVM: BJVBaseVM

// 消息覆盖更新
- (BJLObservable)receivedMessagesDidOverwrite:(nullable NSArray<BJLMessage *> *)messages;

// 消息增量更新
- (BJLObservable)didReceiveMessages:(nullable NSArray<BJLMessage *> *)messages;

#pragma mark - gift

- (BJLObservable)didReceiveNewGift:(NSArray<NSDictionary *> *)newGiftArray;

/** 收到直播打赏
 #param shouldShowSpecial 是否展示大图特效
 */
- (BJLObservable)receivedAnmationAwardWithImageUrl:(NSString *)imageUrlString fromUsrName:(NSString *)userName shouldShowSpecialEffect:(BOOL)shouldShowSpecial;

@end

NS_ASSUME_NONNULL_END
