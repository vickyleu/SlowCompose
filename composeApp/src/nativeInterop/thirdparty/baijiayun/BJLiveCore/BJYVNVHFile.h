//
//  BJYVNVHFile.h
//  Pods
//
//  Created by Niels van Hoorn on 26/03/14.
//
//

#import <Foundation/Foundation.h>

@interface BJYVNVHFile : NSObject

@property (nonatomic, readonly) NSString *filePath;
@property (nonatomic, assign, readonly) unsigned long long fileSize;

- (instancetype)initWithPath:(NSString *)filePath;

@end
