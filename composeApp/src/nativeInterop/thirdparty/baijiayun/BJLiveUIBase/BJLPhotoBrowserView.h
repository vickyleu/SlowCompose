//
//  BJLPhotoBrowserView.h
//  BJLiveUI
//
//  Created by Ney on 3/11/21.
//  Copyright © 2021 BaijiaYun. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BJLPhotoListViewController.h"

@interface BJLPhotoBrowserView: UIView
@property (nonatomic, assign) BOOL showImageWithFill; // defalut NO

@property (nonatomic, assign) BOOL showErrorWithBuildinUI; // defalut YES
@property (nonatomic, nullable) void (^hideCallback)(BJLPhotoBrowserView *_Nonnull view);
@property (nonatomic, nullable) void (^errorCallback)(BJLPhotoBrowserView *_Nonnull view, BJLPhotoMode *_Nonnull photo, NSError *_Nullable error);

- (instancetype _Nonnull)initWithPhotos:(NSArray<BJLPhotoMode *> *_Nullable)photo currentPhoto:(BJLPhotoMode *_Nullable)currentPhoto;

- (void)updatePhotos:(NSMutableArray<BJLPhotoMode *> *_Nullable)photo;
- (void)updatePhotosWithImageURLs:(NSArray<NSString *> *_Nullable)photoURLs;
- (void)updatePhotosWithImageURLs:(NSArray<NSString *> *_Nullable)photoURLs imageBgUrlString:(NSString *_Nullable)imageBgUrlString;
- (void)updateCurrentPhoto:(BJLPhotoMode *_Nullable)currentPhoto;
- (void)updateCurrentIndex:(NSInteger)index;
@end
