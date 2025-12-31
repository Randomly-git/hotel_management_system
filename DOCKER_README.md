# 🐳 酒店管理系统Docker部署指南

## 📋 概述

本项目支持完整的Docker容器化部署，包含前端、后端和数据库服务。

## 🏗️ 项目结构

```
hotel-management-system/
├── Dockerfile.frontend      # 前端Dockerfile
├── Dockerfile.backend       # 后端Dockerfile
├── docker-compose.yml       # Docker Compose配置
├── nginx.conf              # Nginx配置文件
├── .dockerignore           # Docker忽略文件
├── deploy-to-tencent.sh    # 腾讯云部署脚本
└── backend/hotel/src/main/resources/application-docker.yml
```

## 🚀 本地开发环境

### 1. 环境要求

- Docker Desktop
- Docker Compose
- 至少4GB可用内存

### 2. 启动服务

```bash
# 克隆项目
git clone <repository-url>
cd hotel-management-system

# 构建并启动所有服务
docker-compose up --build

# 或者后台运行
docker-compose up -d --build
```

### 3. 服务访问

- **前端**: http://localhost
- **后端API**: http://localhost/api
- **数据库**: localhost:3306 (hotel_user/hotel_password)

### 4. 查看日志

```bash
# 查看所有服务日志
docker-compose logs -f

# 查看特定服务日志
docker-compose logs -f backend
docker-compose logs -f frontend
docker-compose logs -f mysql
```

### 5. 停止服务

```bash
# 停止所有服务
docker-compose down

# 停止并删除数据卷
docker-compose down -v
```

## 🔧 开发模式

### 前端开发

```bash
# 进入前端容器
docker-compose exec frontend sh

# 安装新依赖
npm install <package-name>

# 重启前端服务
docker-compose restart frontend
```

### 后端开发

```bash
# 进入后端容器
docker-compose exec backend sh

# 查看应用日志
tail -f /app/logs/application.log

# 重启后端服务
docker-compose restart backend
```

## 📦 腾讯云部署

### 1. 服务器准备

#### 服务器要求
- Ubuntu 20.04+ 或 CentOS 7+
- 至少2GB内存，推荐4GB
- 开放端口：80, 443 (可选)
- 至少20GB存储空间

#### 初始化服务器
```bash
# 更新系统
sudo apt update && sudo apt upgrade -y

# 安装必要工具
sudo apt install -y curl wget git vim htop

# 配置防火墙
sudo ufw allow 22/tcp
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw --force enable
```

### 2. Docker环境配置

#### 安装Docker和Docker Compose
```bash
# 安装Docker
curl -fsSL https://get.docker.com | sh

# 启动Docker服务
sudo systemctl start docker
sudo systemctl enable docker

# 安装Docker Compose
sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# 验证安装
docker --version
docker-compose --version
```

#### 配置腾讯云镜像加速器
```bash
# 创建Docker配置文件
sudo mkdir -p /etc/docker

# 配置镜像加速器（替换为你的腾讯云加速器地址）
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com"
  ],
  "log-driver": "json-file",
  "log-opts": {
    "max-size": "100m",
    "max-file": "3"
  }
}
EOF

# 重启Docker服务
sudo systemctl restart docker
```

### 3. 项目部署

#### 方法一：自动部署（推荐）
```bash
# 下载项目
git clone <your-repository-url>
cd hotel-management-system

# 修改部署脚本中的服务器信息
vim deploy-to-tencent.sh
# 将 TENCENT_CLOUD_IP 修改为你的服务器IP
# 将 SSH_USER 修改为你的用户名

# 运行部署脚本
chmod +x deploy-to-tencent.sh
./deploy-to-tencent.sh
```

#### 方法二：手动部署
```bash
# 1. 下载项目到服务器
git clone <your-repository-url>
cd hotel-management-system

# 2. 构建镜像（可能需要时间）
docker-compose build

# 3. 启动服务
docker-compose up -d

# 4. 检查服务状态
docker-compose ps
docker-compose logs -f
```

### 4. 域名和SSL配置

#### 配置域名
```bash
# 在腾讯云控制台添加域名解析
# 类型：A记录
# 主机记录：@ 或 www
# 记录值：你的服务器公网IP
```

#### 自动SSL证书（推荐）
```bash
# 安装Certbot
sudo apt install -y certbot python3-certbot-nginx

# 获取SSL证书（将your-domain.com替换为你的域名）
sudo certbot --nginx -d your-domain.com -d www.your-domain.com

# 设置自动续期
sudo crontab -e
# 添加以下行：
# 0 12 * * * /usr/bin/certbot renew --quiet
```

#### 手动SSL配置
```bash
# 如果你已经有SSL证书，将证书文件上传到服务器
# 然后修改nginx.conf配置SSL
```

### 5. 生产环境优化

#### 修改默认密码
```bash
# 编辑docker-compose.yml
vim docker-compose.yml

# 修改数据库密码
environment:
  MYSQL_ROOT_PASSWORD: your-secure-root-password
  MYSQL_PASSWORD: your-secure-user-password
```

#### 配置数据持久化
```bash
# 创建数据目录
sudo mkdir -p /data/mysql
sudo mkdir -p /data/logs

# 修改docker-compose.yml挂载路径
volumes:
  - /data/mysql:/var/lib/mysql
  - /data/logs:/app/logs
```

#### 设置自动备份
```bash
# 创建备份脚本
sudo tee /usr/local/bin/backup-db.sh <<-'EOF'
#!/bin/bash
BACKUP_DIR="/data/backup"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="hotel_management_system"
DB_USER="hotel_user"
DB_PASS="your-password"

mkdir -p $BACKUP_DIR

docker-compose exec -T mysql mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# 只保留最近7天的备份
find $BACKUP_DIR -name "backup_*.sql" -mtime +7 -delete

echo "Backup completed: $BACKUP_DIR/backup_$DATE.sql"
EOF

# 设置可执行权限
sudo chmod +x /usr/local/bin/backup-db.sh

# 添加定时任务（每天凌晨2点备份）
sudo crontab -e
# 添加：0 2 * * * /usr/local/bin/backup-db.sh
```

### 6. 监控和维护

#### 查看服务状态
```bash
# 检查所有服务
docker-compose ps

# 查看资源使用
docker stats

# 查看日志
docker-compose logs -f backend
docker-compose logs -f mysql
```

#### 更新部署
```bash
# 拉取最新代码
git pull origin main

# 重新构建（如果有代码变更）
docker-compose build --no-cache

# 滚动更新
docker-compose up -d

# 清理无用镜像
docker image prune -f
```

### 7. 常见问题

#### Docker镜像拉取慢或失败
```bash
# 检查网络连接
ping mirror.ccs.tencentyun.com

# 重试构建
docker-compose build --no-cache
```

#### 服务启动失败
```bash
# 查看详细错误日志
docker-compose logs

# 检查端口冲突
sudo netstat -tlnp | grep -E ':(80|3306|8080)'

# 检查磁盘空间
df -h
```

#### 数据库连接失败
```bash
# 检查MySQL容器状态
docker-compose ps mysql

# 进入MySQL容器检查
docker-compose exec mysql mysql -u hotel_user -p -e "SELECT 1;"

# 重置数据库
docker-compose down -v
docker-compose up -d mysql
```

## 🔍 故障排除

### Docker镜像拉取失败 (403 Forbidden)

如果遇到Docker Hub连接问题：

```bash
# 1. 检查网络连接
ping docker.io

# 2. 配置Docker镜像加速器（中国大陆）
# 创建或编辑 /etc/docker/daemon.json
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://registry.docker-cn.com",
    "https://docker.mirrors.ustc.edu.cn",
    "https://hub-mirror.c.163.com"
  ]
}
EOF

# 重启Docker服务
sudo systemctl restart docker

# 3. 或者使用腾讯云镜像加速器
# 登录腾讯云容器镜像服务，获取加速器地址
```

### 服务启动失败

```bash
# 检查服务状态
docker-compose ps

# 查看详细日志
docker-compose logs backend
docker-compose logs mysql

# 检查端口占用
netstat -tlnp | grep -E ':(80|3306|8080)'
```

### 数据库连接问题

```bash
# 检查数据库是否启动
docker-compose exec mysql mysql -u hotel_user -p hotel_management_system -e "SELECT 1;"

# 重置数据库
docker-compose down -v
docker-compose up -d mysql
```

### 前端访问异常

```bash
# 检查前端构建
docker-compose exec frontend ls -la /usr/share/nginx/html

# 检查Nginx配置
docker-compose exec frontend nginx -t

# 检查前端日志
docker-compose logs frontend
```

### Node.js版本问题

```bash
# 如果遇到Node.js版本错误
# 检查当前Node版本
docker-compose exec frontend node --version

# 重新构建前端镜像
docker-compose build --no-cache frontend
```

### 内存不足

```bash
# 增加Docker内存分配
# Docker Desktop -> Settings -> Resources -> Memory

# 或者在docker-compose.yml中限制内存使用
services:
  backend:
    deploy:
      resources:
        limits:
          memory: 1G
        reservations:
          memory: 512M
```

## 📊 监控和维护

### 查看资源使用

```bash
# Docker统计信息
docker stats

# 磁盘使用
docker system df
```

### 备份数据

```bash
# 备份数据库
docker-compose exec mysql mysqldump -u hotel_user -p hotel_management_system > backup.sql

# 备份配置文件
docker-compose exec backend cp /app/application.yml /backup/
```

### 更新部署

```bash
# 重新构建镜像
docker-compose build --no-cache

# 滚动更新
docker-compose up -d

# 查看更新状态
docker-compose ps
```

## 🔐 安全配置

### 生产环境建议

1. **修改默认密码**
   ```yaml
   # docker-compose.yml
   environment:
     MYSQL_ROOT_PASSWORD: your-secure-root-password
     MYSQL_PASSWORD: your-secure-user-password
   ```

2. **启用SSL**
   ```nginx
   # nginx.conf
   listen 443 ssl;
   ssl_certificate /path/to/cert.pem;
   ssl_certificate_key /path/to/key.pem;
   ```

3. **限制数据库访问**
   ```yaml
   mysql:
     ports:
       # 生产环境建议移除外部端口映射
   ```

4. **设置防火墙**
   ```bash
   sudo ufw allow 80
   sudo ufw allow 443
   sudo ufw --force enable
   ```

## 📞 技术支持

如果遇到问题，请检查：

1. **Docker版本**: `docker --version`
2. **Compose版本**: `docker-compose --version`
3. **系统资源**: `docker system info`
4. **服务日志**: `docker-compose logs`

---

## 🎉 部署成功！

部署完成后，你可以通过以下地址访问：

- **前端应用**: http://localhost (本地) 或 http://your-domain.com (生产)
- **API文档**: http://localhost/api (本地) 或 http://your-domain.com/api (生产)
- **健康检查**: http://localhost/health

祝你部署顺利！ 🚀
