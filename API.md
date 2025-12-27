# 酒店管理系统 API 文档

**基础地址**: `http://localhost:8082`

---

## 目录

1. [房间管理](#房间管理)
2. [预订管理](#预订管理)
3. [数据模型](#数据模型)

---

## 房间管理

### 1. 获取酒店所有房间

**请求**
```
GET /api/rooms/hotel/{hotelId}
```

**路径参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| hotelId | Long | 是 | 酒店ID |

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 房间状态: `available`, `occupied`, `maintenance`, `cleaning` |

**响应示例**
```json
[
  {
    "id": 16,
    "hotelId": 1,
    "roomTypeId": 1,
    "roomNumber": "A101",
    "floor": 1,
    "status": "available",
    "hasAc": true,
    "hasTv": true,
    "hasWifi": true,
    "hasBalcony": false,
    "hasKitchen": false,
    "parkingSpaces": 0,
    "typeName": "标准间",
    "typeCode": "A",
    "basePrice": 500.00,
    "facilities": ["WiFi", "空调", "电视", "淋浴"]
  }
]
```

---

### 2. 获取房间详情

**请求**
```
GET /api/rooms/{roomId}
```

**响应示例**
```json
{
  "id": 16,
  "roomNumber": "A101",
  "floor": 1,
  "status": "available",
  "typeName": "标准间",
  "basePrice": 500.00
}
```

---

### 3. 查询可用房间

**请求**
```
GET /api/rooms/available
```

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| roomTypeId | Long | 是 | 房型ID |
| checkInDate | Date | 是 | 入住日期 (yyyy-MM-dd) |
| checkOutDate | Date | 是 | 退房日期 (yyyy-MM-dd) |

**示例**
```
GET /api/rooms/available?roomTypeId=1&checkInDate=2025-12-28&checkOutDate=2025-12-30
```

---

### 4. 更新房间状态

**请求**
```
PATCH /api/rooms/{roomId}/status
Content-Type: application/json
```

**请求体**
```json
{
  "status": "occupied"
}
```

**状态值**: `available`, `occupied`, `maintenance`, `cleaning`

---

### 5. 获取房间统计

**请求**
```
GET /api/rooms/hotel/{hotelId}/statistics
```

**响应示例**
```json
{
  "total": 10,
  "available": 5,
  "occupied": 3,
  "maintenance": 1,
  "cleaning": 1
}
```

---

### 6. 按房型获取房间

**请求**
```
GET /api/rooms/hotel/{hotelId}/type/{roomTypeId}
```

---

## 预订管理

### 1. 创建预订

**请求**
```
POST /api/bookings
Content-Type: application/json
```

**请求体**
```json
{
  "hotelId": 1,
  "customerId": 1,
  "roomTypeId": 1,
  "checkInDate": "2025-12-28",
  "checkOutDate": "2025-12-30",
  "adults": 2,
  "children": 0,
  "babies": 0,
  "requiredCarParkingSpaces": 0,
  "mealType": "bb",
  "marketSegment": "Online TA",
  "distributionChannel": "TA/TO",
  "depositType": "no_deposit",
  "requestsText": "需要高楼层"
}
```

**字段说明**
| 字段 | 类型 | 必填 | 默认值 | 说明 |
|------|------|------|--------|------|
| hotelId | Long | 是 | - | 酒店ID |
| customerId | Long | 是 | - | 客户ID |
| roomTypeId | Long | 是 | - | 房型ID |
| checkInDate | Date | 是 | - | 入住日期 |
| checkOutDate | Date | 是 | - | 退房日期 |
| adults | Integer | 是(至少1) | 1 | 成人数量 |
| children | Integer | 否 | 0 | 儿童数量 |
| babies | Integer | 否 | 0 | 婴儿数量 |
| requiredCarParkingSpaces | Integer | 否 | 0 | 停车位数量 |
| mealType | Enum | 否 | bb | 餐型: `bb`, `hb`, `fb`, `sc` |
| depositType | Enum | 否 | no_deposit | 押金类型: `no_deposit`, `non_refund`, `refundable` |

**响应示例**
```json
{
  "id": 1,
  "bookingNumber": "BK17668106723482F19",
  "hotelId": 1,
  "customerId": 1,
  "roomTypeId": 1,
  "checkInDate": "2025-12-28",
  "checkOutDate": "2025-12-30",
  "totalNights": 2,
  "adults": 2,
  "totalPrice": 1000.00,
  "adr": 500.00,
  "status": "confirmed",
  "customerName": "张三",
  "typeName": "标准间"
}
```

---

### 2. 获取酒店所有预订

**请求**
```
GET /api/bookings/hotel/{hotelId}
```

**查询参数**
| 参数 | 类型 | 必填 | 说明 |
|------|------|------|------|
| status | String | 否 | 预订状态: `pending`, `confirmed`, `checked_in`, `checked_out`, `canceled`, `no_show` |

---

### 3. 获取预订详情

**请求**
```
GET /api/bookings/{bookingId}
```

或按预订号查询:
```
GET /api/bookings/number/{bookingNumber}
```

---

### 4. 取消预订

**请求**
```
PATCH /api/bookings/{bookingId}/cancel
```

**响应示例**
```json
{
  "id": 1,
  "status": "canceled",
  "isCanceled": true,
  "cancelDate": "2025-12-27T13:03:50"
}
```

---

### 5. 获取今日入住/退房

**请求**
```
GET /api/bookings/hotel/{hotelId}/today/checkins
GET /api/bookings/hotel/{hotelId}/today/checkouts
```

---

### 6. 获取客户预订列表

**请求**
```
GET /api/bookings/customer/{customerId}
```

---

## 数据模型

### 预订状态 (BookingStatus)

| 状态 | 说明 |
|------|------|
| `pending` | 待确认 |
| `confirmed` | 已确认 |
| `checked_in` | 已入住 |
| `checked_out` | 已退房 |
| `canceled` | 已取消 |
| `no_show` | 未到 |

### 房间状态 (RoomStatus)

| 状态 | 说明 |
|------|------|
| `available` | 可用 |
| `occupied` | 已入住 |
| `maintenance` | 维护中 |
| `cleaning` | 清洁中 |

### 餐型 (MealType)

| 类型 | 说明 |
|------|------|
| `bb` | 含早 (Bed & Breakfast) |
| `hb` | 含早晚餐 (Half Board) |
| `fb` | 全含 (Full Board) |
| `sc` | 无餐 (Self Catering) |

### 押金类型 (DepositType)

| 类型 | 说明 |
|------|------|
| `no_deposit` | 无押金 |
| `non_refund` | 不可退 |
| `refundable` | 可退 |

### VIP等级 (VipLevel)

| 等级 | 说明 |
|------|------|
| `normal` | 普通 |
| `silver` | 银卡 |
| `gold` | 金卡 |
| `platinum` | 白金卡 |

---

## 错误响应

**400 Bad Request**
```json
{
  "timestamp": "2025-12-27T04:44:32.351",
  "status": 400,
  "error": "Bad Request",
  "message": "入住人数超过房型最大容量"
}
```

**404 Not Found**
```json
{
  "timestamp": "2025-12-27T04:44:32.351",
  "status": 404,
  "error": "Not Found"
}
```

**500 Internal Server Error**
```json
{
  "timestamp": "2025-12-27T04:44:32.351",
  "status": 500,
  "error": "Internal Server Error"
}
```
