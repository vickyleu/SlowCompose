//
//  BJLEERoomListItem.h
//  BJLiveCore
//
//  Created by ney on 2022/10/28.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJLEERoomListItem: NSObject
@property (nonatomic, strong, readonly) NSString *imageURL;
@property (nonatomic, strong, readonly) NSString *roomID;
@property (nonatomic, strong, readonly) NSString *title;
@property (nonatomic, strong, readonly) NSString *enterURL;
@property (nonatomic, readonly) NSInteger displayOrder;
@end

@interface BJLEERoomListGroup: NSObject
@property (nonatomic, strong, readonly) NSString *name;
@property (nonatomic, strong, readonly) NSArray<BJLEERoomListItem *> *items;
@end
NS_ASSUME_NONNULL_END
