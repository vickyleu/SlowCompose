//
//  BJPPlaybackOptions.h
//  BJPlaybackUI
//
//  Created by HuangJie on 2018/6/5.
//  Copyright © 2018年 BaijiaYun. All rights reserved.
//

#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

#import <Foundation/Foundation.h>
#import "../BJVideoPlayerCore/BJVideoPlayerCore.h"
#import "../BJPlayerUIBase/BJPUStopPlayAlertView.h"

@interface BJPPlaybackOptions: NSObject <BJLYYModel>

@property (nonatomic, assign) BJVPlayerType playerType; // 播放器类型
@property (nonatomic, assign) BOOL advertisementEnabled; // 播放广告，暂不支持
@property (nonatomic, assign) BOOL autoplay; // 自动播放
@property (nonatomic, assign) BOOL sliderDragEnabled; // 能否拖动进度条
@property (nonatomic, assign) BOOL zoomScalePPT; // 课件是否可以缩放
@property (nonatomic, assign) BOOL enablePlaybackRate; // 能否倍速播放
@property (nonatomic, assign) BOOL playTimeRecordEnabled; // 开启记忆播放
@property (nonatomic, assign) BOOL encryptEnabled; // 开启加密
@property (nonatomic, assign) BOOL backgroundAudioEnabled; // 开启后台播放，必须在工程的 background modes 中添加 audio 才会生效
@property (nonatomic, assign) BOOL remoteControlEnabled; // 开启远程控制
@property (nonatomic, strong) UIImage *remoteControlImage; // 远程控制图片
@property (nonatomic, assign) BOOL disablePPTAnimation; // 回放是否禁用动效课件，默认NO
@property (nonatomic, assign) BJVMajorPlayType majorPlayType; // 主屏播放器内容（视频/ppt）
@property (nonatomic, assign) NSInteger maxWatchTime; // 回放最大可观看时间，单位秒，达到时间点之前可以拖动，之后不可以任意拖动，取值大于0有效
@property (nonatomic, assign) BOOL initialHorizontalLayout; //常规回放是否横屏播放，默认NO，小班课回放默认为横屏，不支持改动

/**
 裁剪后的回放视频的version
 #discussion  -1: 裁剪后的主版本,  0: 裁剪前的原始视频.
 #discussion 默认 clipedVersion 为 -1
 */
@property (nonatomic, assign) NSInteger clipedVersion;
@property (nonatomic, assign) NSTimeInterval initialPlayTime; // 起始播放时间

/**
 分组, 设置后则返回对应分组的聊天信息
 #discussion 默认 group 为 -1, 即返回所有分组的聊天信息
 */
@property (nonatomic) NSInteger groupID;

/** 是否要展示用户列表，需要在调用enter之前设置，只有直播配置了展示且showUserInfo = YES才展示 */
@property (nonatomic) BOOL showUserInfo;

/**
 偏好清晰度列表
 
 #discussion 优先使用此列表中的清晰度播放在线视频，优先级按数组元素顺序递减
 #discussion 列表元素为清晰度的标识字符串，现有标识符：low（标清），high（高清），superHD（超清），720p，1080p，audio（纯音频），可根据实际情况动态扩展
 #discussion 此设置对播放本地视频无效
 */
@property (nonatomic, strong) NSArray<NSString *> *preferredDefinitionList;

/**
回放合集默认起始播放的index，默认0
 
 #discussion 此设置对播放本地视频无效
 */
@property (nonatomic, assign) NSInteger initialAlbumIndex;

/**
 开启画中画
 note: 当前的播放器类型为 BJVPlayerType_AVPlayer 且 backgroundAudioEnabled == NO 时, 设置 pictureInPictureEnabled 才有效
 note: 当设置 backgroundAudioEnabled 为 YES 时, 内部会把 pictureInPictureEnabled 置为 NO
 #discussion 默认为YES
 */
@property (nonatomic, assign) BOOL pictureInPictureEnabled;


/**
 能否拖动进度条到未观看过的位置
 
 #discussion 开启之后，拖动进度条会弹框提示
 #discussion 可自定义设置弹框的文字提示标题，最大 16 字符
 #discussion 可自定义设置弹框的文字提示内容，最大 48 字符
 #discussion 可自定义设置弹框的icon 和按钮文本，按钮文本最大 20 字符
 */
@property (nonatomic, assign) BOOL sliderAdvanceEnabled;
@property (nonatomic, strong) BJPUStopPlayModel *sliderDragStopPlayModel;

/**
 试听时间，单位秒
 
 #discussion 开启之后，拖动进度条会弹框提示
 #discussion 可自定义设置弹框的文字提示标题，最大 16 字符
 #discussion 可自定义设置弹框的文字提示内容，最大 48 字符
 #discussion 可自定义设置弹框的icon 和按钮文本，按钮文本最大 20 字符
 */
@property (nonatomic, assign) NSInteger auditionTime;
@property (nonatomic, strong) BJPUStopPlayModel *auditionTimeStopPlayModel;

/**
 自定义水印
 #discussion 自定义优先级高于原配置项
 */
@property (nonatomic, strong) BJVCustomWaterMarkModel *waterMarkModel;

/**
 第三方用户名和编号
 
 #discussion 用于上报统计
 */
@property (nonatomic, strong) NSString *userName;
@property (nonatomic, strong) NSString *userNumber;

@end
