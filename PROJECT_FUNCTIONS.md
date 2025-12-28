# 精品单体酒店智能管理系统 - 功能说明

## 项目结构

### 后端结构 (`backend/hotel/`)
```
src/main/java/com/hotel/hotel/
├── controller/          # API控制器
│   ├── BookingController.java      # 预订管理
│   ├── CustomerController.java     # 客户管理
│   ├── DashboardController.java    # 数据概览
│   ├── DepartmentTaskController.java # 部门任务
│   ├── FeedbackController.java     # 客户反馈
│   ├── OverbookingController.java  # 智能超售
│   ├── PersonalizationController.java # 个性化服务
│   ├── PricingController.java      # 动态定价
│   ├── RLTrainingController.java   # RL训练
│   └── RoomController.java         # 房间管理
├── entity/              # 数据实体
├── repository/          # 数据访问层
├── service/             # 业务逻辑层
└── config/              # 配置类
```

### 前端结构 (`frontend/src/`)
```
views/                  # 页面组件
├── Dashboard.vue       # 数据概览
├── Rooms.vue          # 房态管理
├── Bookings.vue       # 预订管理
├── Customers.vue      # 客户管理
├── Services.vue       # 个性化服务
├── Overbooking.vue    # 智能超售
├── Reputation.vue     # 声誉管理
└── placeholder/       # 占位页面
    ├── CheckIn.vue    # 入住办理
    ├── CheckOut.vue   # 退房办理
    ├── TaskCenter.vue # 任务中心
    ├── Feedback.vue   # 客户反馈
    ├── VIP.vue        # 会员管理
    └── Reports.vue    # 报表分析
```

## 核心功能模块

### 1. 数据概览 (Dashboard)
- **实时统计**: 房间状态、入住率、营收数据
- **今日概览**: 今日入住/退房数量
- **趋势图表**: 入住率趋势、营收走势
- **快捷操作**: 快速跳转各功能模块

**API端点**:
- `GET /api/v1/dashboard/overview?hotelId=1` - 概览数据
- `GET /api/rooms/hotel/1/statistics` - 房间统计
- `GET /api/bookings/hotel/1` - 预订统计

### 2. 房态管理 (Rooms)
- **多视图展示**: 网格视图、楼层视图、列表视图
- **房间状态**: 可用、已入住、清洁中、维护中
- **房间操作**: 查看详情、状态更新、入住办理、退房办理
- **房间添加**: 新增房间，支持完整属性配置
- **筛选过滤**: 按楼层、房型、状态筛选

**API端点**:
- `GET /api/rooms/hotel/{hotelId}` - 获取房间列表
- `POST /api/rooms/hotel/{hotelId}` - 添加新房间
- `PATCH /api/rooms/{roomId}/status` - 更新房间状态
- `GET /api/rooms/room-types/hotel/{hotelId}` - 获取房型列表

### 3. 预订管理 (Bookings)
- **预订列表**: 全部预订记录展示
- **预订状态**: 已确认、进行中、已完成、已取消
- **预订操作**: 查看详情、确认预订、取消预订
- **搜索过滤**: 按日期、状态、客户搜索
- **分页展示**: 支持大量数据分页加载

**API端点**:
- `GET /api/bookings/hotel/{hotelId}` - 获取预订列表
- `GET /api/bookings/{bookingId}` - 获取预订详情
- `PATCH /api/bookings/{bookingId}/status` - 更新预订状态
- `GET /api/bookings/number/{bookingNumber}` - 按预订号查询

### 4. 客户管理 (Customers)
- **客户列表**: 全部客户信息展示
- **客户画像**: 基础信息、预订历史、偏好分析
- **VIP管理**: 会员等级、积分管理、专属服务
- **客户搜索**: 按姓名、手机号、邮箱搜索
- **客户详情**: 完整的客户信息和历史记录

**API端点**:
- `GET /api/customers/hotel/{hotelId}` - 获取客户列表
- `GET /api/customers/{customerId}` - 获取客户详情
- `POST /api/customers` - 新增客户
- `PUT /api/customers/{customerId}` - 更新客户信息

### 5. 个性化服务 (Services)
- **智能推荐**: 基于客户画像的服务推荐
- **任务管理**: 部门任务分配和跟踪
- **客户反馈**: 反馈收集和分析
- **AI分析**: 利用Zhipu AI进行情感分析和内容理解

**API端点**:
- `GET /api/personalization/recommend/{customerId}` - 获取个性化推荐
- `GET /api/department-tasks` - 获取部门任务
- `POST /api/feedback` - 提交客户反馈
- `POST /api/feedback/analyze` - AI情感分析

### 6. 智能超售 (Overbooking)
- **AI决策**: 基于强化学习的超售建议
- **风险评估**: 预测No-show率和潜在损失
- **决策历史**: 超售决策记录和结果分析
- **模型训练**: 在线训练强化学习模型
- **性能监控**: 超售策略效果统计

**API端点**:
- `GET /api/overbooking/recommend` - 获取超售建议
- `POST /api/overbooking/apply` - 应用超售决策
- `GET /api/overbooking/history` - 获取决策历史
- `POST /api/rl-training/train` - 开始模型训练
- `GET /api/overbooking/performance` - 获取性能统计

### 7. 声誉管理 (Reputation)
- **评分监控**: 客户评分趋势分析
- **反馈分析**: 正面/负面反馈分类
- **改进建议**: 基于反馈的改进措施
- **竞争分析**: 行业对比和市场定位

**API端点**:
- `GET /api/reputation/overview` - 声誉概览
- `GET /api/reputation/trends` - 评分趋势
- `GET /api/reputation/feedback` - 反馈分析

## 数据库表结构

### 核心表
- `hotels` - 酒店信息
- `room_types` - 房型配置
- `rooms` - 房间信息
- `customers` - 客户信息
- `bookings` - 预订记录
- `booking_guests` - 入住客人

### 智能功能表
- `overbooking_config` - 超售配置
- `overbooking_decisions` - 超售决策记录
- `q_learning_state` - Q-Learning状态表
- `rl_training_history` - RL训练历史

### 服务管理表
- `department_tasks` - 部门任务
- `customer_feedback` - 客户反馈
- `guest_profiles` - 客户画像
- `pricing_records` - 定价记录

## 开发环境设置

### 后端启动
```bash
cd backend/hotel
mvn spring-boot:run
# 或
java -jar target/hotel-0.0.1-SNAPSHOT.jar
```

### 前端启动
```bash
cd frontend
npm install
npm run dev
```

### 数据库初始化
- 执行 `backend/hotel/src/main/resources/db/migration/` 中的SQL文件
- 默认管理员账号: admin/admin123

## 测试账户
- 前台: front/front123
- 经理: manager/manager123
- 管理员: admin/admin123

## 注意事项

1. **API版本**: 所有API都有 `/api` 前缀，部分使用 `/api/v1`
2. **跨域**: 已配置CORS支持前端开发服务器
3. **分页**: 列表API支持 `page` 和 `size` 参数
4. **状态枚举**: 房间状态、预订状态等使用英文枚举值
5. **时间格式**: ISO 8601格式 (`2024-01-01T00:00:00Z`)

## 待完成功能

- [ ] 房态日历可视化
- [ ] 团队预订管理
- [ ] 动态定价策略
- [ ] 完整的报表系统
- [ ] 系统设置管理
- [ ] 权限角色管理

## 联系方式

如有问题请联系开发团队或查看相关文档。
