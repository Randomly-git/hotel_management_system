# dueTime功能测试指南

## 📋 功能说明

客户现在可以在提交请求时指定期望的解决时间，支持以下特性：

1. **可选字段** - dueTime为可选参数，不提供时使用默认30分钟
2. **ISO格式** - 支持ISO 8601格式的时间字符串
3. **容错处理** - 格式错误时自动回退到默认时间
4. **时间验证** - 系统会记录客户期望的解决时间

## 🧪 测试用例

### 测试1：指定dueTime（正常情况）

```bash
curl -X POST "http://localhost:8082/api/v1/personalization/request" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "VIP001",
    "requestContent": "需要更换床单被罩",
    "roomNumber": "801",
    "hotelId": 1,
    "dueTime": "2025-12-17T18:00:00"
  }'
```

**预期结果：**
- 任务创建成功
- dueTime字段设置为 "2025-12-17T18:00:00"
- 返回任务信息包含正确的截止时间

### 测试2：不指定dueTime（使用默认值）

```bash
curl -X POST "http://localhost:8082/api/v1/personalization/request" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "VIP002",
    "requestContent": "房间需要打扫",
    "roomNumber": "802",
    "hotelId": 1
  }'
```

**预期结果：**
- 任务创建成功
- dueTime自动设置为当前时间+30分钟
- 返回默认的截止时间

### 测试3：错误的dueTime格式（容错处理）

```bash
curl -X POST "http://localhost:8082/api/v1/personalization/request" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "VIP003",
    "requestContent": "空调坏了需要维修",
    "roomNumber": "803",
    "hotelId": 1,
    "dueTime": "2025-12-17 18:00:00"  // 错误格式（缺少T分隔符）
  }'
```

**预期结果：**
- 任务创建成功
- dueTime格式错误，系统使用默认值（当前时间+30分钟）
- 日志中出现警告信息

### 测试4：过去的时间（边界测试）

```bash
curl -X POST "http://localhost:8082/api/v1/personalization/request" \
  -H "Content-Type: application/json" \
  -d '{
    "customerId": "VIP004",
    "requestContent": "紧急！浴室漏水",
    "roomNumber": "804",
    "hotelId": 1,
    "dueTime": "2025-12-16T10:00:00"  // 过去的时间
  }'
```

**预期结果：**
- 任务创建成功
- dueTime设置为客户指定的过去时间
- 系统会标记为紧急任务（需要后续优化）

## 📊 API响应格式

### 成功响应示例

```json
{
  "code": 201,
  "message": "客户请求已处理",
  "data": {
    "taskId": 22,
    "assignedTo": "房务部",
    "nlpAnalysis": {
      "intent": "ROOM_SERVICE",
      "description": "需要更换床单被罩",
      "recommendedDepartment": "房务部",
      "urgency": "MEDIUM"
    },
    "dueTime": "2025-12-17T18:00:00"
  },
  "timestamp": 1702678800000
}
```

## 📈 前端使用建议

### JavaScript示例

```javascript
// 获取当前时间并添加1小时
function formatDueTime(hoursFromNow = 1) {
    const now = new Date();
    now.setHours(now.getHours() + hoursFromNow);
    return now.toISOString();
}

// 提交请求
async function submitCustomerRequest() {
    const requestData = {
        customerId: "VIP001",
        requestContent: "需要额外毛巾",
        roomNumber: "801",
        hotelId: 1,
        dueTime: formatDueTime(2) // 2小时后
    };

    try {
        const response = await fetch('/api/v1/personalization/request', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(requestData)
        });

        const result = await response.json();
        console.log('任务创建成功:', result);
    } catch (error) {
        console.error('请求失败:', error);
    }
}
```

### 时间选择器组件

```html
<!-- HTML时间选择器 -->
<label for="dueTime">期望解决时间：</label>
<input type="datetime-local" id="dueTime" name="dueTime">
```

```javascript
// 转换为ISO格式
function getDateTimeLocalValue() {
    const input = document.getElementById('dueTime');
    const value = input.value; // 格式: "2025-12-17T18:00"
    return value ? value + ":00" : null; // 添加秒数
}
```

## 🔍 数据库验证

```sql
-- 查看任务表中的dueTime字段
SELECT
    task_id,
    guest_member_id,
    task_content,
    status,
    create_time,
    due_time,
    TIMESTAMPDIFF(MINUTE, create_time, due_time) as minutes_until_due
FROM task_order
WHERE hotel_id = 1
ORDER BY create_time DESC;

-- 查找即将超时的任务
SELECT
    task_id,
    task_content,
    due_time,
    TIMESTAMPDIFF(MINUTE, NOW(), due_time) as minutes_remaining
FROM task_order
WHERE hotel_id = 1
  AND status = 'PENDING'
  AND due_time > NOW()
ORDER BY due_time ASC;
```

## ⚠️ 注意事项

1. **时间格式** - 必须使用ISO 8601格式 (YYYY-MM-DDTHH:mm:ss)
2. **时区处理** - 建议统一使用UTC时间或服务器本地时间
3. **业务规则** - 客户指定的时间可能需要业务规则验证（如不能设置过去时间）
4. **性能考虑** - 大量任务时需要due_time索引优化

## 🚀 扩展功能建议

1. **时间验证** - 防止设置过去时间
2. **紧急标记** - 根据dueTime自动标记紧急任务
3. **超时提醒** - 临近dueTime时自动提醒
4. **批量调整** - 管理员批量调整任务dueTime