#!/bin/bash

# 酒店管理系统腾讯云部署脚本
# 使用方法: ./deploy-to-tencent.sh [服务器IP] [SSH密钥路径]

set -e

# 配置参数
TENCENT_CLOUD_IP=${1:-"111.229.207.192"}
SSH_USER=${2:-"root"}
SSH_USER=${2:-"ubuntu"}
PROJECT_DIR=${3:-"/opt/hotel-system"}
DOCKER_COMPOSE_FILE="docker-compose.yml"

echo "🚀 开始部署酒店管理系统到腾讯云服务器"

# 检查参数
if [ "$TENCENT_CLOUD_IP" = "YOUR_TENCENT_CLOUD_SERVER_IP" ]; then
    echo "❌ 请提供腾讯云服务器IP地址"
    echo "使用方法: $0 <服务器IP> [SSH用户名] [项目目录]"
    echo "示例: $0 123.456.789.0 ubuntu /opt/hotel-system"
    exit 1
fi

# 设置默认值
SSH_USER=${SSH_USER:-"ubuntu"}
PROJECT_DIR=${PROJECT_DIR:-"/opt/hotel-system"}

# 构建Docker镜像
echo "📦 构建Docker镜像..."
docker-compose build

# 保存镜像为tar文件
echo "💾 保存镜像..."
docker save hotel-backend:latest -o hotel-backend.tar
docker save hotel-frontend:latest -o hotel-frontend.tar

# 检查SSH连接
echo "🔗 检查SSH连接..."
if ! ssh -o ConnectTimeout=10 -o StrictHostKeyChecking=no "$SSH_USER@$TENCENT_CLOUD_IP" "echo 'SSH连接成功'" 2>/dev/null; then
    echo "❌ SSH连接失败，请检查："
    echo "  1. 服务器IP是否正确: $TENCENT_CLOUD_IP"
    echo "  2. SSH密钥是否正确配置"
    echo "  3. 安全组是否开放22端口"
    exit 1
fi

# 传输文件到腾讯云服务器
echo "📤 传输文件到腾讯云服务器..."
ssh "$SSH_USER@$TENCENT_CLOUD_IP" "mkdir -p $PROJECT_DIR" || {
    echo "❌ 创建远程目录失败"
    exit 1
}

scp "$DOCKER_COMPOSE_FILE" "$SSH_USER@$TENCENT_CLOUD_IP:$PROJECT_DIR/" || {
    echo "❌ 上传docker-compose.yml失败"
    exit 1
}
scp "Dockerfile.backend" "$SSH_USER@$TENCENT_CLOUD_IP:$PROJECT_DIR/" || {
    echo "❌ 上传Dockerfile.backend失败"
    exit 1
}
scp "Dockerfile.frontend" "$SSH_USER@$TENCENT_CLOUD_IP:$PROJECT_DIR/" || {
    echo "❌ 上传Dockerfile.frontend失败"
    exit 1
}
scp "nginx.conf" "$SSH_USER@$TENCENT_CLOUD_IP:$PROJECT_DIR/" || {
    echo "❌ 上传nginx.conf失败"
    exit 1
}
scp ".dockerignore" "$SSH_USER@$TENCENT_CLOUD_IP:$PROJECT_DIR/" || {
    echo "❌ 上传.dockerignore失败"
    exit 1
}
scp "hotel-backend.tar" "$SSH_USER@$TENCENT_CLOUD_IP:$PROJECT_DIR/" || {
    echo "❌ 上传backend镜像失败"
    exit 1
}
scp "hotel-frontend.tar" "$SSH_USER@$TENCENT_CLOUD_IP:$PROJECT_DIR/" || {
    echo "❌ 上传frontend镜像失败"
    exit 1
}

# 传输数据库迁移脚本
echo "📤 传输数据库迁移脚本..."
ssh "$SSH_USER@$TENCENT_CLOUD_IP" "mkdir -p $PROJECT_DIR/backend/hotel/src/main/resources/db/migration"
scp "backend/hotel/src/main/resources/db/migration/*.sql" "$SSH_USER@$TENCENT_CLOUD_IP:$PROJECT_DIR/backend/hotel/src/main/resources/db/migration/" || {
    echo "❌ 上传数据库迁移脚本失败"
    exit 1
}

# 在腾讯云服务器上部署
echo "🔧 在腾讯云服务器上部署..."
ssh "$SSH_USER@$TENCENT_CLOUD_IP" "cd $PROJECT_DIR && ls -la" || {
    echo "❌ 进入项目目录失败"
    exit 1
}

ssh "$SSH_USER@$TENCENT_CLOUD_IP" << EOF
    set -e
    cd $PROJECT_DIR

    echo "🔄 检查系统环境..."
    # 检查是否为Ubuntu/Debian
    if ! command -v apt &> /dev/null; then
        echo "❌ 此脚本仅支持Ubuntu/Debian系统"
        exit 1
    fi

    # 更新系统包
    echo "📦 更新系统..."
    sudo apt update && sudo apt upgrade -y

    # 安装Docker
    echo "🐳 安装Docker..."
    if ! command -v docker &> /dev/null; then
        sudo apt install -y docker.io
        sudo systemctl start docker
        sudo systemctl enable docker
        sudo usermod -aG docker \$USER
    fi

    # 安装Docker Compose
    echo "🐳 安装Docker Compose..."
    if ! command -v docker-compose &> /dev/null; then
        sudo curl -L "https://github.com/docker/compose/releases/latest/download/docker-compose-\$(uname -s)-\$(uname -m)" -o /usr/local/bin/docker-compose
        sudo chmod +x /usr/local/bin/docker-compose
    fi

    # 配置腾讯云镜像加速器
    echo "⚡ 配置Docker镜像加速器..."
    sudo mkdir -p /etc/docker
    sudo tee /etc/docker/daemon.json > /dev/null <<'DAEMON_EOF'
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
DAEMON_EOF

    sudo systemctl restart docker

    # 加载Docker镜像
    echo "📦 加载Docker镜像..."
    docker load -i hotel-backend.tar
    docker load -i hotel-frontend.tar

    # 创建必要的目录
    sudo mkdir -p /data/mysql
    sudo mkdir -p /data/logs

    # 配置防火墙
    echo "🔥 配置防火墙..."
    sudo ufw allow 80/tcp
    sudo ufw allow 443/tcp
    echo "y" | sudo ufw enable

    # 启动服务
    echo "🚀 启动服务..."
    docker-compose up -d

    # 等待服务启动
    echo "⏳ 等待服务启动..."
    sleep 60

    # 检查服务状态
    echo "📊 检查服务状态..."
    docker-compose ps

    # 检查服务健康状态
    echo "🏥 检查服务健康状态..."
    if curl -f http://localhost/health 2>/dev/null; then
        echo "✅ 前端健康检查通过"
    else
        echo "⚠️ 前端健康检查失败，请稍后手动检查"
    fi

    if curl -f http://localhost/api/actuator/health 2>/dev/null; then
        echo "✅ 后端健康检查通过"
    else
        echo "⚠️ 后端健康检查失败，请稍后手动检查"
    fi

    echo ""
    echo "🎉 部署完成！"
    echo "🌐 访问地址: http://$TENCENT_CLOUD_IP"
    echo "🔗 API地址: http://$TENCENT_CLOUD_IP/api"
    echo "📊 管理后台: http://$TENCENT_CLOUD_IP"
EOF
    # 安装Docker和Docker Compose
    sudo apt update
    sudo apt install -y docker.io docker-compose-plugin

    # 启动Docker服务
    sudo systemctl start docker
    sudo systemctl enable docker

    # 加载Docker镜像
    echo "加载Docker镜像..."
    docker load -i hotel-backend.tar
    docker load -i hotel-frontend.tar

    # 创建项目目录
    mkdir -p hotel-system

    # 移动配置文件
    mv docker-compose.yml hotel-system/
    mv nginx.conf hotel-system/

    # 进入项目目录
    cd hotel-system

    # 修改docker-compose.yml中的端口映射（如果需要）
    # 可以在这里添加防火墙规则
    sudo ufw allow 80
    sudo ufw allow 3306
    sudo ufw --force enable

    # 启动服务
    echo "启动服务..."
    docker-compose up -d

    # 等待服务启动
    echo "等待服务启动..."
    sleep 30

    # 检查服务状态
    docker-compose ps

    echo "✅ 部署完成！"
    echo "🌐 前端访问地址: http://$SERVER_IP"
    echo "🔗 API地址: http://$SERVER_IP/api"
    echo "🗄️ 数据库端口: $SERVER_IP:3306"
EOF

if [ $? -eq 0 ]; then
    # 清理本地临时文件
    echo "🧹 清理本地临时文件..."
    rm -f hotel-backend.tar hotel-frontend.tar

    echo ""
    echo "🎉 腾讯云部署完成！"
    echo ""
    echo "📋 访问信息:"
    echo "🌐 前端应用: http://$TENCENT_CLOUD_IP"
    echo "🔗 后端API: http://$TENCENT_CLOUD_IP/api"
    echo "🏥 健康检查: http://$TENCENT_CLOUD_IP/health"
    echo ""
    echo "🔍 管理命令:"
    echo "检查服务状态: ssh $SSH_USER@$TENCENT_CLOUD_IP 'cd $PROJECT_DIR && docker-compose ps'"
    echo "查看实时日志: ssh $SSH_USER@$TENCENT_CLOUD_IP 'cd $PROJECT_DIR && docker-compose logs -f'"
    echo "重启服务: ssh $SSH_USER@$TENCENT_CLOUD_IP 'cd $PROJECT_DIR && docker-compose restart'"
    echo "停止服务: ssh $SSH_USER@$TENCENT_CLOUD_IP 'cd $PROJECT_DIR && docker-compose down'"
    echo ""
    echo "🛠️ 如果遇到问题，请运行:"
    echo "ssh $SSH_USER@$TENCENT_CLOUD_IP 'cd $PROJECT_DIR && docker-compose logs'"
    echo ""
    echo "📞 技术支持: 检查 DOCKER_README.md 获取更多故障排除信息"
else
    echo "❌ 部署失败，请检查上述错误信息"
    exit 1
fi
