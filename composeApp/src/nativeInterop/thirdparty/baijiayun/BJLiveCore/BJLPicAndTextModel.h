//
//  BJLPicAndTextModel.h
//  BJLiveUIEE
//
//  Created by 凡义 on 2022/11/2.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJLPicAndTextModel : NSObject

@property (nonatomic, readonly) NSString *graphiID;
@property (nonatomic, readonly, nullable) NSString *content;
@property (nonatomic, readonly, nullable) NSArray<NSString *> *imagelist;
@property (nonatomic, readonly, nullable) NSString *creatTime;

@end

NS_ASSUME_NONNULL_END
