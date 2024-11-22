//
//  BJLSpeakingRequestVM.h
//  BJLiveCore
//
//  Created by MingLQ on 2016-12-07.
//  Copyright © 2016 BaijiaYun. All rights reserved.
//

#import "BJLBaseVM.h"
#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

NS_ASSUME_NONNULL_BEGIN

/** ### 发言申请/处理 */
@interface BJLSpeakingRequestVM: BJLBaseVM

/** 老师禁止学生举手状态 */
@property (nonatomic, readonly) BOOL forbidSpeakingRequest;
/**
 老师设置禁止学生举手状态
 #param forbid YES：禁止，NO：取消禁止
 #return BJLError:
 BJLErrorCode_invalidUserRole   错误权限，要求老师或助教权限
 */
- (nullable BJLError *)requestForbidSpeakingRequest:(BOOL)forbid;

/**
 学生: 发言状态
 #discussion 举手、邀请发言、远程开关音视频等事件会改变此状态
 #discussion 上层需要根据这个状态开启/关闭音视频，上层开关音视频前需要判断当前音视频状态
 #discussion 因为 `speakingDidRemoteControl:` 会直接开关音视频、然后再更新学生的 `speakingEnabled` */
@property (nonatomic, readonly) BOOL speakingEnabled;

/**
 举手自动取消倒计时总时长、更新间隔、剩余时间
 #discussion 调用 `sendSpeakingRequest` 举手时设置为 `speakingRequestTimeoutInterval` 秒
 #discussion 每 `speakingRequestCountdownStep` 秒更新，变为 0.0 表示举手超时，变为 - 1.0 表示计时被取消 */
@property (nonatomic, readonly) NSTimeInterval speakingRequestTimeoutInterval, speakingRequestCountdownStep, speakingRequestTimeRemaining;
/**
 学生: 发送发言申请
 #discussion 上课状态才能举手，参考 `roomVM.liveStarted`
 #discussion 发言申请被允许/拒绝时会收到通知 `speakingRequestDidReply:`
 #return BJLError:
 BJLErrorCode_invalidCalling    错误调用，如在非上课状态、或者禁止举手等情况下调用此方法；
 BJLErrorCode_invalidUserRole   错误权限，要求非试听学生权限。
*/
- (nullable BJLError *)sendSpeakingRequest;

/**
 学生: 取消发言申请/结束发言
 #discussion `speakingEnabled = NO` 时调用，取消发言申请，不更新 `speakingEnabled`
 #discussion `speakingEnabled = YES`时调用，结束发言，更新 `speakingEnabled`
 */
- (void)stopSpeakingRequest;

/** 老师: 正在申请发言的学生 */
@property (nonatomic, readonly, copy, nullable) NSArray<BJLUser *> *speakingRequestUsers;

/**
 老师: 收到发言申请
 #param user 申请用户
 */
- (BJLObservable)didReceiveSpeakingRequestFromUser:(BJLUser *)user;
/**
 老师: 允许/拒绝发言申请
 #discussion 允许发言后，关闭发言需要调用 `BJLRecordingVM` 的 `remoteChangeRecordingWithUser:audioOn:videoOn:` 方法
 #param userID 申请用户 ID
 #param allowed YES：允许，NO：拒绝
 #return BJLError:
 BJLErrorCode_invalidUserRole   错误权限，要求老师或助教权限
 */
- (nullable BJLError *)replySpeakingRequestToUserID:(NSString *)userID allowed:(BOOL)allowed;

/**
 老师: 收到允许/拒绝发言申请的结果
 #discussion 允许发言的结果
 #param userID 申请用户 ID
 #param allowed YES：允许，NO：拒绝 - 目前拒绝结果不会回调，只要 replySpeakingRequestToUserID:allowed: 方法没返回 error 就一定会成功
 #param success YES：成功，NO：失败 - 因为上麦路数达到上限，需要先将某些学生下麦、然后重新同意
 */
- (BJLObservable)speakingRequestDidReplyToUserID:(NSString *)userID allowed:(BOOL)allowed success:(BOOL)success;

/**
 学生&老师: 发言申请被允许/拒绝
 #discussion 更新学生的 `speakingEnabled`
 #discussion 老师可以收到所有人发言状态的变更，比如学生自己取消、助教协助允许/拒绝
 #param speakingEnabled 发言申请是否被允许、关闭
 #param isUserCancelled 学生本人取消/请求超时自动取消
 #param user            申请发言的用户
 */
- (BJLObservable)speakingRequestDidReplyEnabled:(BOOL)speakingEnabled
                                isUserCancelled:(BOOL)isUserCancelled
                                           user:(BJLUser *)user;

/**
 老师: 邀请学生发言
 #param invite  YES 邀请、NO 取消邀请
 */
- (nullable BJLError *)sendSpeakingInviteToUserID:(NSString *)userID invite:(BOOL)invite;

/**
 学生: 收到邀请发言
 #param invite  YES 收到邀请、NO 邀请取消 */
- (BJLObservable)didReceiveSpeakingInvite:(BOOL)invite;

/**
 学生: 接受或拒绝发言邀请
 #discussion 接受后更新学生的 `speakingEnabled`
 #param accept  接受或拒绝
 #return BJLError:
 BJLErrorCode_invalidUserRole   错误权限，要求老师或助教权限
 */
- (nullable BJLError *)responseSpeakingInvite:(BOOL)accept;

/**
 老师: 收到邀请发言的结果
 #param accept  YES 接受邀请、NO 拒绝邀请 */
- (BJLObservable)didReceiveSpeakingInviteResultWithUserID:(NSString *)userID accept:(BOOL)accept;

/**
 音视频被远程开启、关闭，导致发言状态变化
 #discussion 音视频有一个打开就开启发言、全部关闭就结束发言
 #discussion SDK 内部先开关音视频、然后再更更新学生的 `speakingEnabled` 的状态
 #discussion 参考 `BJLRecordingVM` 的 `recordingDidRemoteChangedRecordingAudio:recordingVideo:recordingAudioChanged:recordingVideoChanged:`
 #param enabled YES：开启，NO：关闭
 */
- (BJLObservable)speakingDidRemoteControl:(BOOL)enabled;

/**
 学生: 发送麦克风申请
 #discussion 上课状态才能举手，参考 `roomVM.liveStarted`
 #discussion 麦克风申请被允许/拒绝时会收到通知 `AudioOpenRequestDidReply:`
 #return BJLError:
 BJLErrorCode_invalidCalling    错误调用，如在非上课状态、或者禁止举手等情况下调用此方法；
 BJLErrorCode_invalidUserRole   错误权限，要求非试听学生权限。
*/
- (nullable BJLError *)sendAudioOpenRequest;

- (BJLObservable)AudioOpenRequestDidReply:(BOOL)allowed;

@end

NS_ASSUME_NONNULL_END
