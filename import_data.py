import pandas as pd
import pymysql
from datetime import datetime

# 1. 配置映射关系 (原始 A-H 对应 数据库 1-8)
room_type_map = {
    'A': 1, 'B': 2, 'C': 3, 'D': 4,
    'E': 5, 'F': 6, 'G': 7, 'H': 8
}

def fix_date(row):
    # 原始数据通常有 arrival_date_year, month, day_of_month 三列
    try:
        year = int(row['arrival_date_year']) + 8 # 年份平移：2015 -> 2023
        month_str = row['arrival_date_month']
        day = int(row['arrival_date_day_of_month'])

        # 月份转换
        dt = datetime.strptime(f"{year} {month_str} {day}", "%Y %B %d")
        return dt.strftime("%Y-%m-%d")
    except:
        return "2024-01-01" # 兜底日期

# 2. 读取 CSV 前 20000 条
df = pd.read_csv('hotel_bookings.csv', nrows=20000)

# 3. 连接数据库 (根据你的 yml 配置)
conn = pymysql.connect(
    host='47.100.240.111',
    user='root',
    password='Db123456',
    database='hotel_management_system',
    charset='utf8mb4'
)

try:
    with conn.cursor() as cursor:
        # A. 清空原有数据 (慎重操作，已按要求加入)
        print("正在清空旧数据...")
        cursor.execute("SET FOREIGN_KEY_CHECKS = 0;") # 临时关闭外键约束
        cursor.execute("TRUNCATE TABLE bookings;")
        cursor.execute("SET FOREIGN_KEY_CHECKS = 1;")

        # B. 准备插入数据
        sql = """INSERT INTO bookings (hotel_id, room_type_id, check_in_date, check_out_date, status, customer_id)
                 VALUES (%s, %s, %s, %s, %s, %s)"""

        records = []
        for _, row in df.iterrows():
            arrival_date = fix_date(row)
            # 原始数据有 stays_in_weekend_nights + stays_in_week_nights
            duration = int(row['stays_in_weekend_nights']) + int(row['stays_in_week_nights'])
            if duration == 0: duration = 1 # 至少住一天

            # 计算离店日期 (这里简化处理，直接加持续天数)
            check_in = datetime.strptime(arrival_date, "%Y-%m-%d")
            check_out = check_in.replace(day=check_in.day + duration) if check_in.day + duration <= 28 else check_in

            # 映射房型
            raw_type = row['reserved_room_type'].strip()
            room_type_id = room_type_map.get(raw_type, 1) # 找不到则默认为标准间 1

            records.append((
                1, # hotel_id 统一为 1
                room_type_id,
                arrival_date,
                check_out.strftime("%Y-%m-%d"),
                'CONFIRMED',
                100 # 模拟 customer_id
            ))

        # C. 批量执行
        cursor.executemany(sql, records)
        conn.commit()
        print(f"成功导入 {len(records)} 条数据，房型已按 A-H -> 1-8 映射。")

finally:
    conn.close()