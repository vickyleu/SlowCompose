//
//  BJLRecordingVM+define.h
//  BJLiveCore
//
//  Created by ney on 2022/12/28.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

/**
 * 音频采样率
 *
 * 音频采样率用来衡量声音的保真程度，采样率越高保真程度越好，如果您的应用场景有音乐的存在，推荐使用 BJLAudioSampleRate48000。
 */
typedef NS_ENUM(NSInteger, BJLAudioSampleRate) {
    /// 16k采样率
    BJLAudioSampleRate16000 = 16000,

    /// 32k采样率
    BJLAudioSampleRate32000 = 32000,

    /// 44.1k采样率
    BJLAudioSampleRate44100 = 44100,

    /// 48k采样率
    BJLAudioSampleRate48000 = 48000,
};

@interface BJLCustomAudioFrame: NSObject
///【字段含义】音频数据
@property(nonatomic, strong, nonnull) NSData *data;

///【字段含义】采样率
@property(nonatomic, assign) BJLAudioSampleRate sampleRate;

///【字段含义】声道数
@property(nonatomic, assign) int channel;

///【字段含义】时间戳，单位ms
@property(nonatomic, assign) uint64_t timestamp;
@end

@interface BJLCustomAudioDelegateFormat : NSObject

///【字段含义】采样率
///【推荐取值】48000Hz。支持 16000, 32000, 44100, 48000。
@property(nonatomic, assign) BJLAudioSampleRate sampleRate;

///【字段含义】声道数
@property(nonatomic, assign) int channel;

///【字段含义】采样点数
///【推荐取值】取值必须是 sampleRate/100 的整数倍。
@property(nonatomic, assign) int samplesPerCall;

@end
