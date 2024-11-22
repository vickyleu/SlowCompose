//
//  BJVQuizManager.h
//  BJVideoPlayerCore
//
//  Created by ney on 2022/10/25.
//  Copyright © 2022 BaijiaYun. All rights reserved.
//

#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface BJVOptionsItem: NSObject
@property (nonatomic, copy) NSString *title;
@property (nonatomic, assign) BOOL isCorrect;
@property (nonatomic, assign) BOOL selected;
@property (nonatomic, assign) NSInteger displayOrder;
@end

@interface BJVQuizItem: NSObject
@property (nonatomic, assign) NSInteger quizID;
@property (nonatomic, assign) BOOL singleChoice;
@property (nonatomic, copy) NSString *title;
@property (nonatomic, copy) NSString *imageURL;
@property (nonatomic, copy) NSString *rightHint;
@property (nonatomic, copy) NSString *wrongHint;
@property (nonatomic, assign) BOOL canSkip;
@property (nonatomic, assign) NSTimeInterval timeOffset;
@property (nonatomic, assign) NSTimeInterval backInterval;
@property (nonatomic, strong) NSArray<BJVOptionsItem *> *options;
@property (nonatomic, assign) BOOL didShown;
@end

@interface BJVQuizManager: NSObject
/**
 点播：请求测试题目数据

 #param vid        视频vid
 #param userID      用户id
 #param token       token
 #param completion  请求完成回调
 #return            请求的 task
 */
+ (NSURLSessionTask *)getQuizListWithVideoVID:(NSString *)vid
                                   userNumber:(NSString *)userNumber
                                        token:(NSString *)token
                                   completion:(nullable void (^)(NSURLSessionDataTask *_Nullable task,
                                                                 NSArray<BJVQuizItem *> *_Nullable quizList,
                                                                 NSError *_Nullable error))completion;

/**
 点播：上报答题结果

 #param quizID      题目 id
 #param answerIDs   把所有答案id用英文逗号拼接起来的字符串
 #param correct     是否回答正确
 #param vid         视频vid
 #param userName    用户名字
 #param userNumber  用户number，用来唯一标志用户的字段
 #param token       token
 #param completion  请求完成回调
 #return            请求的 task
 */
+ (NSURLSessionTask *)reportQuizResultWithQuizID:(NSString *)quizID
                                       answerIDs:(NSString *)answerIDs
                                         correct:(BOOL)correct
                                             vID:(NSString *)vid
                                        userName:(NSString *)userName
                                      userNumber:(NSString *)userNumber
                                           token:(NSString *)token
                                      completion:(nullable void (^)(NSURLSessionDataTask *_Nullable task, NSError *_Nullable error))completion;
@end

NS_ASSUME_NONNULL_END
