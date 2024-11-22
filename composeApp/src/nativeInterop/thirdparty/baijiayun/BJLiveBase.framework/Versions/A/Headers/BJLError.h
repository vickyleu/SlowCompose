//
//  BJLError.h
//  BJLiveBase
//
//  Created by MingLQ on 2018-05-11.
//  Copyright (c) 2018 MingLQ <minglq.9@gmail.com>. Released under the MIT license.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

FOUNDATION_EXPORT NSString *const BJLErrorSourceErrorKey;

@protocol BJLError <NSObject>
@property (nonatomic, readonly, nullable) NSError *bjl_sourceError;
@end

@interface NSError (BJLError) <BJLError>
@end

typedef NSError<BJLError> BJLError;

NS_ASSUME_NONNULL_END
