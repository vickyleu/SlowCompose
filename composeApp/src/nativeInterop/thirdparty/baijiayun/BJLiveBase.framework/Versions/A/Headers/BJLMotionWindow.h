//
//  BJLMotionWindow.h
//  M9Dev
//
//  Created by MingLQ on 2016-08-19.
//  Copyright (c) 2016 MingLQ <minglq.9@gmail.com>. Released under the MIT license.
//

#import <UIKit/UIKit.h>

FOUNDATION_EXPORT NSString *const BJLEventSubtypeMotionShakeNotification;
FOUNDATION_EXPORT NSString *const BJLEventSubtypeMotionShakeStateKey;

typedef NS_ENUM(NSInteger, BJLEventSubtypeMotionShakeState) {
    BJLEventSubtypeMotionShakeStateBegan,
    BJLEventSubtypeMotionShakeStateEnded,
    BJLEventSubtypeMotionShakeStateCancelled
};

@interface BJLMotionWindow: UIWindow

@end
