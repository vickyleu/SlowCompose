//
//  BJVCDNInfo.h
//  BJVideoPlayerCore
//
//  Created by HuangJie on 2018/8/24.
//  Copyright © 2018年 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "../BJLiveBase/BJLYYModel.h"

NS_ASSUME_NONNULL_BEGIN

@interface BJVCDNInfo: NSObject <BJLYYModel>

@property (nonatomic, readonly) NSString *cdn;
@property (nonatomic, readonly) NSString *playURL;

@end

NS_ASSUME_NONNULL_END
