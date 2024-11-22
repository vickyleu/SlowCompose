//
//  BJLChatVM+swift.h
//  BJLiveCore
//
//  Created by ney on 2022/2/25.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import "BJLChatVM.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJLChatVM (swift)
- (id<BJLObservation>)sw_receivedMessagesDidOverwrite:(BJLControlObserving (^)(NSArray<BJLMessage *> *_Nullable receivedMessages))block NS_SWIFT_NAME(sw_receivedMessagesDidOverwrite(_:));
- (id<BJLObservation>)sw_didReceiveMessages:(BJLControlObserving (^)(NSArray<BJLMessage *> *messages))block NS_SWIFT_NAME(sw_didReceiveMessages(_:));
- (id<BJLObservation>)sw_didReceiveVirtualMessages:(BJLControlObserving (^)(NSArray<BJLMessage *> *messages))block NS_SWIFT_NAME(sw_didReceiveVirtualMessages(_:));
- (id<BJLObservation>)sw_didReceiveKickOutMessage:(BJLControlObserving (^)(void))block NS_SWIFT_NAME(sw_didReceiveKickOutMessage(_:));
- (id<BJLObservation>)sw_didRevokeMessageWithIDIsCurrentUserRevoke:(BJLControlObserving (^)(NSString *messageID, BOOL isCurrentUserRevoke))block NS_SWIFT_NAME(sw_didRevokeMessageWithIDIsCurrentUserRevoke(_:));
- (id<BJLObservation>)sw_didRevokeMessageWithIDListUserNumbersIsCurrentUserRevoke:(BJLControlObserving (^)(NSArray <NSString *> *_Nullable messageIDList, NSArray <NSString *> *_Nullable userNumbers, BOOL isCurrentUserRevoke))block NS_SWIFT_NAME(sw_didRevokeMessageWithIDListUserNumbersIsCurrentUserRevoke(_:));
- (id<BJLObservation>)sw_didMessageUpdateWallSuccess:(BJLControlObserving (^)(BJLWallMessage *message))block NS_SWIFT_NAME(sw_didMessageUpdateWallSuccess(_:));
- (id<BJLObservation>)sw_didReceiveMessageUpdateWall:(BJLControlObserving (^)(BJLWallMessage *message))block NS_SWIFT_NAME(sw_didReceiveMessageUpdateWall(_:));
- (id<BJLObservation>)sw_didReceiveMessageTranslationMessageUUIDFromTo:(BJLControlObserving (^)(NSString *translation, NSString *_Nullable messageUUID, BJLMessageLanguageType from, BJLMessageLanguageType to))block NS_SWIFT_NAME(sw_didReceiveMessageTranslationMessageUUIDFromTo(_:));
- (id<BJLObservation>)sw_didReceiveForbidUserFromUserDuration:(BJLControlObserving (^)(BJLUser *user, BJLUser *_Nullable fromUser, NSTimeInterval duration))block NS_SWIFT_NAME(sw_didReceiveForbidUserFromUserDuration(_:));
- (id<BJLObservation>)sw_didReceiveForbidUserList:(BJLControlObserving (^)(NSDictionary<NSString *, NSNumber *> *_Nullable forbidUserList))block NS_SWIFT_NAME(sw_didReceiveForbidUserList(_:));
- (id<BJLObservation>)sw_didReceiveWhisperMessagesTargetUserNumberHasMore:(BJLControlObserving (^)(NSArray<BJLMessage *> *messages, NSString *targetUserNumber, BOOL hasMore))block NS_SWIFT_NAME(sw_didReceiveWhisperMessagesTargetUserNumberHasMore(_:));
- (id<BJLObservation>)sw_didReceivePicAndTextPresenterInfoUpdate:(BJLControlObserving (^)(NSDictionary *userInfo))block NS_SWIFT_NAME(sw_didReceivePicAndTextPresenterInfoUpdate(_:));
- (id<BJLObservation>)sw_didReceiveNewPicAndTextInfo:(BJLControlObserving (^)(BJLPicAndTextModel *newPicAndTextInfo))block NS_SWIFT_NAME(sw_didReceiveNewPicAndTextInfo(_:));
- (id<BJLObservation>)sw_didReceivePicAndTextInfoDelete:(BJLControlObserving (^)(NSString *graphicID))block NS_SWIFT_NAME(sw_didReceivePicAndTextInfoDelete(_:));
@end

NS_ASSUME_NONNULL_END
