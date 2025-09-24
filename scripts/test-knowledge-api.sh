#!/bin/bash

echo "=== 知识库功能 API 测试脚本 ==="

# 配置
BASE_URL="http://localhost:8080/api"
TOKEN=""  # 需要先登录获取 token

# 颜色输出
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 检查 token
if [ -z "$TOKEN" ]; then
    echo -e "${RED}请先设置 TOKEN 变量${NC}"
    echo "1. 先调用登录接口获取 token"
    echo "2. 设置 TOKEN 变量: export TOKEN='your-jwt-token'"
    exit 1
fi

# 通用请求函数
make_request() {
    local method=$1
    local url=$2
    local data=$3
    local content_type=${4:-"application/json"}
    
    echo -e "${YELLOW}请求: $method $url${NC}"
    
    if [ -n "$data" ]; then
        curl -s -X $method "$BASE_URL$url" \
            -H "Authorization: Bearer $TOKEN" \
            -H "Content-Type: $content_type" \
            -d "$data" | jq '.'
    else
        curl -s -X $method "$BASE_URL$url" \
            -H "Authorization: Bearer $TOKEN" | jq '.'
    fi
    
    echo ""
}

# 1. 创建知识库
echo -e "${GREEN}=== 1. 创建知识库 ===${NC}"
KB_DATA='{
    "name": "测试知识库",
    "description": "用于API测试的知识库",
    "embeddingModel": "text-embedding-ada-002",
    "chunkSize": 500,
    "chunkOverlap": 50
}'

KB_RESPONSE=$(make_request "POST" "/knowledge-bases" "$KB_DATA")
KB_ID=$(echo "$KB_RESPONSE" | jq -r '.data.id // empty')

if [ -z "$KB_ID" ] || [ "$KB_ID" = "null" ]; then
    echo -e "${RED}创建知识库失败${NC}"
    exit 1
fi

echo -e "${GREEN}知识库创建成功，ID: $KB_ID${NC}"

# 2. 获取知识库列表
echo -e "${GREEN}=== 2. 获取知识库列表 ===${NC}"
make_request "GET" "/knowledge-bases"

# 3. 获取知识库详情
echo -e "${GREEN}=== 3. 获取知识库详情 ===${NC}"
make_request "GET" "/knowledge-bases/$KB_ID"

# 4. 更新知识库
echo -e "${GREEN}=== 4. 更新知识库 ===${NC}"
UPDATE_DATA='{
    "name": "更新后的测试知识库",
    "description": "已更新的描述",
    "embeddingModel": "text-embedding-ada-002",
    "chunkSize": 600,
    "chunkOverlap": 60
}'
make_request "PUT" "/knowledge-bases/$KB_ID" "$UPDATE_DATA"

# 5. 创建测试文档（模拟上传）
echo -e "${GREEN}=== 5. 模拟文档上传 ===${NC}"
echo "注意：实际文档上传需要使用 multipart/form-data 格式"
echo "可以使用以下命令测试文档上传："
echo "curl -X POST $BASE_URL/knowledge-bases/$KB_ID/documents/upload \\"
echo "  -H \"Authorization: Bearer \$TOKEN\" \\"
echo "  -F \"file=@test.pdf\""

# 6. 获取文档列表
echo -e "${GREEN}=== 6. 获取文档列表 ===${NC}"
make_request "GET" "/knowledge-bases/$KB_ID/documents"

# 7. 测试搜索功能
echo -e "${GREEN}=== 7. 测试语义搜索 ===${NC}"
SEARCH_DATA='{
    "knowledgeBaseId": '$KB_ID',
    "query": "人工智能的发展历程",
    "topK": 5,
    "threshold": 0.7,
    "includeMetadata": true
}'
make_request "POST" "/search/semantic" "$SEARCH_DATA"

# 8. 测试混合搜索
echo -e "${GREEN}=== 8. 测试混合搜索 ===${NC}"
make_request "POST" "/search/hybrid" "$SEARCH_DATA"

# 9. 获取 RAG 上下文
echo -e "${GREEN}=== 9. 获取 RAG 上下文 ===${NC}"
make_request "GET" "/search/context?knowledgeBaseId=$KB_ID&query=人工智能&maxTokens=1000"

# 10. 搜索知识库
echo -e "${GREEN}=== 10. 搜索知识库 ===${NC}"
make_request "GET" "/knowledge-bases/search?keyword=测试"

# 11. 获取活跃知识库
echo -e "${GREEN}=== 11. 获取活跃知识库 ===${NC}"
make_request "GET" "/knowledge-bases/active"

# 清理：删除测试知识库
echo -e "${GREEN}=== 清理：删除测试知识库 ===${NC}"
read -p "是否删除测试知识库? (y/N): " -n 1 -r
echo
if [[ $REPLY =~ ^[Yy]$ ]]; then
    make_request "DELETE" "/knowledge-bases/$KB_ID"
    echo -e "${GREEN}测试知识库已删除${NC}"
else
    echo -e "${YELLOW}保留测试知识库，ID: $KB_ID${NC}"
fi

echo -e "${GREEN}=== API 测试完成 ===${NC}"