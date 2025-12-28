package com.hotel.hotel.repository;

import com.hotel.hotel.entity.GuestProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

/**
 * 客户画像数据访问接口
 */
public interface GuestProfileRepository extends JpaRepository<GuestProfile, Long> {
    /**
     * 根据会员ID查询客户画像
     */
    Optional<GuestProfile> findByMemberId(String memberId);

    /**
     * 根据会员ID和酒店ID查询（租户隔离）
     */
    Optional<GuestProfile> findByMemberIdAndHotelId(String memberId, Long hotelId);

    /**
     * 根据会员ID或标签模糊搜索
     */
    List<GuestProfile> findByMemberIdContainingIgnoreCaseOrTagsContainingIgnoreCase(
            String memberId, String tags);

    /**
     * 根据偏好标签模糊搜索
     */
    List<GuestProfile> findByPreferenceTagsContainingIgnoreCase(String preference);

    /**
     * 获取最近更新的100个客户画像
     */
    List<GuestProfile> findTop100ByOrderByUpdatedAtDesc();

    /**
     * 根据酒店ID获取客户画像列表
     */
    @Query("SELECT g FROM GuestProfile g WHERE g.hotelId = :hotelId ORDER BY g.updatedAt DESC")
    List<GuestProfile> findByHotelIdOrderByUpdatedAt(@Param("hotelId") Long hotelId);

    /**
     * 根据酒店ID和偏好标签搜索
     */
    @Query("SELECT g FROM GuestProfile g WHERE g.hotelId = :hotelId " +
           "AND g.preferenceTags LIKE %:preference% ORDER BY g.updatedAt DESC")
    List<GuestProfile> findByHotelIdAndPreferenceContaining(
            @Param("hotelId") Long hotelId,
            @Param("preference") String preference);

    /**
     * 根据酒店ID和关键词搜索
     */
    @Query("SELECT g FROM GuestProfile g WHERE g.hotelId = :hotelId " +
           "AND (g.memberId LIKE %:keyword% OR g.tags LIKE %:keyword% OR g.customerName LIKE %:keyword%) " +
           "ORDER BY g.updatedAt DESC")
    List<GuestProfile> findByHotelIdAndKeyword(
            @Param("hotelId") Long hotelId,
            @Param("keyword") String keyword);
}
