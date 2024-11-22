//
//  BJPUStopPlayModel.h
//  BJPlayerUIBase
//
//  Created by ney on 2023/11/17.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJPUStopPlayModel: NSObject 
@property (nonatomic, copy, readonly, nullable) NSString *title;
@property (nonatomic, copy, readonly, nullable) NSString *titleIconURLString;
@property (nonatomic, copy, readonly, nullable) NSString *message;
@property (nonatomic, copy, readonly, nullable) NSString *conformButtonText;
@property (nonatomic, copy, readonly, nullable) NSString *cancelButtonText;

+ (instancetype)modelWithTitle:(nullable NSString *)title
                          icon:(nullable NSString *)icon
                       message:(nullable NSString *)message
             conformButtonText:(nullable NSString *)conformButtonText
              cancelButtonText:(nullable NSString *)cancelButtonText;
@end

NS_ASSUME_NONNULL_END
