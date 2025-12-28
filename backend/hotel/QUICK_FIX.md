# 快速修复方案

## 已发现的问题
1. ✅ NLP解析逻辑已修复 - 手动JSON解析
2. ❌ API Key可能不完整
3. ❌ 中文字符编码问题

## 立即执行步骤

### 1. 检查API Key
确认智谱AI API Key是否完整，如果不完整需要更新

### 2. 启动应用并检查日志
```bash
cd backend/hotel
./mvnw.cmd spring-boot:run
```

### 3. 先测试英文请求（排除编码问题）
```bash
curl -X POST "http://localhost:8082/api/v1/personalization/request" \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d '{"customerId":"TEST001","requestContent":"air conditioning is broken","roomNumber":"801","hotelId":1}'
```

### 4. 查看日志输出
- NLP解析结果
- 是否有API调用错误
- 是否有JSON解析错误

### 5. 如果英文请求成功，再测试中文

## 预期结果
- 英文请求应该能正常处理（即使AI返回UNKNOWN）
- 日志应显示完整的NLP解析过程
- 任务应该能保存到数据库