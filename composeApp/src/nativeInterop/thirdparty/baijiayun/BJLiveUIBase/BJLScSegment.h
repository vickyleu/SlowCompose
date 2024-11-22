//
//  BJLScSegment.h
//  BJLiveUI
//
//  Created by xijia dai on 2019/9/23.
//  Copyright © 2019 BaijiaYun. All rights reserved.
//

#import <UIKit/UIKit.h>

typedef NS_ENUM(NSInteger, BJLSegmentStyle) {
    BJLSegmentStyleUnderline,
    BJLSegmentStyleRoundCorner,
};

NS_ASSUME_NONNULL_BEGIN

@interface BJLScSegment: UIView

@property (nonatomic) NSInteger selectedIndex;
@property (nonatomic) CGFloat bottomlLineGap; //Underline类型时，距离文字label的距离，默认0
- (instancetype)initWithItems:(NSArray<NSString *> *)items
                        width:(CGFloat)width
                     fontSize:(CGFloat)fontSize
               bottomlLineGap:(CGFloat)bottomlLineGap
                    textColor:(UIColor *)textColor
                        style:(BJLSegmentStyle)style;

- (instancetype)initWithItems:(NSArray<NSString *> *)items
                        width:(CGFloat)width
                     fontSize:(CGFloat)fontSize
               bottomlLineGap:(CGFloat)bottomlLineGap
                    textColor:(nonnull UIColor *)textColor
            selectedTextColor:(nullable UIColor *)selectedTextColor
                        style:(BJLSegmentStyle)style;

- (void)setTitle:(nullable NSString *)title forSegmentAtIndex:(NSInteger)index;
- (void)setImage:(nullable UIImage *)image forSegmentAtIndex:(NSInteger)index;
- (void)updateRedDotAtIndex:(NSInteger)index count:(NSInteger)count ignoreCount:(BOOL)ignoreCount;
- (void)updateButtonEnableAtIndex:(NSInteger)index
           userInteractionEnabled:(BOOL)enable;

@end

NS_ASSUME_NONNULL_END
