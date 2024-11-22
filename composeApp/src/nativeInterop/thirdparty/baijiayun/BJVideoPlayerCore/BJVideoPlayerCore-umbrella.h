#ifdef __OBJC__
#import <UIKit/UIKit.h>
#else
#ifndef FOUNDATION_EXPORT
#if defined(__cplusplus)
#define FOUNDATION_EXPORT extern "C"
#else
#define FOUNDATION_EXPORT extern
#endif
#endif
#endif

#import "BJVideoPlayerCore.h"
#import "BJVTokenManager.h"
#import "BJVDownloadItem+public.h"
#import "BJVDownloadManager.h"
#import "BJVCDNInfo.h"
#import "BJVCustomWaterMarkModel.h"
#import "BJVDefinitionInfo.h"
#import "BJVPlaybackInfo.h"
#import "BJVPlayInfo.h"
#import "BJVPlayinfoItem.h"
#import "BJVSubtitle.h"
#import "BJVTimeMarkModel.h"
#import "BJVTripartiteLayoutInfo.h"
#import "BJVUserVideo.h"
#import "BJVVideoCatalogueItem.h"
#import "BJVLamp.h"
#import "BJVLampConstructor.h"
#import "BJVAppConfig.h"
#import "BJVPlayerMacro.h"
#import "NSError+BJVPlayerError.h"
#import "BJVCommentManager.h"
#import "BJVPlayerManager.h"
#import "BJVPlayProtocol.h"
#import "BJVQuizManager.h"
#import "BJPDocumentCatalogueModel.h"
#import "BJPPageChangeModel.h"
#import "BJPPPTCatalogueModel.h"
#import "BJVCloudVideoPlayerProtocol.h"
#import "BJVRoom.h"
#import "BJVAnswerSheet.h"
#import "BJVMediaUser.h"
#import "BJVNotice.h"
#import "BJVQuestion.h"
#import "BJVQuiz.h"
#import "BJVSurvey.h"
#import "BJVUser.h"
#import "BJVConstants.h"
#import "BJVBaseVM.h"
#import "BJVMessageVM.h"
#import "BJVOnlineUserVM.h"
#import "BJVRoomVM.h"

FOUNDATION_EXPORT double BJVideoPlayerCoreVersionNumber;
FOUNDATION_EXPORT const unsigned char BJVideoPlayerCoreVersionString[];

