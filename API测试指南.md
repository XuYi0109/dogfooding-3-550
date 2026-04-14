# E-commerce Backend API 测试指南

## 启动应用

```bash
# 使用Maven启动
mvn spring-boot:run

# 或打包后运行
mvn clean package
java -jar target/ecommerce-backend-1.0.0.jar
```

## 访问地址

- **应用地址**: http://localhost:8080/api
- **Swagger文档**: http://localhost:8080/api/swagger-ui.html
- **H2数据库控制台**: http://localhost:8080/api/h2-console
  - JDBC URL: `jdbc:h2:mem:testdb`
  - 用户名: `sa`
  - 密码: `password`

## 默认测试账号

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| testuser | user123 | 普通用户 |

---

## 1. 用户认证接口测试

### 1.1 用户注册

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "newuser",
    "email": "newuser@example.com",
    "password": "password123",
    "firstName": "New",
    "lastName": "User"
  }'
```

**预期响应**:
```json
{
  "success": true,
  "message": "User registered successfully",
  "code": "SUCCESS",
  "data": {
    "id": 3,
    "username": "newuser",
    "email": "newuser@example.com",
    "firstName": "New",
    "lastName": "User",
    "role": "ROLE_USER"
  }
}
```

### 1.2 用户登录

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "usernameOrEmail": "admin",
    "password": "admin123"
  }'
```

**预期响应**:
```json
{
  "success": true,
  "message": "Login successful",
  "code": "SUCCESS",
  "data": {
    "token": "eyJhbGciOiJIUzI1NiJ9...",
    "type": "Bearer",
    "username": "admin"
  }
}
```

> 保存返回的token，后续接口需要使用:
> ```bash
> export TOKEN="Bearer eyJhbGciOiJIUzI1NiJ9..."
> ```

### 1.3 获取当前用户信息

```bash
curl -X GET http://localhost:8080/api/auth/me \
  -H "Authorization: $TOKEN"
```

---

## 2. 商品接口测试

### 2.1 获取商品列表（无需登录）

```bash
# 获取第一页，每页10条
curl "http://localhost:8080/api/products?page=0&size=10"

# 按价格排序
curl "http://localhost:8080/api/products?page=0&size=10&sortBy=price&direction=asc"
```

### 2.2 搜索商品

```bash
# 按名称搜索
curl "http://localhost:8080/api/products/search?name=iPhone"

# 按分类筛选
curl "http://localhost:8080/api/products/search?category=ELECTRONICS"

# 价格区间筛选
curl "http://localhost:8080/api/products/search?minPrice=100&maxPrice=1000"
```

### 2.3 获取单个商品详情

```bash
curl http://localhost:8080/api/products/1
```

### 2.4 创建商品（管理员权限）

```bash
curl -X POST http://localhost:8080/api/products \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "AirPods Pro 2",
    "description": "苹果无线降噪耳机",
    "price": 1899.00,
    "stockQuantity": 100,
    "category": "ELECTRONICS",
    "imageUrl": "https://example.com/airpods.jpg"
  }'
```

### 2.5 更新商品（管理员权限）

```bash
curl -X PUT http://localhost:8080/api/products/1 \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPhone 15 Pro Max",
    "description": "更新后的描述",
    "price": 8999.00,
    "stockQuantity": 50,
    "category": "ELECTRONICS"
  }'
```

### 2.6 更新库存（管理员权限）

```bash
curl -X PATCH "http://localhost:8080/api/products/1/stock?quantity=75" \
  -H "Authorization: $TOKEN"
```

---

## 3. 订单接口测试

### 3.1 创建订单

```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "items": [
      {"productId": 1, "quantity": 1},
      {"productId": 3, "quantity": 2}
    ],
    "shippingAddress": {
      "recipientName": "张三",
      "addressLine1": "北京市朝阳区建国路88号",
      "city": "北京",
      "state": "北京市",
      "postalCode": "100022",
      "country": "中国",
      "phoneNumber": "13800138000"
    }
  }'
```

### 3.2 获取我的订单

```bash
curl "http://localhost:8080/api/orders/my-orders?page=0&size=10" \
  -H "Authorization: $TOKEN"
```

### 3.3 获取订单详情

```bash
curl http://localhost:8080/api/orders/1 \
  -H "Authorization: $TOKEN"
```

### 3.4 更新订单状态

```bash
curl -X PATCH "http://localhost:8080/api/orders/1/status?status=CANCELLED" \
  -H "Authorization: $TOKEN"
```

### 3.5 获取所有订单（管理员权限）

```bash
curl "http://localhost:8080/api/orders?page=0&size=10" \
  -H "Authorization: $TOKEN"
```

---

## 4. 测试场景示例

### 场景1：完整购物流程

1. 用户登录获取token
2. 浏览商品列表
3. 查看商品详情
4. 创建订单
5. 查看订单状态

### 场景2：管理员商品管理

1. 管理员登录
2. 创建新商品
3. 更新商品信息
4. 调整库存
5. 查看所有订单

---

## 5. 常见错误测试

### 5.1 用户名已存在

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","email":"test@test.com","password":"123456","firstName":"Test","lastName":"User"}'
```

**预期**: 返回 `USERNAME_EXISTS` 错误

### 5.2 登录密码错误

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"usernameOrEmail":"admin","password":"wrongpassword"}'
```

**预期**: 返回 `INVALID_CREDENTIALS` 错误

### 5.3 库存不足

创建订单时商品数量超过库存，预期返回 `INSUFFICIENT_STOCK` 错误

### 5.4 未授权访问

```bash
curl http://localhost:8080/api/orders/my-orders
```

**预期**: 返回401未授权

---

## 6. 使用Swagger UI测试

访问 http://localhost:8080/api/swagger-ui.html

1. 点击 `Authorize` 按钮
2. 输入: `Bearer {你的token}`
3. 点击 `Authorize` 完成认证
4. 展开任意API，点击 `Try it out`
5. 填写参数，点击 `Execute` 执行

---

## 7. 验证数据库

访问H2控制台，执行以下SQL验证数据:

```sql
-- 查看用户
SELECT * FROM USERS;

-- 查看商品
SELECT * FROM PRODUCTS;

-- 查看订单
SELECT * FROM ORDERS;
SELECT * FROM ORDER_ITEMS;
```