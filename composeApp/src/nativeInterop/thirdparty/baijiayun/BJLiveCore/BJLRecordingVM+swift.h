//
//  BJLRecordingVM+swift.h
//  BJLiveCore
//
//  Created by ney on 2022/2/25.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import "BJLRecordingVM.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJLRecordingVM (swift)
- (id<BJLObservation>)sw_recordingDidDeny:(BJLControlObserving (^)(void))block NS_SWIFT_NAME(sw_recordingDidDeny(_:));
- (id<BJLObservation>)sw_onAudioCapturedRawAudioFrame:(BJLControlObserving (^)(BJLCustomAudioFrame *frame))block NS_SWIFT_NAME(sw_onAudioCapturedRawAudioFrame(_:));
- (id<BJLObservation>)sw_onAudioLocalProcessedAudioFrame:(BJLControlObserving (^)(BJLCustomAudioFrame *frame))block NS_SWIFT_NAME(sw_onAudioLocalProcessedAudioFrame(_:));
- (id<BJLObservation>)sw_recordingDidRemoteChangedRecordingAudioRecordingVideoRecordingAudioChangedRecordingVideoChanged:(BJLControlObserving (^)(BOOL recordingAudio, BOOL recordingVideo, BOOL recordingAudioChanged, BOOL recordingVideoChanged))block NS_SWIFT_NAME(sw_recordingDidRemoteChangedRecordingAudioRecordingVideoRecordingAudioChangedRecordingVideoChanged(_:));
- (id<BJLObservation>)sw_didUpadateAllRecordingAudioMute:(BJLControlObserving (^)(BOOL mute))block NS_SWIFT_NAME(sw_didUpadateAllRecordingAudioMute(_:));
- (id<BJLObservation>)sw_remoteChangeRecordingDidDenyForUser:(BJLControlObserving (^)(BJLUser *user))block NS_SWIFT_NAME(sw_remoteChangeRecordingDidDenyForUser(_:));
- (id<BJLObservation>)sw_onScreenCaptureStarted:(BJLControlObserving (^)(void))block NS_SWIFT_NAME(sw_onScreenCaptureStarted(_:));
- (id<BJLObservation>)sw_onScreenCapturePaused:(BJLControlObserving (^)(NSInteger reason))block NS_SWIFT_NAME(sw_onScreenCapturePaused(_:));
- (id<BJLObservation>)sw_onScreenCaptureResumed:(BJLControlObserving (^)(NSInteger reason))block NS_SWIFT_NAME(sw_onScreenCaptureResumed(_:));
- (id<BJLObservation>)sw_onScreenCaptureStoped:(BJLControlObserving (^)(NSInteger reason))block NS_SWIFT_NAME(sw_onScreenCaptureStoped(_:));
- (id<BJLObservation>)sw_republishing:(BJLControlObserving (^)(void))block NS_SWIFT_NAME(sw_republishing(_:));
- (id<BJLObservation>)sw_publishFailed:(BJLControlObserving (^)(void))block NS_SWIFT_NAME(sw_publishFailed(_:));
@end

NS_ASSUME_NONNULL_END
