//
//  BJVDownloadItem+public.h
//  BJVideoPlayerCore
//
//  Created by 凡义 on 2023/5/16.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import "BJVDownloadManager.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJVDownloadItem ()

/** 视频文件 */
@property (nonatomic, readwrite, nullable) BJVDownloadFile *videoFile;

@end

NS_ASSUME_NONNULL_END
