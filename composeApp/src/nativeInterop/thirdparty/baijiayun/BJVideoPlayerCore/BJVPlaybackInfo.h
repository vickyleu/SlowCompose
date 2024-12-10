//
//  _BJVPlaybackInfo.h
//  BJVideoPlayerCore
//
//  Created by HuangJie on 2018/5/19.
//  Copyright © 2018年 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "../BJLiveBase/BJLYYModel.h"

#import "BJVPlayInfo.h"
#import "BJVUserVideo.h"
#import "BJVConstants.h"
#import "BJVPlayerMacro.h"
#import "BJVTimeMarkModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJVPlaybackInfo: BJVPlayInfo <BJLYYModel>

// all.json信令文件 URL
@property (nonatomic, readonly) NSString *signalFileURL;

// command.json信令文件 URL
@property (nonatomic, readonly) NSString *commandSignalFileURL;

// user.json信令文件 URL
@property (nonatomic, readonly) NSString *userSignalFileURL;

// chat.json信令分片 URL
@property (nonatomic, readonly) NSArray<NSDictionary *> *chatSignalFileURLs;

// chat.json信令分片对应的时间戳
@property (nonatomic, readonly) NSString *chatFileInfoSignalFileURL;

// 虚拟聊天信令 URL
@property (nonatomic, readonly) NSString *virtualChatSignalURL;

// 回放是否要支持答题器和小测
@property (nonatomic, readonly) BOOL enableQuizAndAnswer;

// 是否显示问答
@property (nonatomic, readonly) BOOL enableQuestion;

// 学生视频列表
@property (nonatomic, readonly) NSArray<BJVUserVideo *> *userVideoList;

// 回放的班型
@property (nonatomic, readonly) BJVRoomType roomType;

// 小班课 1v1 是不是信令录制
@property (nonatomic, readonly) BOOL isInteractiveClass1v1SignalingRecord;

// 小班课 1v1 黑板数量
@property (nonatomic, readonly) NSInteger interactiveClass1v1BlackboardPages;

// 白板背景图片 url
@property (nonatomic, readonly) NSString *whiteboardURL;

// 是否开启隐藏学生消息中的手机号功能
@property (nonatomic, readonly) BOOL enableHideStudentPhoneNumber;

// 三分屏主屏内容
@property (nonatomic, readonly) BJVMajorPlayType majorPlayType;

// 动效课件 webview 地址
@property (nonatomic, readonly) NSString *whiteboardWebviewURL;

// 标记打点列表
@property (nonatomic, readonly) NSArray<BJVTimeMarkModel *> *timeMarkList;

// 有效期时间戳
@property (nonatomic, readonly) NSTimeInterval invalidTime;

// 默认ppt/视频操作模式
@property (nonatomic, readonly) BOOL initialPPTMode;

@end

NS_ASSUME_NONNULL_END
