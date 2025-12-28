#!/usr/bin/env python3
import requests
import json
import sys

BASE_URL = "http://localhost:8082"

def test_api(endpoint, method='GET', data=None, params=None, description=""):
    """测试API接口"""
    try:
        url = f"{BASE_URL}{endpoint}"
        print(f"\n🔍 测试: {description}")
        print(f"📡 {method} {url}")

        if method == 'GET':
            response = requests.get(url, params=params, timeout=10)
        elif method == 'POST':
            response = requests.post(url, json=data, timeout=10)
        elif method == 'PUT':
            response = requests.put(url, json=data, timeout=10)
        elif method == 'PATCH':
            response = requests.patch(url, json=data, timeout=10)
        else:
            print(f"❌ 不支持的HTTP方法: {method}")
            return False

        print(f"📊 状态码: {response.status_code}")

        if response.status_code >= 200 and response.status_code < 300:
            print("✅ 请求成功")
            try:
                json_data = response.json()
                if isinstance(json_data, dict) and 'content' in json_data:
                    content = json_data['content']
                    if isinstance(content, list):
                        print(f"📋 返回数据条数: {len(content)}")
                        if len(content) > 0:
                            print(f"📝 首条数据预览: {json.dumps(content[0], ensure_ascii=False, indent=2)[:200]}...")
                    else:
                        print(f"📋 返回数据: {json.dumps(content, ensure_ascii=False, indent=2)[:200]}...")
                elif isinstance(json_data, list):
                    print(f"📋 返回数组长度: {len(json_data)}")
                else:
                    print(f"📋 返回数据类型: {type(json_data)}")
            except:
                print(f"📋 响应内容: {response.text[:200]}...")
            return True
        else:
            print(f"❌ 请求失败: {response.status_code}")
            try:
                error_data = response.json()
                print(f"❌ 错误信息: {error_data}")
            except:
                print(f"❌ 错误内容: {response.text[:200]}...")
            return False

    except requests.exceptions.RequestException as e:
        print(f"❌ 网络错误: {e}")
        return False
    except Exception as e:
        print(f"❌ 其他错误: {e}")
        return False

def main():
    print("🚀 开始测试酒店管理系统后端API")
    print("=" * 50)

    # 测试结果统计
    total_tests = 0
    passed_tests = 0

    # 1. 测试房间相关API
    print("\n🏨 房间管理API测试")
    print("-" * 30)

    # 获取酒店房间列表
    total_tests += 1
    if test_api("/api/rooms/hotel/1", description="获取酒店房间列表"):
        passed_tests += 1

    # 获取房间统计
    total_tests += 1
    if test_api("/api/rooms/hotel/1/statistics", description="获取房间统计信息"):
        passed_tests += 1

    # 获取房型列表
    total_tests += 1
    if test_api("/api/rooms/room-types/hotel/1", description="获取房型列表"):
        passed_tests += 1

    # 2. 测试预订相关API
    print("\n📅 预订管理API测试")
    print("-" * 30)

    # 获取酒店预订列表
    total_tests += 1
    if test_api("/api/bookings/hotel/1", description="获取酒店预订列表"):
        passed_tests += 1

    # 获取今日入住预订
    total_tests += 1
    if test_api("/api/bookings/hotel/1/today/checkins", description="获取今日入住预订"):
        passed_tests += 1

    # 获取今日退房预订
    total_tests += 1
    if test_api("/api/bookings/hotel/1/today/checkouts", description="获取今日退房预订"):
        passed_tests += 1

    # 3. 测试客户相关API
    print("\n👥 客户管理API测试")
    print("-" * 30)

    # 获取酒店客户列表
    total_tests += 1
    if test_api("/api/customers/hotel/1", description="获取酒店客户列表"):
        passed_tests += 1

    # 获取客户统计
    total_tests += 1
    if test_api("/api/customers/hotel/1/statistics", description="获取客户统计信息"):
        passed_tests += 1

    # 4. 测试仪表盘API
    print("\n📊 仪表盘API测试")
    print("-" * 30)

    # 获取仪表盘概览
    total_tests += 1
    if test_api("/api/v1/dashboard/overview", params={"hotelId": 1}, description="获取仪表盘概览"):
        passed_tests += 1

    # 获取仪表盘统计
    total_tests += 1
    if test_api("/api/v1/dashboard/statistics", params={"hotelId": 1}, description="获取仪表盘统计"):
        passed_tests += 1

    # 5. 测试个性化服务API
    print("\n🤖 个性化服务API测试")
    print("-" * 30)

    # 获取待处理任务
    total_tests += 1
    if test_api("/api/v1/personalization/tasks/pending", description="获取待处理任务"):
        passed_tests += 1

    # 6. 测试超售API
    print("\n📈 智能超售API测试")
    print("-" * 30)

    # 获取超售历史
    total_tests += 1
    if test_api("/api/overbooking/history", params={"hotelId": 1}, description="获取超售历史"):
        passed_tests += 1

    # 获取超售统计
    total_tests += 1
    if test_api("/api/overbooking/performance", params={"hotelId": 1}, description="获取超售统计"):
        passed_tests += 1

    # 7. 测试绩效API
    print("\n📈 绩效管理API测试")
    print("-" * 30)

    # 获取绩效历史
    total_tests += 1
    if test_api("/api/v1/performance/history", params={"hotelId": "1", "startDate": "2025-12-01", "endDate": "2025-12-31"}, description="获取绩效历史"):
        passed_tests += 1

    # 8. 测试反馈API
    print("\n💬 客户反馈API测试")
    print("-" * 30)

    # 提交反馈测试（可选）
    print("\n🔍 注意: 反馈API需要POST请求，这里仅测试GET接口可用性")

    # 9. 测试定价API
    print("\n💰 动态定价API测试")
    print("-" * 30)

    # 获取当前价格
    total_tests += 1
    if test_api("/api/v1/pricing/current", params={"date": "2025-12-28"}, description="获取当前价格"):
        passed_tests += 1

    # 总结测试结果
    print("\n" + "=" * 50)
    print("🎯 API测试完成总结")
    print("=" * 50)
    print(f"📊 总测试数: {total_tests}")
    print(f"✅ 通过测试: {passed_tests}")
    print(f"❌ 失败测试: {total_tests - passed_tests}")
    success_rate = (passed_tests / total_tests * 100) if total_tests > 0 else 0
    print(".1f")
    if passed_tests == total_tests:
        print("🎉 所有API测试通过！后端服务运行正常。")
    else:
        print("⚠️  部分API测试失败，请检查后端服务。")

if __name__ == "__main__":
    main()
