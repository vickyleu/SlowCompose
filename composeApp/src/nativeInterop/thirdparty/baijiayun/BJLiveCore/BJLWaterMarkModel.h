//
//  BJLWaterMarkModel.h
//  BJLiveCore
//
//  Created by 凡义 on 2024/3/6.
//  Copyright © 2024 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

typedef NS_ENUM(NSInteger, BJLWaterMarkPosition) {
    BJLWaterMarkPosition_None = 0, //不显示
    BJLWaterMarkPosition_LeftUp = 1, //左上
    BJLWaterMarkPosition_RightUp = 2, //右上
    BJLWaterMarkPosition_RightDown = 3, //右下
    BJLWaterMarkPosition_LeftDown = 4, //左下
};

@interface BJLWaterMarkModel: NSObject

@property (nonatomic, copy, readonly) NSString *url;
@property (nonatomic, assign, readonly) BJLWaterMarkPosition position;

+ (instancetype)waterMarkModelWithUrl:(NSString *)url
                             position:(BJLWaterMarkPosition )position;

@end
NS_ASSUME_NONNULL_END
