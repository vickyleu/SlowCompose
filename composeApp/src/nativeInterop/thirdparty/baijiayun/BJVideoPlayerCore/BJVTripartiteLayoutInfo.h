//
//  BJVTripartiteLayoutInfo.h
//  BJVideoPlayerCore
//
//  Created by ney on 2022/12/23.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

#if __has_feature(modules) && BJL_USE_SEMANTIC_IMPORT
@import BJLiveBase;
#else
#import "../BJLiveBase/BJLiveBase.h"
#endif

NS_ASSUME_NONNULL_BEGIN

@interface BJVTripartiteLayoutPPT: NSObject <BJLYYModel, NSCopying>
@property (nonatomic, readonly) NSString *baseURL;
@property (nonatomic, readonly) NSString *fid;
@property (nonatomic, readonly) NSString *sn;
@end

@interface BJVTripartiteLayoutTxtItem: NSObject <BJLYYModel, NSCopying>
@property (nonatomic, readonly) NSInteger time;
@property (nonatomic, readonly) NSInteger page;
@property (nonatomic, readonly) NSString *outline;
@end

@interface BJVTripartiteLayoutTxt: NSObject <BJLYYModel, NSCopying>
@property (nonatomic, readonly) NSArray<BJVTripartiteLayoutTxtItem *> *content;
- (void)updateContent:(NSArray<BJVTripartiteLayoutTxtItem *> *)content;
@end

@interface BJVTripartiteLayoutInfo: NSObject <BJLYYModel, NSCopying>
@property (nonatomic, readonly) BJVTripartiteLayoutPPT *ppt;
@property (nonatomic, readonly) BJVTripartiteLayoutTxt *txt;
@end

NS_ASSUME_NONNULL_END
