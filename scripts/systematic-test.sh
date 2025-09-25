#!/bin/bash

# 系统功能验证脚本
# 用于验证所有模块是否正常工作

echo "========================================"
echo "AI Agent Platform 系统功能验证脚本"
echo "========================================"

# 配置
BASE_URL="http://localhost:8080/api"
USERNAME="testuser_$(date +%s)"
EMAIL="${USERNAME}@example.com"
PASSWORD="testpassword123"
FULL_NAME="Test User $(date +%s)"

echo "使用用户信息: $USERNAME / $EMAIL"

# 保存token的变量
TOKEN=""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 打印结果的函数
print_result() {
    if [ $1 -eq 0 ]; then
        echo -e "${GREEN}✓ 成功${NC} - $2"
    else
        echo -e "${RED}✗ 失败${NC} - $2"
        exit 1
    fi
}

print_info() {
    echo -e "${YELLOW}→${NC} $1"
}

# 1. 用户认证测试
print_info "1. 测试用户注册功能"

REGISTER_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/register" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"email\":\"$EMAIL\",\"password\":\"$PASSWORD\",\"fullName\":\"$FULL_NAME\"}")

REGISTER_CODE=$?
echo "$REGISTER_RESPONSE" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    TOKEN=$(echo "$REGISTER_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    print_result 0 "用户注册成功"
else
    print_result 1 "用户注册失败: $REGISTER_RESPONSE"
fi

# 2. 用户登录测试
print_info "2. 测试用户登录功能"

LOGIN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
    -H "Content-Type: application/json" \
    -d "{\"username\":\"$USERNAME\",\"password\":\"$PASSWORD\"}")

LOGIN_CODE=$?
echo "$LOGIN_RESPONSE" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    NEW_TOKEN=$(echo "$LOGIN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
    if [ -n "$NEW_TOKEN" ]; then
        TOKEN=$NEW_TOKEN
    fi
    print_result 0 "用户登录成功"
else
    print_result 1 "用户登录失败: $LOGIN_RESPONSE"
fi

# 3. 获取当前用户信息
print_info "3. 测试获取当前用户信息"

USER_RESPONSE=$(curl -s -X GET "$BASE_URL/auth/me" \
    -H "Authorization: Bearer $TOKEN")

USER_CODE=$?
echo "$USER_RESPONSE" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    print_result 0 "获取用户信息成功"
else
    print_result 1 "获取用户信息失败: $USER_RESPONSE"
fi

# 4. 知识库功能测试
print_info "4. 测试知识库功能"

# 4.1 创建知识库
KB_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/knowledge-bases" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{"name":"测试知识库","description":"系统测试创建的知识库","embeddingModel":"text-embedding-ada-002","chunkSize":500,"chunkOverlap":50}')

KB_CREATE_CODE=$?
KB_ID=$(echo "$KB_CREATE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -n "$KB_ID" ]; then
    print_result 0 "创建知识库成功 (ID: $KB_ID)"
else
    print_result 1 "创建知识库失败: $KB_CREATE_RESPONSE"
fi

# 4.2 获取知识库列表
print_info "  4.2 获取知识库列表"

KB_LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/knowledge-bases" \
    -H "Authorization: Bearer $TOKEN")

KB_LIST_CODE=$?
echo "$KB_LIST_RESPONSE" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    print_result 0 "获取知识库列表成功"
else
    print_result 1 "获取知识库列表失败: $KB_LIST_RESPONSE"
fi

# 4.3 获取知识库详情
print_info "  4.3 获取知识库详情"

KB_DETAIL_RESPONSE=$(curl -s -X GET "$BASE_URL/knowledge-bases/$KB_ID" \
    -H "Authorization: Bearer $TOKEN")

KB_DETAIL_CODE=$?
echo "$KB_DETAIL_RESPONSE" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    print_result 0 "获取知识库详情成功"
else
    print_result 1 "获取知识库详情失败: $KB_DETAIL_RESPONSE"
fi

# 5. 工作流功能测试
print_info "5. 测试工作流功能"

# 5.1 创建工作流
WF_CREATE_RESPONSE=$(curl -s -X POST "$BASE_URL/workflows" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "name":"测试工作流",
        "description":"系统测试创建的工作流",
        "definition":{
            "nodes":[
                {"id":"node-1","type":"start","name":"开始","position":{"x":100,"y":100}},
                {"id":"node-2","type":"end","name":"结束","position":{"x":300,"y":100}}
            ],
            "edges":[
                {"id":"edge-1","source":"node-1","target":"node-2"}
            ]
        },
        "category":"测试",
        "tags":["系统测试"]
    }')

WF_CREATE_CODE=$?
WF_ID=$(echo "$WF_CREATE_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -n "$WF_ID" ]; then
    print_result 0 "创建工作流成功 (ID: $WF_ID)"
else
    print_result 1 "创建工作流失败: $WF_CREATE_RESPONSE"
fi

# 5.2 获取工作流列表
print_info "  5.2 获取工作流列表"

WF_LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/workflows" \
    -H "Authorization: Bearer $TOKEN")

WF_LIST_CODE=$?
echo "$WF_LIST_RESPONSE" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    print_result 0 "获取工作流列表成功"
else
    print_result 1 "获取工作流列表失败: $WF_LIST_RESPONSE"
fi

# 5.3 获取工作流详情
print_info "  5.3 获取工作流详情"

WF_DETAIL_RESPONSE=$(curl -s -X GET "$BASE_URL/workflows/$WF_ID" \
    -H "Authorization: Bearer $TOKEN")

WF_DETAIL_CODE=$?
echo "$WF_DETAIL_RESPONSE" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    print_result 0 "获取工作流详情成功"
else
    print_result 1 "获取工作流详情失败: $WF_DETAIL_RESPONSE"
fi

# 6. 智能体功能测试
print_info "6. 测试智能体功能"

# 6.1 创建智能体定义
AGENT_DEF_RESPONSE=$(curl -s -X POST "$BASE_URL/agent-definitions" \
    -H "Content-Type: application/json" \
    -H "Authorization: Bearer $TOKEN" \
    -d '{
        "name":"测试智能体",
        "description":"系统测试创建的智能体",
        "type":"CHAT",
        "model":"gpt-3.5-turbo",
        "systemPrompt":"你是一个测试智能体",
        "temperature":0.7,
        "maxTokens":1000
    }')

AGENT_DEF_CODE=$?
AGENT_DEF_ID=$(echo "$AGENT_DEF_RESPONSE" | grep -o '"id":[0-9]*' | cut -d':' -f2)

if [ -n "$AGENT_DEF_ID" ]; then
    print_result 0 "创建智能体定义成功 (ID: $AGENT_DEF_ID)"
else
    print_result 1 "创建智能体定义失败: $AGENT_DEF_RESPONSE"
fi

# 6.2 获取智能体定义列表
print_info "  6.2 获取智能体定义列表"

AGENT_DEF_LIST_RESPONSE=$(curl -s -X GET "$BASE_URL/agent-definitions" \
    -H "Authorization: Bearer $TOKEN")

AGENT_DEF_LIST_CODE=$?
echo "$AGENT_DEF_LIST_RESPONSE" | grep -q '"code":200'
if [ $? -eq 0 ]; then
    print_result 0 "获取智能体定义列表成功"
else
    print_result 1 "获取智能体定义列表失败: $AGENT_DEF_LIST_RESPONSE"
fi

echo "========================================"
echo "系统功能验证完成！"
echo "========================================"
echo "总结:"
echo "  用户认证: ✓ 完成"
echo "  知识库管理: ✓ 完成"
echo "  工作流管理: ✓ 完成"
echo "  智能体管理: ✓ 完成"
echo ""
echo "所有功能模块均已通过测试，系统运行正常！"
echo "========================================"