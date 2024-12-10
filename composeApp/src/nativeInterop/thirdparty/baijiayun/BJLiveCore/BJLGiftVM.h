//
//  BJLGiftVM.h
//  BJLiveCore
//
//  Created by MingLQ on 2016-12-06.
//  Copyright © 2016 BaijiaYun. All rights reserved.
//

#import "BJLBaseVM.h"

#import "BJLGift.h"
#import "BJLAward.h"

NS_ASSUME_NONNULL_BEGIN

/** ### 打赏 */
@interface BJLGiftVM: BJLBaseVM

@property (nonatomic, assign, readonly) BOOL giftUserDidLogin;

#pragma mark - 新版内购打赏

/** 请求直播打赏的配置数据 */
- (nullable NSURLSessionDataTask *)requestAwardConfigWithComplete:(nullable void (^)(BOOL success, NSArray<BJLAwardBase *> *_Nullable awardList, BJLError *_Nullable error))completion;

/** 请求直播打赏统计数据
 #param awardStatisticDic 打赏记录 key 为BJLAwardType，value为统计数据
 */
- (nullable NSURLSessionDataTask *)requestAwardStatisticsWithComplete:(nullable void (^)(BOOL success, NSDictionary *_Nullable awardStatisticDic, BJLError *_Nullable error))completion;

/** 请求手机号登录验证码 */
- (nullable NSURLSessionDataTask *)requestSMSCodeWithPhoneNumber:(NSString *)number
                                                        complete:(nullable void (^)(BOOL success, BJLError *_Nullable error))completion;

/** 手机号+验证码登录 */
- (nullable NSURLSessionDataTask *)requestCheckPhoneNumber:(NSString *)number
                                                andSMSCode:(NSString *)code
                                                  complete:(nullable void (^)(BOOL success, BJLError *_Nullable error))completion;
/** 重置礼物用户登录状态 */
- (nullable BJLError *)logoutGiftUser;

/** 获取手机号账户余额 */
- (nullable NSURLSessionDataTask *)requestPhoneAccountMoneyComplete:(nullable void (^)(BOOL success, NSString *_Nullable money, BJLError *_Nullable error))completion;

/** 准备内购充值之前，先记录账户充值 */
- (nullable NSURLSessionDataTask *)requestChargeMoney:(CGFloat)money
                                            productID:(NSString *)productID
                                             complete:(nullable void (^)(BOOL success, NSString *_Nullable orderID, BJLError *_Nullable error))completion;

/** 内购 验证receipt */
- (nullable NSURLSessionDataTask *)requestVerifyReceipt:(id)receipt
                                               complete:(nullable void (^)(BOOL success, BJLError *_Nullable error))completion;

/** 直播打赏
 * 根据type类型 使用不同的参数进行打赏
 */
- (nullable NSURLSessionDataTask *)requestAwardWitType:(BJLAwardType)type
                                                 money:(NSString *)money
                                             awardInfo:(nullable BJLAwardInfo *)info
                                              complete:(nullable void (^)(BOOL success, BJLError *_Nullable error))completion;

/** 收到直播打赏
 #param imageUrlString    默认png图片资源
 #param svgImageUrlString 如果后台配置支持svg，此处可以返回SVG图片资源
 #param shouldShowSpecial 是否展示大图特效
 */
- (BJLObservable)receivedAnmationAwardWithImageUrl:(NSString *)imageUrlString
                                       svgImageUrl:(nullable NSString *)svgImageUrlString
                                       fromUsrName:(NSString *)userName
                           shouldShowSpecialEffect:(BOOL)shouldShowSpecial;

#pragma mark - 旧版免费礼品互动（已废弃）

/** 互动记录
 #discussion 参考 `loadReceivedGifts`
 */
@property (nonatomic, readonly, copy, nullable) NSArray<NSObject<BJLReceivedGift> *> *receivedGifts;

/** `receivedGifts` 被覆盖更新
 #discussion 覆盖更新才调用，增量更新不调用
 #discussion 首次连接 server 或断开重连会导致覆盖更新
 #param receivedGifts 打赏记录
 */
- (BJLObservable)receivedGiftsDidOverwrite:(nullable NSArray<NSObject<BJLReceivedGift> *> *)receivedGifts;

/** 加载所有互动记录
 #discussion 连接直播间后、掉线重新连接后自动调用加载
 #discussion 加载成功后更新 `receivedGifts`、调用 `receivedMessagesDidOverwrite:`
 */
- (void)loadReceivedGifts;

/**
 打赏
 #discussion 成功后会收到通知，只支持学生给老师打赏
 #param teacher 打赏对象，老师在直播间内时使用老师信息、否则使用传入的 `teacher`
 #param gift    礼物
 #return BJLError:
 BJLErrorCode_invalidArguments  错误参数；
 BJLErrorCode_invalidCalling    错误调用，如老师和助教调用此方法、老师不在直播间、打赏对象不是老师等；
 BJLErrorCode_invalidUserRole   错误权限，要求学生权限。
 */
- (nullable BJLError *)sendGift:(BJLGift *)gift toTeacher:(BJLUser *)teacher;

/** 收到打赏通知
 #discussion 同时更新 `receivedGifts`
 #param receivedGift 打赏内容
 */
- (BJLObservable)didReceiveGift:(NSObject<BJLReceivedGift> *)receivedGift;

@end

NS_ASSUME_NONNULL_END
