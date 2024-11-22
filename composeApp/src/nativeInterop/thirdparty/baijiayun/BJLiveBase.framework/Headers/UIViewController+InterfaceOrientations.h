//
//  UIViewController+InterfaceOrientations.h
//  BJLiveBase
//
//  Created by ney on 2022/8/25.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@interface UIViewController (InterfaceOrientations)
- (void)bjl_setNeedsUpdateOfSupportedInterfaceOrientations;
@end

NS_ASSUME_NONNULL_END
