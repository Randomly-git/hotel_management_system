#!/usr/bin/env python3
"""
个性化服务系统API测试脚本
使用requests库进行API测试
"""

import requests
import json
import time

# API基础URL
BASE_URL = "http://localhost:8082/api/v1/personalization"

def test_api(method, endpoint, data=None):
    """测试API接口"""
    url = f"{BASE_URL}{endpoint}"
    headers = {"Content-Type": "application/json"}

    try:
        if method.upper() == "GET":
            response = requests.get(url, headers=headers)
        elif method.upper() == "POST":
            response = requests.post(url, headers=headers, json=data)
        elif method.upper() == "PUT":
            response = requests.put(url, headers=headers, json=data)

        print(f"\n{'='*50}")
        print(f"测试: {method} {endpoint}")
        print(f"状态码: {response.status_code}")
        print(f"响应: {json.dumps(response.json(), indent=2, ensure_ascii=False)}")

        return response.status_code == 200 or response.status_code == 201

    except Exception as e:
        print(f"❌ 测试失败: {e}")
        return False

def main():
    """主测试函数"""
    print("🚀 开始API测试...")

    success_count = 0
    total_tests = 0

    # 测试1: 创建客户画像
    print("\n📝 测试1: 创建客户画像")
    profile_data = {
        "memberId": "VIP001",
        "customerName": "张三",
        "phone": "13800138000",
        "idCard": "310101199001011234",
        "hotelId": 1,
        "roomNumber": "801",
        "preferences": ["安静环境", "高层房间"],
        "tags": ["VIP", "商务客户"]
    }

    total_tests += 1
    if test_api("POST", "/profiles", profile_data):
        success_count += 1
        time.sleep(1)

    # 测试2: 客户请求处理
    print("\n🤖 测试2: 客户请求处理")
    request_data = {
        "customerId": "VIP001",
        "requestContent": "房间里有点冷，能不能调高空调温度？另外我需要一条浴巾",
        "roomNumber": "801"
    }

    total_tests += 1
    if test_api("POST", "/request", request_data):
        success_count += 1
        time.sleep(1)

    # 测试3: AI预测任务
    print("\n🔮 测试3: AI预测任务生成")
    predict_data = {
        "memberId": "VIP001",
        "predictedNeed": "预测客户需要儿童座椅和加床服务",
        "recommendedDepartment": "房务部",
        "priority": "MEDIUM"
    }

    total_tests += 1
    if test_api("POST", "/predict", predict_data):
        success_count += 1
        time.sleep(1)

    # 测试4: 查询待处理任务
    print("\n📋 测试4: 查询待处理任务")
    total_tests += 1
    if test_api("GET", "/tasks/pending"):
        success_count += 1
        time.sleep(1)

    # 测试5: 获取任务统计
    print("\n📊 测试5: 获取任务统计")
    total_tests += 1
    if test_api("GET", "/tasks/statistics"):
        success_count += 1
        time.sleep(1)

    # 测试6: 查询客户画像
    print("\n👤 测试6: 查询客户画像")
    total_tests += 1
    if test_api("GET", "/profiles/VIP001"):
        success_count += 1
        time.sleep(1)

    # 测试7: 更新客户画像
    print("\n✏️ 测试7: 更新客户画像")
    update_data = {
        "preferences": ["安静环境", "高层房间", "无烟房", "靠窗"],
        "tags": ["VIP", "商务客户", "长住客", "老客户"]
    }

    total_tests += 1
    if test_api("PUT", "/profiles/VIP001", update_data):
        success_count += 1

    # 测试结果汇总
    print(f"\n{'='*50}")
    print("🎉 测试完成!")
    print(f"✅ 成功: {success_count}/{total_tests}")
    print(f"❌ 失败: {total_tests - success_count}/{total_tests}")

    if success_count == total_tests:
        print("🌟 所有测试通过！")
    else:
        print("⚠️ 部分测试失败，请检查应用状态")

if __name__ == "__main__":
    main()