//
//  BJLRoomViewController.h
//  BJLiveUI-Base-BJLiveUI
//
//  Created by Ney on 7/8/21.
//  Copyright © 2021 BaijiaYun. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "../BJLiveCore/BJLiveCore.h"
#import "BJLCustomizedFeatureCallback.h"

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSUInteger, BJLRoomVCType) {
    BJLRoomVCTypeSmallClass = 1, // 专业小班课，横屏模式
    BJLRoomVCTypeBigClass = 2, // 大班课，横屏模式
    BJLRoomVCTypeEnterpriseEdition = 3, // 企业版大班直播间，竖屏模式
    BJLRoomVCTypeSell = 4, // 带货直播
};

@protocol BJLRoomVCDelegate;

@interface BJLRoomCode: NSObject
/// 参加码
@property (nonatomic, copy) NSString *code;
/// 用户名
@property (nonatomic, copy) NSString *userName;
/// 用户头像
@property (nonatomic, copy, nullable) NSString *userAvatar;
@end

@interface BJLRoomID: NSObject
/// 直播间 ID
@property (nonatomic, copy) NSString *roomID;
/// API sign
@property (nonatomic, copy) NSString *apiSign;
/// 用户，初始化时的属性未标记可为空的都需要有值，且字符值长度不能为0
@property (nonatomic, strong) BJLUser *user;
@end

@interface BJLRoomViewController: UIViewController
/**
 通过参加码创建直播间，如果sdk没有集成type对应的uisdk，返回nil
 #param type  直播间类型
 #param roomCode 参加码对象
 #return 直播间
 */
+ (__kindof instancetype)instanceWithRoomType:(BJLRoomVCType)type roomCode:(BJLRoomCode *)roomCode;

/**
 通过roomID创建直播间，如果sdk没有集成type对应的uisdk，返回nil
 #param type  直播间类型
 #param roomID roomID对象
 #return 直播间
 */
+ (__kindof instancetype)instanceWithRoomType:(BJLRoomVCType)type roomID:(BJLRoomID *)roomID;

/** 直播直播间
 参考 `BJLiveCore` */
@property (nonatomic, readonly, nullable) BJLRoom *room;

/** 初始化时传入的 roomID 入参
*/
@property (nonatomic, readonly, nullable) BJLRoomID *roomID;

/** 初始化时传入的 roomCode 入参
*/
@property (nonatomic, readonly, nullable) BJLRoomCode *roomCode;

/** 直播间的类型，大班课或者小班课
*/
@property (nonatomic, readonly) BJLRoomVCType roomVCType;

/** 定制需求的客户的相关回调，一般客户不需要关注
*/
@property (nonatomic, readonly) BJLCustomizedFeatureCallback *customizedFeatureCallback;

/** 跑马灯内容 */
@property (nonatomic, copy, nullable) NSString *customLampContent;

/** 支持更丰富自定义跑马灯，优先级最高，老师角色不展示，助教角色受配置项控制 */
@property (nonatomic, copy, nullable) BJLLamp *customLamp;

/** 外部控制截屏，仅对大班课有效，设置为disable时，screenRecordingType也变化为disable */
@property (nonatomic, assign) BJLRoomAPIControlType screenShotType;

/** 外部控制录屏，仅对大班课有效, screenShotType 设置为禁止时，screenRecordingType 不可设置为允许 */
@property (nonatomic, assign) BJLRoomAPIControlType screenRecordingType;

/** 是否显示用户数量，仅对大班课有效 */
@property (nonatomic, assign) BJLRoomAPIControlType showOnlineUserNumber;

/** 是否显示用户列表，仅对大班课有效 */
@property (nonatomic, assign) BJLRoomAPIControlType showOnlineUserList;

/** 设置自定义水印信息 */
- (void)setCustomWaterMark:(BJLWaterMarkModel *)customWaterMark;

/** 退出直播间 */
- (void)exitWithCompletion:(void (^)(void))completion;

/** 事件回调 `delegate` */
@property (nonatomic, weak) id<BJLRoomVCDelegate> delegate;

/** 实际的直播间控制器，可以类型转化成具体类型 */
@property (nonatomic, nullable, readonly) UIViewController *controller;

/** 研讨会老师切回大班 */
@property (nonatomic, assign) BOOL canBackToOnlineClass;

/** 屏幕分享的 AppGroup，若不设置，则不能开启屏幕共享功能 */
@property (nonatomic, copy, nullable) NSString *appGroupForScreenShareExtension;
@end

@protocol BJLRoomVCDelegate <NSObject>
@optional

/** 进入直播间 - 成功/失败 */
- (void)roomViewControllerEnterRoomSuccess:(BJLRoomViewController *)roomViewController;
- (void)roomViewController:(BJLRoomViewController *)roomViewController enterRoomFailureWithError:(BJLError *)error;

/**
 退出直播间 - 正常/异常
 正常退出 `error` 为 `nil`，否则为异常退出
 参考 `BJLErrorCode` */
- (void)roomViewController:(BJLRoomViewController *)roomViewController willExitWithError:(nullable BJLError *)error;
- (void)roomViewController:(BJLRoomViewController *)roomViewController didExitWithError:(nullable BJLError *)error;

/**
 点击直播间右上方分享按钮回调。仅仅大班课才会有回调
 */
- (nullable UIViewController *)roomViewControllerToShare:(BJLRoomViewController *)roomViewController;

/**
 直播带货模板的回调
 点击购物车按钮, 展示商品列表
 @param sellViewController sellViewController
 @param superview 商品列表的父view
 @param closeCallback 关闭商品列表vc的回调
 */
- (void)roomViewController:(BJLRoomViewController *)sellViewController openListFromView:(UIView *)superview closeCallback:(nullable void (^)(void))closeCallback;

/**
 点击购物车中的商品，或者正在讲解的商品
 @param sellViewController sellViewController
 @param item 点击的商品的信息
 */
- (void)roomViewController:(BJLRoomViewController *)sellViewController openSellItem:(BJLSellItem *)item;

/** 当前账户需要充值回调 */
- (nullable UIViewController *)roomViewControllerNeedCharge:(BJLRoomViewController *)roomViewController closeCallback:(nullable void (^)(void))closeCallback;

/** 处理当前手机账号未完成的内购订单,防止丢单 */
- (void)roomViewControllerFinishunCompletedTransaction:(BJLRoomViewController *)roomViewController;

@end

NS_ASSUME_NONNULL_END
