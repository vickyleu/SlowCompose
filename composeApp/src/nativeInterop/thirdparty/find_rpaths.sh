#!/bin/bash

# 设置要搜索的目录
SEARCH_DIR=$1

# 检查是否提供了目录参数
if [ -z "$SEARCH_DIR" ]; then
    echo "Usage: $0 <directory>"
    exit 1
fi

# 查找目录下所有的 .framework 文件夹
find "$SEARCH_DIR" -type d -name "*.framework" | while read framework; do
    # 获取框架名
    FRAMEWORK_NAME=$(basename "$framework" .framework)
    # 找到 .framework 文件夹中的二进制文件
    BINARY="$framework/$FRAMEWORK_NAME"

    if [ -f "$BINARY" ]; then
        # 查找并打印库信息
        LIBS=$(otool -L "$BINARY" | grep "@rpath")

        if [ -n "$LIBS" ]; then
            echo "Binary: $BINARY"
            echo "Libraries found:"
            echo "$LIBS"
            echo "--------------------------"
        fi
    fi
done