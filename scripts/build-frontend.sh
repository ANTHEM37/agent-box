#!/bin/bash

echo "=== 构建前端项目 ==="

cd frontend

# 检查 Node.js 版本
echo "🔍 检查 Node.js 版本..."
node --version
if [ $? -ne 0 ]; then
    echo "❌ Node.js 未安装或版本不符合要求"
    exit 1
fi

# 安装依赖
echo "📦 安装依赖..."
npm install
if [ $? -ne 0 ]; then
    echo "❌ 依赖安装失败"
    exit 1
fi

# 类型检查
echo "🔍 类型检查..."
npm run type-check
if [ $? -ne 0 ]; then
    echo "⚠️  类型检查失败，但继续构建"
fi

# 代码检查
echo "🔍 代码检查..."
npm run lint
if [ $? -ne 0 ]; then
    echo "⚠️  代码检查失败，但继续构建"
fi

# 构建项目
echo "🏗️  构建项目..."
npm run build
if [ $? -ne 0 ]; then
    echo "❌ 构建失败"
    exit 1
fi

echo "✅ 前端构建完成！"
echo "📁 构建文件位置: dist/"