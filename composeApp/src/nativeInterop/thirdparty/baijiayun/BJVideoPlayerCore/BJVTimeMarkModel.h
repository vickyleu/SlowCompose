//
//  BJVTimeMarkModel.h
//  BJVideoPlayerCore
//
//  Created by 凡义 on 2024/1/9.
//  Copyright © 2024 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "../BJLiveBase/BJLYYModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJVTimeMarkModel : NSObject<BJLYYModel>

@property (nonatomic, readonly) NSString *decContent;

@property (nonatomic, readonly) NSString *timeString;

@end

NS_ASSUME_NONNULL_END
