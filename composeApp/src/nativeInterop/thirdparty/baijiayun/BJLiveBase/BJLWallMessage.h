//
//  BJLWallMessage.h
//  BJLiveBase
//
//  Created by 凡义 on 2024/7/23.
//  Copyright © 2024 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "BJLMessage.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJLWallMessage : NSObject

@property (nonatomic, readonly) NSString *ID;

@property (nonatomic, readonly) BOOL onWall;

// 被上/下墙的学生number
@property (nonatomic, readonly) NSString *userNumber;

@property (nonatomic, readonly) NSString *content;

@property (nonatomic, readonly) NSTimeInterval timeIntervalMS;

// 上/下墙行为的发起者
@property (nonatomic, readonly) NSString *operateUserNumber;

@end

NS_ASSUME_NONNULL_END
