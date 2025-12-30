package com.hotel.hotel.repository;

import com.hotel.hotel.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 客户 Repository
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 根据酒店ID查询客户列表
     */
    List<Customer> findByHotelId(Long hotelId);

    /**
     * 根据酒店ID和证件号查询
     */
    Optional<Customer> findByHotelIdAndIdCardNumber(Long hotelId, String idCardNumber);

    /**
     * 根据酒店ID和姓名模糊查询
     */
    List<Customer> findByHotelIdAndNameContaining(Long hotelId, String name);

    /**
     * 根据酒店ID和VIP等级查询
     */
    List<Customer> findByHotelIdAndVipLevel(Long hotelId, Customer.VipLevel vipLevel);

    /**
     * 查询重复客户
     */
    List<Customer> findByHotelIdAndIsRepeatedGuestTrue(Long hotelId);

    /**
     * 统计各VIP等级客户数量
     */
    @Query("SELECT c.vipLevel, COUNT(c) FROM Customer c WHERE c.hotelId = :hotelId GROUP BY c.vipLevel")
    List<Object[]> countByVipLevelGroupBy(@Param("hotelId") Long hotelId);

    /**
     * 根据会员ID查询客户
     */
    Optional<Customer> findByMemberId(String memberId);

    /**
     * 根据酒店ID和会员ID查询客户
     */
    Optional<Customer> findByHotelIdAndMemberId(Long hotelId, String memberId);

    /**
     * 根据偏好标签搜索客户
     */
    List<Customer> findByHotelIdAndPreferenceTagsContainingIgnoreCase(Long hotelId, String preferenceTag);

    /**
     * 根据标签搜索客户
     */
    List<Customer> findByHotelIdAndTagsContainingIgnoreCase(Long hotelId, String tag);
}
