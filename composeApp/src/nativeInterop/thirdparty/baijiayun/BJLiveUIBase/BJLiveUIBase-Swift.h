#if 0
#elif defined(__arm64__) && __arm64__
// Generated by Apple Swift version 5.7.2 (swiftlang-5.7.2.135.5 clang-1400.0.29.51)
#ifndef BJLIVEUIBASE_SWIFT_H
#define BJLIVEUIBASE_SWIFT_H
#pragma clang diagnostic push
#pragma clang diagnostic ignored "-Wgcc-compat"

#if !defined(__has_include)
# define __has_include(x) 0
#endif
#if !defined(__has_attribute)
# define __has_attribute(x) 0
#endif
#if !defined(__has_feature)
# define __has_feature(x) 0
#endif
#if !defined(__has_warning)
# define __has_warning(x) 0
#endif

#if __has_include(<swift/objc-prologue.h>)
# include <swift/objc-prologue.h>
#endif

#pragma clang diagnostic ignored "-Wduplicate-method-match"
#pragma clang diagnostic ignored "-Wauto-import"
#if defined(__OBJC__)
#include <Foundation/Foundation.h>
#endif
#if defined(__cplusplus)
#include <cstdint>
#include <cstddef>
#include <cstdbool>
#else
#include <stdint.h>
#include <stddef.h>
#include <stdbool.h>
#endif

#if !defined(SWIFT_TYPEDEFS)
# define SWIFT_TYPEDEFS 1
# if __has_include(<uchar.h>)
#  include <uchar.h>
# elif !defined(__cplusplus)
typedef uint_least16_t char16_t;
typedef uint_least32_t char32_t;
# endif
typedef float swift_float2  __attribute__((__ext_vector_type__(2)));
typedef float swift_float3  __attribute__((__ext_vector_type__(3)));
typedef float swift_float4  __attribute__((__ext_vector_type__(4)));
typedef double swift_double2  __attribute__((__ext_vector_type__(2)));
typedef double swift_double3  __attribute__((__ext_vector_type__(3)));
typedef double swift_double4  __attribute__((__ext_vector_type__(4)));
typedef int swift_int2  __attribute__((__ext_vector_type__(2)));
typedef int swift_int3  __attribute__((__ext_vector_type__(3)));
typedef int swift_int4  __attribute__((__ext_vector_type__(4)));
typedef unsigned int swift_uint2  __attribute__((__ext_vector_type__(2)));
typedef unsigned int swift_uint3  __attribute__((__ext_vector_type__(3)));
typedef unsigned int swift_uint4  __attribute__((__ext_vector_type__(4)));
#endif

#if !defined(SWIFT_PASTE)
# define SWIFT_PASTE_HELPER(x, y) x##y
# define SWIFT_PASTE(x, y) SWIFT_PASTE_HELPER(x, y)
#endif
#if !defined(SWIFT_METATYPE)
# define SWIFT_METATYPE(X) Class
#endif
#if !defined(SWIFT_CLASS_PROPERTY)
# if __has_feature(objc_class_property)
#  define SWIFT_CLASS_PROPERTY(...) __VA_ARGS__
# else
#  define SWIFT_CLASS_PROPERTY(...)
# endif
#endif

#if __has_attribute(objc_runtime_name)
# define SWIFT_RUNTIME_NAME(X) __attribute__((objc_runtime_name(X)))
#else
# define SWIFT_RUNTIME_NAME(X)
#endif
#if __has_attribute(swift_name)
# define SWIFT_COMPILE_NAME(X) __attribute__((swift_name(X)))
#else
# define SWIFT_COMPILE_NAME(X)
#endif
#if __has_attribute(objc_method_family)
# define SWIFT_METHOD_FAMILY(X) __attribute__((objc_method_family(X)))
#else
# define SWIFT_METHOD_FAMILY(X)
#endif
#if __has_attribute(noescape)
# define SWIFT_NOESCAPE __attribute__((noescape))
#else
# define SWIFT_NOESCAPE
#endif
#if __has_attribute(ns_consumed)
# define SWIFT_RELEASES_ARGUMENT __attribute__((ns_consumed))
#else
# define SWIFT_RELEASES_ARGUMENT
#endif
#if __has_attribute(warn_unused_result)
# define SWIFT_WARN_UNUSED_RESULT __attribute__((warn_unused_result))
#else
# define SWIFT_WARN_UNUSED_RESULT
#endif
#if __has_attribute(noreturn)
# define SWIFT_NORETURN __attribute__((noreturn))
#else
# define SWIFT_NORETURN
#endif
#if !defined(SWIFT_CLASS_EXTRA)
# define SWIFT_CLASS_EXTRA
#endif
#if !defined(SWIFT_PROTOCOL_EXTRA)
# define SWIFT_PROTOCOL_EXTRA
#endif
#if !defined(SWIFT_ENUM_EXTRA)
# define SWIFT_ENUM_EXTRA
#endif
#if !defined(SWIFT_CLASS)
# if __has_attribute(objc_subclassing_restricted)
#  define SWIFT_CLASS(SWIFT_NAME) SWIFT_RUNTIME_NAME(SWIFT_NAME) __attribute__((objc_subclassing_restricted)) SWIFT_CLASS_EXTRA
#  define SWIFT_CLASS_NAMED(SWIFT_NAME) __attribute__((objc_subclassing_restricted)) SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
# else
#  define SWIFT_CLASS(SWIFT_NAME) SWIFT_RUNTIME_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
#  define SWIFT_CLASS_NAMED(SWIFT_NAME) SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_CLASS_EXTRA
# endif
#endif
#if !defined(SWIFT_RESILIENT_CLASS)
# if __has_attribute(objc_class_stub)
#  define SWIFT_RESILIENT_CLASS(SWIFT_NAME) SWIFT_CLASS(SWIFT_NAME) __attribute__((objc_class_stub))
#  define SWIFT_RESILIENT_CLASS_NAMED(SWIFT_NAME) __attribute__((objc_class_stub)) SWIFT_CLASS_NAMED(SWIFT_NAME)
# else
#  define SWIFT_RESILIENT_CLASS(SWIFT_NAME) SWIFT_CLASS(SWIFT_NAME)
#  define SWIFT_RESILIENT_CLASS_NAMED(SWIFT_NAME) SWIFT_CLASS_NAMED(SWIFT_NAME)
# endif
#endif

#if !defined(SWIFT_PROTOCOL)
# define SWIFT_PROTOCOL(SWIFT_NAME) SWIFT_RUNTIME_NAME(SWIFT_NAME) SWIFT_PROTOCOL_EXTRA
# define SWIFT_PROTOCOL_NAMED(SWIFT_NAME) SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_PROTOCOL_EXTRA
#endif

#if !defined(SWIFT_EXTENSION)
# define SWIFT_EXTENSION(M) SWIFT_PASTE(M##_Swift_, __LINE__)
#endif

#if !defined(OBJC_DESIGNATED_INITIALIZER)
# if __has_attribute(objc_designated_initializer)
#  define OBJC_DESIGNATED_INITIALIZER __attribute__((objc_designated_initializer))
# else
#  define OBJC_DESIGNATED_INITIALIZER
# endif
#endif
#if !defined(SWIFT_ENUM_ATTR)
# if defined(__has_attribute) && __has_attribute(enum_extensibility)
#  define SWIFT_ENUM_ATTR(_extensibility) __attribute__((enum_extensibility(_extensibility)))
# else
#  define SWIFT_ENUM_ATTR(_extensibility)
# endif
#endif
#if !defined(SWIFT_ENUM)
# define SWIFT_ENUM(_type, _name, _extensibility) enum _name : _type _name; enum SWIFT_ENUM_ATTR(_extensibility) SWIFT_ENUM_EXTRA _name : _type
# if __has_feature(generalized_swift_name)
#  define SWIFT_ENUM_NAMED(_type, _name, SWIFT_NAME, _extensibility) enum _name : _type _name SWIFT_COMPILE_NAME(SWIFT_NAME); enum SWIFT_COMPILE_NAME(SWIFT_NAME) SWIFT_ENUM_ATTR(_extensibility) SWIFT_ENUM_EXTRA _name : _type
# else
#  define SWIFT_ENUM_NAMED(_type, _name, SWIFT_NAME, _extensibility) SWIFT_ENUM(_type, _name, _extensibility)
# endif
#endif
#if !defined(SWIFT_UNAVAILABLE)
# define SWIFT_UNAVAILABLE __attribute__((unavailable))
#endif
#if !defined(SWIFT_UNAVAILABLE_MSG)
# define SWIFT_UNAVAILABLE_MSG(msg) __attribute__((unavailable(msg)))
#endif
#if !defined(SWIFT_AVAILABILITY)
# define SWIFT_AVAILABILITY(plat, ...) __attribute__((availability(plat, __VA_ARGS__)))
#endif
#if !defined(SWIFT_WEAK_IMPORT)
# define SWIFT_WEAK_IMPORT __attribute__((weak_import))
#endif
#if !defined(SWIFT_DEPRECATED)
# define SWIFT_DEPRECATED __attribute__((deprecated))
#endif
#if !defined(SWIFT_DEPRECATED_MSG)
# define SWIFT_DEPRECATED_MSG(...) __attribute__((deprecated(__VA_ARGS__)))
#endif
#if __has_feature(attribute_diagnose_if_objc)
# define SWIFT_DEPRECATED_OBJC(Msg) __attribute__((diagnose_if(1, Msg, "warning")))
#else
# define SWIFT_DEPRECATED_OBJC(Msg) SWIFT_DEPRECATED_MSG(Msg)
#endif
#if defined(__OBJC__)
#if !defined(IBSegueAction)
# define IBSegueAction
#endif
#endif
#if !defined(SWIFT_EXTERN)
# if defined(__cplusplus)
#  define SWIFT_EXTERN extern "C"
# else
#  define SWIFT_EXTERN extern
# endif
#endif
#if !defined(SWIFT_CALL)
# define SWIFT_CALL __attribute__((swiftcall))
#endif
#if defined(__cplusplus)
#if !defined(SWIFT_NOEXCEPT)
# define SWIFT_NOEXCEPT noexcept
#endif
#else
#if !defined(SWIFT_NOEXCEPT)
# define SWIFT_NOEXCEPT 
#endif
#endif
#if defined(__cplusplus)
#if !defined(SWIFT_CXX_INT_DEFINED)
#define SWIFT_CXX_INT_DEFINED
namespace swift {
using Int = ptrdiff_t;
using UInt = size_t;
}
#endif
#endif
#if defined(__OBJC__)
#if __has_feature(modules)
#if __has_warning("-Watimport-in-framework-header")
#pragma clang diagnostic ignored "-Watimport-in-framework-header"
#endif
@import BJLiveBase;
@import CoreFoundation;
@import Foundation;
@import ObjectiveC;
@import UIKit;
#endif

#endif
#pragma clang diagnostic ignored "-Wproperty-attribute-mismatch"
#pragma clang diagnostic ignored "-Wduplicate-method-arg"
#if __has_warning("-Wpragma-clang-attribute")
# pragma clang diagnostic ignored "-Wpragma-clang-attribute"
#endif
#pragma clang diagnostic ignored "-Wunknown-pragmas"
#pragma clang diagnostic ignored "-Wnullability"
#pragma clang diagnostic ignored "-Wdollar-in-identifier-extension"

#if __has_attribute(external_source_symbol)
# pragma push_macro("any")
# undef any
# pragma clang attribute push(__attribute__((external_source_symbol(language="Swift", defined_in="BJLiveUIBase",generated_declaration))), apply_to=any(function,enum,objc_interface,objc_category,objc_protocol))
# pragma pop_macro("any")
#endif

#if defined(__OBJC__)
@class NSString;
@class NSBundle;
@class NSCoder;

SWIFT_CLASS("_TtC12BJLiveUIBase32BJLChatOnWallPanelViewController")
@interface BJLChatOnWallPanelViewController : UIViewController
- (void)viewDidLoad;
- (void)viewDidLayoutSubviews;
- (nonnull instancetype)initWithNibName:(NSString * _Nullable)nibNameOrNil bundle:(NSBundle * _Nullable)nibBundleOrNil OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder OBJC_DESIGNATED_INITIALIZER;
@end


@class BJLRoom;

SWIFT_CLASS("_TtC12BJLiveUIBase24BJLPKSceneViewController")
@interface BJLPKSceneViewController : UIViewController
@property (nonatomic, copy) void (^ _Nullable closeSceneCallback)(BJLPKSceneViewController * _Nonnull);
@property (nonatomic, copy) void (^ _Nullable showMessageCallback)(NSString * _Nullable);
@property (nonatomic, copy) void (^ _Nullable updateConstraintCallback)(void);
- (nonnull instancetype)initWithRoom:(BJLRoom * _Nonnull)room OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)viewDidLoad;
- (nonnull instancetype)initWithNibName:(NSString * _Nullable)nibNameOrNil bundle:(NSBundle * _Nullable)nibBundleOrNil SWIFT_UNAVAILABLE;
@end




@class BJLAnswerRankModel;
@class BJLUser;
@class BJLAnswerSheet;

SWIFT_CLASS("_TtC12BJLiveUIBase25BJLQuestionAnswerRankView")
@interface BJLQuestionAnswerRankView : UIView
@property (nonatomic, copy) void (^ _Nullable updateTopRankCallback)(NSInteger);
@property (nonatomic, copy) BOOL (^ _Nullable sendZanCallback)(NSArray<BJLAnswerRankModel *> * _Nonnull);
@property (nonatomic, copy) void (^ _Nullable errorTipCallback)(NSString * _Nonnull);
- (nonnull instancetype)initWithUsers:(NSArray<BJLAnswerRankModel *> * _Nullable)users loginUser:(BJLUser * _Nonnull)loginUser room:(BJLRoom * _Nonnull)room OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)updateRankListWithUsers:(NSArray<BJLAnswerRankModel *> * _Nonnull)users currentAnswerSheet:(BJLAnswerSheet * _Nullable)currentAnswerSheet;
- (nonnull instancetype)initWithFrame:(CGRect)frame SWIFT_UNAVAILABLE;
@end


@class UITableView;
@class NSIndexPath;
@class UITableViewCell;

@interface BJLQuestionAnswerRankView (SWIFT_EXTENSION(BJLiveUIBase)) <UITableViewDataSource, UITableViewDelegate>
- (NSInteger)numberOfSectionsInTableView:(UITableView * _Nonnull)tableView SWIFT_WARN_UNUSED_RESULT;
- (NSInteger)tableView:(UITableView * _Nonnull)tableView numberOfRowsInSection:(NSInteger)section SWIFT_WARN_UNUSED_RESULT;
- (BOOL)tableView:(UITableView * _Nonnull)tableView shouldHighlightRowAtIndexPath:(NSIndexPath * _Nonnull)indexPath SWIFT_WARN_UNUSED_RESULT;
- (void)tableView:(UITableView * _Nonnull)tableView didSelectRowAtIndexPath:(NSIndexPath * _Nonnull)indexPath;
- (UITableViewCell * _Nonnull)tableView:(UITableView * _Nonnull)tableView cellForRowAtIndexPath:(NSIndexPath * _Nonnull)indexPath SWIFT_WARN_UNUSED_RESULT;
@end


SWIFT_CLASS("_TtC12BJLiveUIBase30BJLReportIllegalViewController")
@interface BJLReportIllegalViewController : BJLViewController <UITableViewDataSource, UITableViewDelegate>
@property (nonatomic, copy) void (^ _Nullable submitCallback)(NSString * _Nonnull);
@property (nonatomic, copy) void (^ _Nullable closeCallback)(void);
- (nonnull instancetype)initWithRoom:(BJLRoom * _Nonnull)room OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)viewDidLoad;
- (NSInteger)numberOfSectionsInTableView:(UITableView * _Nonnull)tableView SWIFT_WARN_UNUSED_RESULT;
- (UITableViewCell * _Nonnull)tableView:(UITableView * _Nonnull)tableView cellForRowAtIndexPath:(NSIndexPath * _Nonnull)indexPath SWIFT_WARN_UNUSED_RESULT;
- (NSInteger)tableView:(UITableView * _Nonnull)tableView numberOfRowsInSection:(NSInteger)section SWIFT_WARN_UNUSED_RESULT;
- (void)tableView:(UITableView * _Nonnull)tableView didSelectRowAtIndexPath:(NSIndexPath * _Nonnull)indexPath;
- (CGFloat)tableView:(UITableView * _Nonnull)tableView heightForRowAtIndexPath:(NSIndexPath * _Nonnull)indexPath SWIFT_WARN_UNUSED_RESULT;
- (nonnull instancetype)initWithNibName:(NSString * _Nullable)nibNameOrNil bundle:(NSBundle * _Nullable)nibBundleOrNil SWIFT_UNAVAILABLE;
@end



SWIFT_CLASS("_TtC12BJLiveUIBase27BJLRoundaboutViewController")
@interface BJLRoundaboutViewController : BJLViewController
- (CGSize)roundaboutContentSize SWIFT_WARN_UNUSED_RESULT;
@property (nonatomic, copy) void (^ _Nullable closeCakkback)(void);
- (nonnull instancetype)initWithCount:(NSInteger)count room:(BJLRoom * _Nonnull)room OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)viewDidLoad;
- (void)close;
- (void)startRotatewithWithTargetIndex:(NSInteger)targetIndex;
- (nonnull instancetype)initWithNibName:(NSString * _Nullable)nibNameOrNil bundle:(NSBundle * _Nullable)nibBundleOrNil SWIFT_UNAVAILABLE;
@end


@class BJLSellActivityModel;

SWIFT_CLASS("_TtC12BJLiveUIBase21BJLSActivityPanelView")
@interface BJLSActivityPanelView : UIView
@property (nonatomic, copy) void (^ _Nullable clickCallback)(BJLSellActivityModel * _Nullable);
- (nonnull instancetype)initWithSuperView:(UIView * _Nonnull)superView OBJC_DESIGNATED_INITIALIZER;
- (void)updateWithModel:(BJLSellActivityModel * _Nonnull)model userName:(NSString * _Nonnull)userName goodsName:(NSString * _Nonnull)goodsName;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)layoutSubviews;
- (void)stopAnimation;
- (nonnull instancetype)initWithFrame:(CGRect)frame SWIFT_UNAVAILABLE;
@end



SWIFT_CLASS("_TtC12BJLiveUIBase29BJLSellMoreMenuViewController")
@interface BJLSellMoreMenuViewController : BJLViewController
- (nonnull instancetype)initWithRoom:(BJLRoom * _Nonnull)room OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)viewDidLoad;
- (nonnull instancetype)initWithNibName:(NSString * _Nullable)nibNameOrNil bundle:(NSBundle * _Nullable)nibBundleOrNil SWIFT_UNAVAILABLE;
@end


enum BJLSellTopMenuViewState : NSInteger;
@class BJLScSegment;
@class BJLVerticalButton;
@class UISwitch;
@class UILabel;
enum BJLSellTopMenuViewType : NSInteger;

SWIFT_CLASS("_TtC12BJLiveUIBase18BJLSellTopMenuView")
@interface BJLSellTopMenuView : UIView
@property (nonatomic, copy) NSString * _Nullable sellGoodsWordDisplayName;
@property (nonatomic, copy) void (^ _Nullable searchCallback)(NSString * _Nonnull);
@property (nonatomic, copy) void (^ _Nullable reloadCallback)(void);
@property (nonatomic, copy) void (^ _Nullable uploadNewSortRankCallback)(void);
@property (nonatomic) enum BJLSellTopMenuViewState viewState;
@property (nonatomic, strong) BJLScSegment * _Nonnull segment;
@property (nonatomic, strong) BJLVerticalButton * _Nonnull sortButton;
@property (nonatomic, strong) BJLVerticalButton * _Nonnull searchButton;
@property (nonatomic, strong) UISwitch * _Nonnull showShoppingSwitch;
@property (nonatomic, strong) UILabel * _Nonnull showShoppingLabel;
@property (nonatomic, strong) BJLVerticalButton * _Nonnull moreButton;
@property (nonatomic, strong) BJLVerticalButton * _Nonnull closeButton;
- (nonnull instancetype)initWithRole:(BJLUserRole)role type:(enum BJLSellTopMenuViewType)type OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)updateTitleOnShelfCount:(NSInteger)onShelfCount allGoodsCount:(NSInteger)allGoodsCount;
- (void)switchViewType:(enum BJLSellTopMenuViewState)type;
- (nonnull instancetype)initWithFrame:(CGRect)frame SWIFT_UNAVAILABLE;
@end

@class UITextField;

@interface BJLSellTopMenuView (SWIFT_EXTENSION(BJLiveUIBase)) <UITextFieldDelegate>
- (BOOL)textFieldShouldReturn:(UITextField * _Nonnull)textField SWIFT_WARN_UNUSED_RESULT;
@end


typedef SWIFT_ENUM(NSInteger, BJLSellTopMenuViewState, open) {
  BJLSellTopMenuViewStateNormal = 0,
  BJLSellTopMenuViewStateSearch = 1,
  BJLSellTopMenuViewStateReSort = 2,
  BJLSellTopMenuViewStateStudent = 3,
};

typedef SWIFT_ENUM(NSInteger, BJLSellTopMenuViewType, open) {
  BJLSellTopMenuViewTypeSell = 0,
  BJLSellTopMenuViewTypeLargeClass = 1,
};


SWIFT_CLASS("_TtC12BJLiveUIBase36BJLStudentAnswerWindowViewController")
@interface BJLStudentAnswerWindowViewController : BJLViewController
@property (nonatomic, copy) void (^ _Nullable closeCallback)(void);
@property (nonatomic, copy) void (^ _Nullable errorCallback)(NSString * _Nonnull);
@property (nonatomic, copy) BOOL (^ _Nullable submitCallback)(BJLAnswerSheet * _Nonnull);
- (nonnull instancetype)initWithRoom:(BJLRoom * _Nonnull)room answerSheet:(BJLAnswerSheet * _Nonnull)answerSheet OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)viewDidLoad;
- (void)closeByForce;
- (CGSize)presentationSizeWithIsFullScreenInEE:(BOOL)isFullScreenInEE SWIFT_WARN_UNUSED_RESULT;
- (NSInteger)answerSheetOptionsCount SWIFT_WARN_UNUSED_RESULT;
- (void)relayoutSubViewWithIsFullScreenInEE:(BOOL)isFullScreenInEE;
- (nonnull instancetype)initWithNibName:(NSString * _Nullable)nibNameOrNil bundle:(NSBundle * _Nullable)nibBundleOrNil SWIFT_UNAVAILABLE;
@end





typedef SWIFT_ENUM(NSInteger, BJLToolboxLayoutPosition, open) {
  BJLToolboxLayoutPositionTopCenter = 0,
  BJLToolboxLayoutPositionBottomCenter = 1,
  BJLToolboxLayoutPositionLeftCenter = 2,
  BJLToolboxLayoutPositionRightCenter = 3,
};

typedef SWIFT_ENUM(NSInteger, BJLToolboxLayoutType, open) {
  BJLToolboxLayoutTypeNormal = 0,
  BJLToolboxLayoutTypeMaximized = 1,
  BJLToolboxLayoutTypeFullScreen = 2,
};


SWIFT_CLASS("_TtC12BJLiveUIBase24BJLToolboxViewController")
@interface BJLToolboxViewController : UIViewController
@property (nonatomic) BOOL disableCoureware;
@property (nonatomic) BOOL disableTeachingAid;
@property (nonatomic) BOOL useHorizontalShapeView;
@property (nonatomic) BOOL enableScreenShare;
@property (nonatomic, copy) void (^ _Nullable showMessageCallback)(NSString * _Nonnull);
@property (nonatomic, copy) void (^ _Nullable pptButtonClickCallback)(BOOL);
@property (nonatomic, copy) void (^ _Nullable hideSecondaryViewCallback)(void);
@property (nonatomic, copy) void (^ _Nullable drawingStateChangeCallback)(BOOL);
@property (nonatomic, copy) void (^ _Nullable showCoursewareCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showCountDownTimerCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showQuestionAnswerCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showEnvelopeRainCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showRollCallCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showQuestionResponderCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showWritingBoardCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showOpenWebWiewCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showOpenRoundaboutCallback)(void);
@property (nonatomic, copy) void (^ _Nullable cleanAllPaintCallback)(void);
@property (nonatomic, copy) void (^ _Nullable showScreenShareCallback)(void);
@property (nonatomic, readonly) BOOL expectedHidden;
@property (nonatomic, copy) void (^ _Nullable toolHiddenChangeCallback)(void);
- (nonnull instancetype)initWithRoom:(BJLRoom * _Nonnull)room OBJC_DESIGNATED_INITIALIZER;
- (nullable instancetype)initWithCoder:(NSCoder * _Nonnull)coder SWIFT_UNAVAILABLE;
- (void)loadView;
- (void)viewDidLoad;
- (nonnull instancetype)initWithNibName:(NSString * _Nullable)nibNameOrNil bundle:(NSBundle * _Nullable)nibBundleOrNil SWIFT_UNAVAILABLE;
@end










@interface BJLToolboxViewController (SWIFT_EXTENSION(BJLiveUIBase))
- (void)setupLayoutViewWithType:(enum BJLToolboxLayoutType)type position:(enum BJLToolboxLayoutPosition)position direction:(enum UILayoutConstraintAxis)direction insets:(UIEdgeInsets)insets view:(UIView * _Nonnull)view controller:(UIViewController * _Nullable)controller;
- (void)updateLayoutInfoWithType:(enum BJLToolboxLayoutType)type insets:(UIEdgeInsets)insets;
- (void)remakeConstraintsWithLayoutType:(enum BJLToolboxLayoutType)type;
- (void)showRollCallBadgePoint:(BOOL)show;
- (BOOL)pptButtonIsSelect SWIFT_WARN_UNUSED_RESULT;
- (void)updateToolViewHidden:(BOOL)hidden;
- (enum BJLToolboxLayoutType)currentLayoutType SWIFT_WARN_UNUSED_RESULT;
- (void)updatePanEnable:(BOOL)enable;
@end

#endif
#if defined(__cplusplus)
#endif
#if __has_attribute(external_source_symbol)
# pragma clang attribute pop
#endif
#pragma clang diagnostic pop
#endif

#else
#error unsupported Swift architecture
#endif
