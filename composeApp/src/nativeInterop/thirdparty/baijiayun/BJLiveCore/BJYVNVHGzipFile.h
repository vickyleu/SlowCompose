//
//  BJYVNVHGzipFile.h
//  Pods
//
//  Created by Niels van Hoorn on 26/03/14.
//
//

#import <Foundation/Foundation.h>
#import "BJYVNVHFile.h"

@interface BJYVNVHGzipFile : BJYVNVHFile

- (BOOL)inflateToPath:(NSString *)destinationPath error:(NSError **)error;
- (void)inflateToPath:(NSString *)destinationPath completion:(void(^)(NSError *))completion;

- (BOOL)deflateFromPath:(NSString *)sourcePath error:(NSError **)error;
- (void)deflateFromPath:(NSString *)sourcePath completion:(void(^)(NSError *))completion;

@end
