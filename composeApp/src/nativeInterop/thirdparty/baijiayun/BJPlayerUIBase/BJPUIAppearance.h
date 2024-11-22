//
//  BJPUIAppearance.h
//  BJPlayerUIBase
//
//  Created by 凡义 on 2023/10/31.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

@interface BJPUIAppearance : NSObject

@end

@interface UIImage (BJPUIBase)

+ (UIImage *)bjpui_imageNamed:(NSString *)name;

@end
