#!/bin/bash

echo "=== 启动 AI Agent Platform 开发环境 ==="

# 检查 Docker 是否运行
if ! docker info > /dev/null 2>&1; then
    echo "❌ Docker 未运行，请先启动 Docker"
    exit 1
fi

# 启动基础服务
echo "🚀 启动基础服务..."
docker-compose up -d

# 等待服务启动
echo "⏳ 等待服务启动..."
sleep 10

# 检查服务状态
echo "🔍 检查服务状态..."
docker-compose ps

# 验证数据库连接
echo "📊 验证数据库连接..."
docker exec agent-postgres pg_isready -U admin -d agent_platform
if [ $? -eq 0 ]; then
    echo "✅ PostgreSQL 连接正常"
else
    echo "❌ PostgreSQL 连接失败"
fi

# 验证 Redis 连接
echo "🔄 验证 Redis 连接..."
docker exec agent-redis redis-cli ping > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Redis 连接正常"
else
    echo "❌ Redis 连接失败"
fi

# 验证 Chroma 连接
echo "🧠 验证 Chroma 连接..."
curl -s http://localhost:8000/api/v1/heartbeat > /dev/null
if [ $? -eq 0 ]; then
    echo "✅ Chroma 连接正常"
else
    echo "❌ Chroma 连接失败"
fi

echo ""
echo "🎉 基础服务启动完成！"
echo ""
echo "📋 服务访问地址："
echo "  - PostgreSQL: localhost:5432"
echo "  - Redis: localhost:6379"
echo "  - Chroma: http://localhost:8000"
echo "  - MinIO: http://localhost:9000 (控制台: http://localhost:9001)"
echo "  - RabbitMQ: http://localhost:15672 (用户名: admin, 密码: password)"
echo ""
echo "🔧 接下来请运行："
echo "  1. 后端: cd backend && mvn spring-boot:run"
echo "  2. 前端: cd frontend && npm install && npm run dev"