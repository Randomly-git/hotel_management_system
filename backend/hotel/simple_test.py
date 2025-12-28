#!/usr/bin/env python3
"""
简单的API测试脚本
用于测试酒店管理系统API
"""

import requests
import json

BASE_URL = "http://localhost:8082"

def test_endpoint(endpoint, method="GET", data=None):
    """测试API端点"""
    url = f"{BASE_URL}{endpoint}"

    try:
        if method == "GET":
            response = requests.get(url)
        elif method == "POST":
            response = requests.post(url, json=data, headers={"Content-Type": "application/json"})

        print(f"\n{'='*50}")
        print(f"测试: {method} {endpoint}")
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.text}")

        return response.status_code == 200

    except Exception as e:
        print(f"❌ 测试失败: {e}")
        return False

def main():
    print("🚀 开始API测试...")

    success_count = 0
    total_tests = 0

    # 测试1: 健康检查
    total_tests += 1
    if test_endpoint("/actuator/health"):
        success_count += 1

    # 测试2: 任务统计
    total_tests += 1
    if test_endpoint("/api/v1/personalization/tasks/statistics"):
        success_count += 1

    # 测试3: 查询待处理任务
    total_tests += 1
    if test_endpoint("/api/v1/personalization/tasks/pending"):
        success_count += 1

    # 测试4: 客户请求（这是关键测试）
    print(f"\n{'='*50}")
    print("🧪 测试客户请求处理")
    request_data = {
        "customerId": "TEST001",
        "requestContent": "我滴空调怎么坏掉啦瓦",
        "roomNumber": "801",
        "hotelId": 1
    }

    try:
        response = requests.post(
            f"{BASE_URL}/api/v1/personalization/request",
            json=request_data,
            headers={"Content-Type": "application/json"}
        )
        print(f"状态码: {response.status_code}")
        print(f"响应: {response.text}")

        if response.status_code in [200, 201]:
            success_count += 1
            print("✅ 客户请求测试成功")
        else:
            print("❌ 客户请求测试失败")

        total_tests += 1

    except Exception as e:
        print(f"❌ 客户请求测试失败: {e}")
        total_tests += 1

    print(f"\n{'='*50}")
    print("🎉 测试完成!")
    print(f"✅ 成功: {success_count}/{total_tests}")
    print(f"❌ 失败: {total_tests - success_count}/{total_tests}")

if __name__ == "__main__":
    main()