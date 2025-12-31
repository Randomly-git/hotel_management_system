# 🚀 腾讯云部署检查清单

## 📋 部署前准备

### 1. 腾讯云服务器配置
- [ ] 已购买腾讯云服务器（推荐配置：2核4GB以上）
- [ ] 已获取服务器公网IP地址
- [ ] 已配置安全组规则：
  - [ ] 开放22端口（SSH）
  - [ ] 开放80端口（HTTP）
  - [ ] 开放443端口（HTTPS，可选）
- [ ] 已设置SSH密钥登录（推荐）或密码登录

### 2. 本地环境准备
- [ ] 已安装Docker Desktop
- [ ] 已安装Docker Compose
- [ ] 已安装Git
- [ ] 项目代码已下载到本地

### 3. 域名配置（可选）
- [ ] 已购买域名
- [ ] 已配置DNS解析指向服务器IP

## 🔧 部署步骤

### 第一步：本地构建测试
```bash
# 进入项目目录
cd hotel-management-system

# 构建镜像（如果网络正常）
docker-compose build

# 如果网络问题，可以跳过此步直接部署
```

### 第二步：运行部署脚本
```bash
# 修改部署脚本中的服务器信息
vim deploy-to-tencent.sh
# 或直接编辑脚本：
# TENCENT_CLOUD_IP="你的服务器IP"
# SSH_USER="ubuntu"  # 或其他用户名

# 运行部署脚本
chmod +x deploy-to-tencent.sh
./deploy-to-tencent.sh
```

### 第三步：验证部署
访问以下地址验证部署成功：
- [ ] 前端应用：http://你的服务器IP
- [ ] API接口：http://你的服务器IP/api
- [ ] 健康检查：http://你的服务器IP/health

## 🔍 故障排除

### 如果部署脚本执行失败

#### 网络连接问题
```bash
# 检查SSH连接
ssh ubuntu@你的服务器IP "echo '连接成功'"

# 如果SSH失败，检查：
# 1. IP地址是否正确
# 2. 安全组是否开放22端口
# 3. SSH密钥是否正确配置
```

#### Docker镜像拉取问题
```bash
# 在服务器上手动配置镜像加速器
ssh ubuntu@你的服务器IP
sudo tee /etc/docker/daemon.json <<-'EOF'
{
  "registry-mirrors": [
    "https://mirror.ccs.tencentyun.com"
  ]
}
EOF
sudo systemctl restart docker
```

#### 服务启动失败
```bash
# 在服务器上检查服务状态
ssh ubuntu@你的服务器IP
cd /opt/hotel-system
docker-compose ps
docker-compose logs

# 常见问题：
# 1. 端口80被占用：netstat -tlnp | grep :80
# 2. 内存不足：free -h
# 3. 磁盘空间不足：df -h
```

### 如果应用无法访问

#### 检查服务状态
```bash
ssh ubuntu@你的服务器IP
cd /opt/hotel-system
docker-compose ps
```

#### 检查防火墙
```bash
ssh ubuntu@你的服务器IP
sudo ufw status
# 确保80端口开放
```

#### 检查应用日志
```bash
ssh ubuntu@你的服务器IP
cd /opt/hotel-system
docker-compose logs backend
docker-compose logs frontend
```

## 📊 部署后配置

### 配置域名（推荐）
```bash
# 在腾讯云控制台配置域名解析
# 类型：A记录
# 主机记录：@
# 记录值：你的服务器IP
```

### 配置SSL证书（推荐）
```bash
ssh ubuntu@你的服务器IP

# 安装Certbot
sudo apt install -y certbot python3-certbot-nginx

# 获取免费SSL证书
sudo certbot --nginx -d 你的域名.com

# 设置自动续期
sudo crontab -e
# 添加：0 12 * * * /usr/bin/certbot renew --quiet
```

### 数据库备份设置
```bash
ssh ubuntu@你的服务器IP

# 创建备份脚本
sudo tee /usr/local/bin/backup-db.sh <<-'EOF'
#!/bin/bash
BACKUP_DIR="/data/backup"
DATE=$(date +%Y%m%d_%H%M%S)
DB_NAME="hotel_management_system"
DB_USER="hotel_user"
DB_PASS="your-password"

mkdir -p $BACKUP_DIR
cd /opt/hotel-system
docker-compose exec -T mysql mysqldump -u$DB_USER -p$DB_PASS $DB_NAME > $BACKUP_DIR/backup_$DATE.sql

# 只保留最近7天的备份
find $BACKUP_DIR -name "backup_*.sql" -mtime +7 -delete
EOF

# 设置定时备份
sudo chmod +x /usr/local/bin/backup-db.sh
sudo crontab -e
# 添加：0 2 * * * /usr/local/bin/backup-db.sh
```

## 📞 技术支持

如果部署过程中遇到问题：

1. **检查日志**：运行 `docker-compose logs` 查看详细错误信息
2. **查看系统资源**：运行 `docker stats` 和 `df -h` 检查资源使用情况
3. **网络诊断**：运行 `ping docker.io` 检查网络连接
4. **参考文档**：查看 `DOCKER_README.md` 获取更多故障排除信息

## ✅ 部署完成标志

- [ ] 前端页面可以正常访问
- [ ] 后端API返回正常响应
- [ ] 数据库连接正常
- [ ] 用户可以正常注册登录
- [ ] 数据持久化正常（重启服务后数据不丢失）

---

**🎉 恭喜！部署完成后，你的酒店管理系统就可以正式使用了！**

