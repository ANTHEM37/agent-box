#!/bin/bash

echo "=== 构建后端项目 ==="

cd backend

# 检查 Java 版本
echo "🔍 检查 Java 版本..."
java -version
if [ $? -ne 0 ]; then
    echo "❌ Java 未安装或版本不符合要求"
    exit 1
fi

# 清理并编译
echo "🧹 清理项目..."
mvn clean

echo "📦 编译项目..."
mvn compile
if [ $? -ne 0 ]; then
    echo "❌ 编译失败"
    exit 1
fi

echo "🧪 运行测试..."
mvn test
if [ $? -ne 0 ]; then
    echo "⚠️  测试失败，但继续构建"
fi

echo "📦 打包项目..."
mvn package -DskipTests
if [ $? -ne 0 ]; then
    echo "❌ 打包失败"
    exit 1
fi

echo "✅ 后端构建完成！"
echo "📁 JAR 文件位置: target/agent-platform-1.0.0.jar"