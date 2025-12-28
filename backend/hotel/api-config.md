# API配置建议

## 1. 添加SpringDoc OpenAPI依赖

在pom.xml中添加：

```xml
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.2.0</version>
</dependency>
```

## 2. API文档访问地址

启动应用后可访问：
- Swagger UI: http://localhost:8080/swagger-ui.html
- API JSON: http://localhost:8080/v3/api-docs

## 3. Controller注解示例

```java
@RestController
@RequestMapping("/api/v1/feedback")
@Tag(name = "口碑量化系统", description = "客户反馈和绩效管理相关接口")
public class CustomerFeedbackController {

    @Operation(summary = "提交客户反馈", description = "接收新的客户反馈并进行情感分析")
    @ApiResponse(responseCode = "201", description = "反馈提交成功")
    @PostMapping
    public ResponseEntity<FeedbackResponse> submitFeedback(
        @Parameter(description = "反馈信息") @RequestBody FeedbackRequest request) {
        // 实现逻辑
    }
}
```

## 4. 前端API调用文件结构

建议在前端创建以下文件结构：

```
frontend/src/
├── api/
│   ├── index.ts          # API入口文件
│   ├── modules/
│   │   ├── reputation.ts # 口碑量化模块API
│   │   ├── personal.ts   # 个性化服务模块API
│   │   └── pricing.ts    # 动态定价模块API
│   └── types/
│       └── api.ts        # API类型定义
```

## 5. 前端API索引文件示例

```typescript
// api/index.ts
export { reputationAPI } from './modules/reputation';
export { personalAPI } from './modules/personal';
export { pricingAPI } from './modules/pricing';

// API模块映射
export const APIModules = {
  REPUTATION: 'reputation',
  PERSONAL: 'personal',
  PRICING: 'pricing'
};
```

## 6. 前端自动生成API文档

可以使用swagger-typescript-api工具自动生成TypeScript API客户端：

```bash
npx swagger-typescript-api -p http://localhost:8080/v3/api-docs -o ./src/api/generated
```