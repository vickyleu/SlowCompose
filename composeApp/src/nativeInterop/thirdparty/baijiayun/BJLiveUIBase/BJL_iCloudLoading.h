//
//  iCloudLoading.h
//  QBImagePicker
//
//  Created by MingLQ on 2017-01-14.
//  Copyright © 2017 MingLQ <minglq.9@gmail.com>. All rights reserved.
//

#import <MobileCoreServices/MobileCoreServices.h>
#import "../QBImagePickerController/QBImagePickerController.h"

NS_ASSUME_NONNULL_BEGIN

@class ICLImageFile;
@class PHPickerResult;

/**
 iCloudLoading
 Loading image-file<ICLImageFile *> with assets<PHAsset *> from iCloud.
 */
@interface QBImagePickerController (iCloudLoading)

/**
 #param contentMode     content mode for image, PHImageContentModeDefault(AspectFit) by default
 #param targetSize      size for image, PHImageManagerMaximumSize by default
 #param thumbnailSize   size for thumbnail, CGSizeZero by default
 */
- (void)icl_loadImageFilesWithAssets:(NSArray<PHAsset *> *)assets;
- (void)icl_loadImageFilesWithAssets:(NSArray<PHAsset *> *)assets
                         contentMode:(PHImageContentMode)contentMode
                          targetSize:(CGSize)targetSize
                       thumbnailSize:(CGSize)thumbnailSize;

@end

/**
 QBImagePickerControllerDelegate+iCloudLoading
 @see QBImagePickerController.delegate
 */
@protocol QBImagePickerControllerDelegate_iCloudLoading <QBImagePickerControllerDelegate>

@required

- (void)icl_imagePickerController:(QBImagePickerController *)imagePickerController
       didFinishLoadingImageFiles:(NSArray<ICLImageFile *> *)imageFiles;

@optional

// alert error like Photos App if not implemented
- (void)icl_imagePickerControllerDidFailLoadingImageFiles:(QBImagePickerController *)imagePickerController;
- (void)icl_imagePickerControllerDidCancelLoadingImageFiles:(QBImagePickerController *)imagePickerController;

- (void)icl_imagePickerController:(QBImagePickerController *)imagePickerController
        didFinishLoadingImageFile:(ICLImageFile *)imageFile;

@end

#pragma mark -

@interface ICLImageFile: NSObject

@property (nonatomic, readonly, copy) NSString *filePath, *fileName;
// UTI (i.e. (__bridge NSString *)kUTTypeImage)
@property (nonatomic, readonly, copy, nullable) NSString *mediaType;
// @property (nonatomic, readonly) UIImageOrientation orientation;

@property (nonatomic, readonly, nullable) UIImage *thumbnail;
@property (nonatomic, readonly, nullable) PHAsset *asset; // nonnull if create with asset
@property (nonatomic, readonly, nullable) PHPickerResult *pickerResult API_AVAILABLE(ios(14));
@property (nonatomic, readonly) CGSize imageSize;

- (NSURL *)fileURL;

+ (nullable instancetype)imageFileWithAsset:(PHAsset *)asset
                                  imageData:(NSData *)imageData
                                  thumbnail:(nullable UIImage *)thumbnail
                                  mediaType:(nullable NSString *)mediaType
                                      // orientation:(UIImageOrientation)orientation
                                      error:(NSError **)error;

+ (nullable instancetype)imageFileWithImage:(UIImage *)image
                                  thumbnail:(nullable UIImage *)thumbnail
                                  mediaType:(nullable NSString *)mediaType
                                      // orientation:(UIImageOrientation)orientation
                                      error:(NSError **)error;

+ (nullable instancetype)imageFileWithPickerResult:(PHPickerResult *)pickerResult
                                         thumbnail:(nullable UIImage *)thumbnail
                                          filePath:(NSString *)filePath
                                             error:(NSError **)error API_AVAILABLE(ios(14));
@end

NS_ASSUME_NONNULL_END
