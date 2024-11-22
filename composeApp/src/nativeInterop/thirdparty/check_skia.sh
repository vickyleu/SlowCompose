#!/bin/bash

# 设置要遍历的根目录
ROOT_DIR="/Volumes/Extra/Github/KMMCompose/composeApp/src/nativeInterop/thirdparty/TencentMeetingSDK"

# 遍历查找以 .framework 结尾的文件夹
find "$ROOT_DIR" -type d -name "*.framework" | while read -r framework_dir; do
    # 获取 xxxx.framework 中的 xxxx 名称
    framework_name=$(basename "$framework_dir" .framework)

    # 构造二进制文件路径
    binary_path="$framework_dir/$framework_name"

    # 检查二进制文件是否存在
    if [[ -f "$binary_path" ]]; then
        # 使用 otool 查找包含 "skia" 的符号
        otool -l "$binary_path" | grep -i "skia" > /dev/null

        # 判断是否找到 "skia"
        if [[ $? -eq 0 ]]; then
            echo "Found 'skia' in $binary_path"
        else
            echo "'skia' not found in $binary_path"
        fi
    else
        echo "Binary file not found in $framework_dir"
    fi
done