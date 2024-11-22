//
//  BJVPlayerManager.h
//  BJPlayerManagerCore
//
//  Created by HuangJie on 2018/3/23.
//

#import <Foundation/Foundation.h>

#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

#import "BJVAppConfig.h"
#import "BJVPlayerMacro.h"
#import "BJVPlayProtocol.h"
#import "BJVDownloadManager.h"
#import "NSError+BJVPlayerError.h"
#import "BJVVideoCatalogueItem.h"
#import "BJVPlayinfoItem.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJVPlayerManager: NSObject <BJVPlayProtocol>

#pragma mark - init

/**
 初始化 manager 实例
 #param playerType 播放器类型：AVPlayer、IJKPlayer
 #discussion AVPlayer 不支持加密
 #return manager 实例
 */
- (instancetype)initWithPlayerType:(BJVPlayerType)playerType;

/**
 播放器类型：AVPlayer（不支持加密视频播放）、IJKPlayer
 #discussion 当播放器初始设置为 AVPlayer 时，但是播放的视频实际为加密格式，
 #discussion SDK 将会切换播放器的类型为 IJKPlayer 来尽量保证可以正常播放视频
 #discussion 因此，在获取播放器的属性时，不能作为一个属性存储，需要实时获取，
 #discussion 或者监听播放器类型的变化，及时获取到新的属性。
 */
@property (nonatomic, readonly) BJVPlayerType playerType;

#pragma mark - setup video

/**
 初始化本地视频
 #param downloadItem  本地视频文件类型，通过下载模块获得
 */
- (void)setupLocalVideoWithDownloadItem:(BJVDownloadItem *)downloadItem;

/**
 初始化在线视频
 #param videoID 视频 ID
 #param token 需要集成方后端调用百家云后端的API获取，传给移动端
 #discussion 调用此方法初始化在线视频时，默认不加密、不使用集成方鉴权
 */
- (void)setupOnlineVideoWithID:(NSString *)videoID
                         token:(NSString *)token;

/**
 初始化在线视频，设置是否加密、集成方鉴权
 #param videoID 视频 ID
 #param token 需要集成方后端调用百家云后端的API获取，传给移动端
 #param encrypted 是否加密，「仅在使用 IJKPlayer 时有效」，参考 initWithPlayerType: 方法
 #param accessKey 集成方鉴权, 视频如果需要请求第三方服务器查看是否有权限, 可设置该参数。鉴权验证请求需要与百家云后台沟通。
 */
- (void)setupOnlineVideoWithID:(NSString *)videoID
                         token:(NSString *)token
                     encrypted:(BOOL)encrypted
                     accessKey:(nullable NSString *)accessKey;


/**
 点播：请求视频打点数据

 #param fids        视频fid
 #param completion  请求完成回调
 #return            请求的 task
 */
- (NSURLSessionTask *)getVideoCatalogueInfoWithVideoVID:(NSString *)vid
                                                  token:(NSString *)token
                                             completion:(nullable void (^)(NSURLSessionDataTask *_Nullable task,
                                                                           NSArray<BJVVideoCatalogueItem *> *_Nullable list,
                                                                           NSError *_Nullable error))completion;

/**
 点播：请求专辑

 #param albumNumber        视频合集id
 #param completion         请求完成回调
 #return                   请求的 task
 */
- (NSURLSessionTask *)getVideoAlbumListWithNumber:(NSString *)albumNumber
                                       completion:(nullable void (^)(NSURLSessionDataTask *_Nullable task,
                                                                     NSArray<BJVPlayinfoItem *> *_Nullable list,
                                                                     NSError *_Nullable error))completion;

/**
 点播：请求专辑中某一个视频的token

 #param albumNumber         视频合集id
 #param vid                 视频id
 #param completion          请求完成回调
 #return                    请求的 task
 */
- (NSURLSessionTask *)getVideoAlbumTokenWithNumber:(NSString *)albumNumber
                                               vid:(NSString *)vid
                                        completion:(nullable void (^)(NSURLSessionDataTask *_Nullable task,
                                                                      NSString * vid,
                                                                      NSString *__nullable token,
                                                                      NSError *_Nullable error))completion;

/**
 获取当前视频播放记录
 #return            记录的播放时间
 */
- (NSTimeInterval)getCurrentPlayTimeRecord;

@end

NS_ASSUME_NONNULL_END
