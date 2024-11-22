//
//  BJLSlideshowconfiguration.h
//  BJLiveBase
//
//  Created by ney on 2023/9/12.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "BJLConstants.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJLSlideshowConfiguration: NSObject
@property (nonatomic, assign) BJLRoomType roomType;
@property (nonatomic, assign) CGFloat maximumZoomScale;
+ (BJLSlideshowConfiguration *)defaultConfiguration;
@end

NS_ASSUME_NONNULL_END
