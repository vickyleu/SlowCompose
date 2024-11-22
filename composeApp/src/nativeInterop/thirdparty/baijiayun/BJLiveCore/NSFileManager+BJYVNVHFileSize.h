//
//  NSFileManager+BJYVNVHFileSize.h
//  Pods
//
//  Created by Niels van Hoorn on 03/07/15.
//
//

#import <Foundation/Foundation.h>

@interface NSFileManager (BJYVNVHFileSize)

- (unsigned long long)fileSizeOfItemAtPath:(NSString *)path;

@end
