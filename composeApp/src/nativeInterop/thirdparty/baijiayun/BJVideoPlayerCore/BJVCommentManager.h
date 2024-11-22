//
//  BJVCommentManager.h
//  BJVideoPlayerUI
//
//  Created by ney on 2023/6/5.
//  Copyright © 2023 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJVCommentItem: NSObject
@property (nonatomic, copy, nullable) NSString *commentID;
@property (nonatomic, copy, nullable) NSString *comment;
@property (nonatomic, copy, nullable) NSString *userName;
@property (nonatomic, copy, nullable) NSString *userAvatar;
@property (nonatomic, copy, nullable) NSString *updateTime;
@property (nonatomic, assign) BOOL top;
@property (nonatomic, assign) BOOL like;
@property (nonatomic, assign) BOOL createdBySelf;
@end

@interface BJVCommentManager: NSObject

@property (nonatomic, readonly, nullable) NSString *userToken;

+ (instancetype)sharedInstance;

- (void)updateUserToken:(NSString *)userToken;

- (void)requestCommentWithVideoID:(NSString *)vid page:(NSInteger)page pageSize:(NSInteger)pageSize completedBlock:(void(^)(NSArray<BJVCommentItem *> * _Nullable data, NSError  * _Nullable error))completedBlock;

- (void)leaveCommentWithVideoID:(NSString *)vid comment:(NSString *)comment  avatar:(NSString *)avatar completedBlock:(void(^)(NSError  * _Nullable error))completedBlock;

- (void)likeCommentWithVideoID:(NSString *)vid commentID:(NSString *)commentID  like:(BOOL)like completedBlock:(void(^)(NSError  * _Nullable error))completedBlock;

- (void)deleteCommentWithVideoID:(NSString *)vid commentID:(NSString *)commentID  completedBlock:(void(^)(NSError  * _Nullable error))completedBlock;
@end

NS_ASSUME_NONNULL_END
