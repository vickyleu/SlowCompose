//
//  BJLSellListViewController.h
//  BJLiveApp
//
//  Created by 凡义 on 2020/11/13.
//  Copyright © 2020 BaijiaYun. All rights reserved.
//

#import "../BJLiveCore/BJLiveCore.h"

#import "../BJLiveBase/BJLTableViewController.h"

typedef NS_ENUM(NSUInteger, BJLSellListViewControllerType) {
    BJLSellListViewControllerTypeSellUI,
    BJLSellListViewControllerTypeLargeClass,
};

NS_ASSUME_NONNULL_BEGIN

/**
 直播带货默认商品列表页面
 */
@interface BJLSellListViewController: BJLTableViewController

@property (nonatomic) void (^showBuyDetailViewCallback)(BJLSellItem *item);
@property (nonatomic) void (^closeCallback)(void);

- (instancetype)initWithRoom:(BJLRoom *)room;

- (instancetype)initWithRoom:(BJLRoom *)room type:(BJLSellListViewControllerType)type;

- (instancetype)init NS_UNAVAILABLE;

@end

NS_ASSUME_NONNULL_END
