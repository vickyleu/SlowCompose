//
//  BJVVideoCatalogueItem.h
//  BJVideoPlayerCore
//
//  Created by 凡义 on 2023/5/18.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif


NS_ASSUME_NONNULL_BEGIN

@interface BJVVideoCatalogueItem : NSObject <BJLYYModel, NSCopying>

@property (nonatomic, readonly) NSString *timeOffset;
@property (nonatomic, readonly) NSString *title;
@property (nonatomic, readonly) NSString *reference_link;
@property (nonatomic, readonly) NSString *btntxt;
@property (nonatomic, readonly) NSString *imageString;

@end

NS_ASSUME_NONNULL_END
