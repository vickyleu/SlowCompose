//
//  BJLDownloadFile+public.h
//  BJLiveBase
//
//  Created by 凡义 on 2023/5/16.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <BJLiveBase/BJLiveBase.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJLDownloadFile ()

@property (nonatomic, readwrite, copy, nullable) NSString *filePath; // NON-KVO, NOT serialized

@end

NS_ASSUME_NONNULL_END
