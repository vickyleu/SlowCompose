//
//  FATWidgetView.h
//  FinApplet
//
//  Created by 滔 on 2023/6/8.
//  Copyright © 2023 finogeeks. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "FATWidgetInfo.h"

NS_ASSUME_NONNULL_BEGIN
@class FATWidgetView;
@protocol FATWidgetViewDelegate <NSObject>

/// 小组件内容宽高更新，宿主APP可以根据业务需要决定是否更新小组件的区域(sdk不会主动将小组件宽高设置成内容宽高)
/// - Parameter widgetView: 小组件视图
/// - Parameter size: 小组件内容宽高
- (void)onWidgetView:(FATWidgetView *)widgetView contentSizeUpdate:(CGSize)size;

@end

@interface FATWidgetView : UIView
@property (nonatomic, strong) NSString *widgetId;
@property (nonatomic, weak) id<FATWidgetViewDelegate> delegate;

/// 小组件的内容宽高
@property (nonatomic, assign) CGSize contentSize;
@end

NS_ASSUME_NONNULL_END
