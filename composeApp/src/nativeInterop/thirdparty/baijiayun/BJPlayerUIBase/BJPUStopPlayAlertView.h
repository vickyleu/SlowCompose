//
//  BJPUStopPlayAlertView.h
//  BJPlayerUIBase
//
//  Created by 凡义 on 2023/10/31.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BJPUStopPlayModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJPUStopPlayAlertView : UIView

@property (nonatomic, copy) void (^_Nullable conformCallback)(void);
@property (nonatomic, copy) void (^_Nullable cancelCallback)(void);

- (instancetype)initWithSliderDragAlertModel:(nullable BJPUStopPlayModel *)model;
- (instancetype)initWithAuditionTimeAlertModel:(nullable BJPUStopPlayModel *)model;

@end


NS_ASSUME_NONNULL_END
