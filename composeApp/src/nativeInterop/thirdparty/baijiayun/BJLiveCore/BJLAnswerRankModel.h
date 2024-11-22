//
//  BJLAnswerRankModel.h
//  BJLiveCore
//
//  Created by 凡义 on 2022/12/1.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJLAnswerRankModel : NSObject

@property (nonatomic, readonly) NSString *name;
@property (nonatomic, readonly) NSNumber *rank;
@property (nonatomic, readonly) NSString *userNumber;
@property (nonatomic, readonly) NSInteger groupID;

@end

NS_ASSUME_NONNULL_END
