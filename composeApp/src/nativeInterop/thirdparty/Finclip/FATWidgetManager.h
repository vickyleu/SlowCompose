//
//  FATWidgetManager.h
//  FinApplet
//  小组件管理器
//  Created by 滔 on 2023/6/8.
//  Copyright © 2023 finogeeks. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "FATWidgetRequest.h"
#import "FATWidgetView.h"
#import "FATError.h"

@protocol FATWidgetDelegate <NSObject>

/// 小组件关闭
/// - Parameter widgetView: 小组件视图
- (void)onWidgetClose:(FATWidgetView *_Nullable)widgetView;


/// 小组件被清除
/// - Parameter widgetId: 小组件id
- (void)onWidgetDestory:(NSString *_Nullable)widgetId;

@end

@interface FATWidgetManager : NSObject
@property (nonatomic, weak) id<FATWidgetDelegate> delegate;

/// 创建widget
/// - Parameters:
///   - request: 请求参数
///   - parentViewController: 组件所在的视图控制器，如果不传，部分API可能没法使用
///   - completion: 创建结果，创建失败时widgetView为nil
- (void)createWidget:(FATWidgetRequest *)request parentViewController:(UIViewController *)parentViewController completion:(void (^)(FATWidgetView *_Nullable widgetView, FATError *error))completion;

/// 以二维码方式创建widget
/// - Parameters:
///   - request: 请求参数
///   - parentViewController: 组件所在的视图控制器，如果不传，部分API可能没法使用
///   - completion: 创建结果，创建失败时widgetView为nil
- (void)createWidgetWithQRCode:(FATWidgetQRCodeRequest *)request parentViewController:(UIViewController *)parentViewController completion:(void (^)(FATWidgetView *_Nullable widgetView, FATError *error))completion;


///  以加密信息的方式创建widget
/// - Parameters:
///   - request: 请求参数
///   - parentViewController: 组件所在的视图控制器，如果不传，部分API可能没法使用
///    - completion: 创建结果，创建失败时widgetView为nil
- (void)createWidgetWithDecryptInfo:(FATWidgetDecryptRequest *)request parentViewController:(UIViewController *)parentViewController completion:(void (^)(FATWidgetView *_Nullable widgetView, FATError *error))completion;


/// 打开本地小组件
/// - Parameters:
///   - request: 请求参数
///   - parentViewController: 组件所在的视图控制器，如果不传，部分API可能没法使用
///    - completion: 创建结果，创建失败时widgetView为nil
- (void)createLocalWidget:(FATWidgetRequest *)request parentViewController:(UIViewController *)parentViewController completion:(void (^)(FATWidgetView *_Nullable widgetView, FATError *error))completion;



/// 清理wdiget
/// - Parameter widgetId: widgetId
- (void)cleanWidget:(NSString *)widgetId;


/// 清理所有widget
- (void)cleanAllWidget;



@end


