# 立即测试

## 问题已修复
✅ 修复了JSON解析问题 - 智谱AI返回的 ```json 代码块现在可以正确提取

## 测试步骤

### 1. 重启应用
```bash
cd backend/hotel
./mvnw.cmd spring-boot:run
```

### 2. 测试英文请求
```bash
curl -X POST "http://localhost:8082/api/v1/personalization/request" \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d '{"customerId":"TEST001","requestContent":"air conditioning is broken","roomNumber":"801","hotelId":1}'
```

### 3. 测试中文请求
```bash
curl -X POST "http://localhost:8082/api/v1/personalization/request" \
  -H "Content-Type: application/json; charset=UTF-8" \
  -d '{"customerId":"TEST002","requestContent":"我滴空调怎么坏掉啦瓦","roomNumber":"801","hotelId":1}'
```

## 预期结果
- 英文请求应该正常解析，分配给工程部
- 中文请求也应该正常解析，分配给工程部
- 任务应该成功保存到数据库
- 日志显示："成功解析NLP结果: intent=MAINTENANCE, description=..., dept=工程部"

## 验证数据库
```sql
SELECT * FROM task_order ORDER BY task_id DESC LIMIT 5;
```