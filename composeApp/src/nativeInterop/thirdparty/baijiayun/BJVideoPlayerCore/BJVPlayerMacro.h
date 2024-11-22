//
//  BJVPlayerMacro.h
//  Pods
//
//  Created by DLM on 2016/10/31.
//
//

#ifndef BJVPlayerMacro_h
#define BJVPlayerMacro_h

typedef NS_ENUM(NSInteger, BJVPlayerType) {
    BJVPlayerType_AVPlayer,
    BJVPlayerType_IJKPlayer
};

typedef NS_ENUM(NSInteger, BJVPlayerStatus) {
    BJVPlayerStatus_unload,
    BJVPlayerStatus_loading,
    BJVPlayerStatus_stalled,
    BJVPlayerStatus_ready,
    BJVPlayerStatus_playing,
    BJVPlayerStatus_paused,
    BJVPlayerStatus_failed,
    BJVPlayerStatus_reachEnd,
    BJVPlayerStatus_stopped
};

typedef NS_ENUM(NSInteger, BJVPlayerViewScalingMode) {
    BJVPlayerViewScalingMode_aspectFit,
    BJVPlayerViewScalingMode_aspectFill,
    BJVPlayerViewScalingMode_fill
};

typedef NS_ENUM(NSInteger, BJVMajorPlayType) {
    BJVMajorPlayType_None = 0,
    BJVMajorPlayType_viedo,
    BJVMajorPlayType_ppt
};

typedef NS_ENUM(NSInteger, BJVWatermarkPos) {
    BJVWatermarkPos_None = 0, //不显示
    BJVWatermarkPos_LeftUp = 1, //左上
    BJVWatermarkPos_RightUp = 2, //右上
    BJVWatermarkPos_RightDown = 3, //右下
    BJVWatermarkPos_LeftDown = 4, //左下
};

#endif /* BJVPlayerMacro_h */
