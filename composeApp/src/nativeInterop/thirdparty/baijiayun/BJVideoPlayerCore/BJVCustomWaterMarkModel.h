//
//  BJVCustomWaterMarkModel.h
//  BJVideoPlayerCore
//
//  Created by 凡义 on 2023/12/21.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BJVPlayerMacro.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJVCustomWaterMarkModel : NSObject

@property (nonatomic, copy, readonly) NSString *url;
@property (nonatomic, assign, readonly) BJVWatermarkPos position;

+ (instancetype)waterMarkModelWithUrl:(NSString *)url
                             position:(BJVWatermarkPos )position;

@end

NS_ASSUME_NONNULL_END
