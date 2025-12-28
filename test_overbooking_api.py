#!/usr/bin/env python3
import requests
import json

BASE_URL = "http://localhost:8082"

def test_overbooking_apis():
    print("🔍 测试超售相关API")
    print("-" * 40)

    # 测试获取超售历史
    try:
        response = requests.get(f"{BASE_URL}/api/overbooking/history", params={"hotelId": 1, "page": 0, "size": 10})
        print(f"📡 获取超售历史: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"✅ 返回格式正确，包含字段: {list(data.keys())}")
            if 'content' in data:
                print(f"📋 历史记录数量: {len(data['content'])}")
        else:
            print(f"❌ 请求失败: {response.status_code}")
    except Exception as e:
        print(f"❌ 网络错误: {e}")

    # 测试获取超售统计
    try:
        response = requests.get(f"{BASE_URL}/api/overbooking/performance", params={"hotelId": 1})
        print(f"📡 获取超售统计: {response.status_code}")
        if response.status_code == 200:
            data = response.json()
            print(f"✅ 返回格式正确，包含字段: {list(data.keys())}")
            print(f"📊 统计数据: 总决策数={data.get('totalDecisions', 0)}, 接受决策数={data.get('acceptedDecisions', 0)}")
        else:
            print(f"❌ 请求失败: {response.status_code}")
    except Exception as e:
        print(f"❌ 网络错误: {e}")

if __name__ == "__main__":
    test_overbooking_apis()
