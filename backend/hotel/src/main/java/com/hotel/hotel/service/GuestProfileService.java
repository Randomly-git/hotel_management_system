package com.hotel.hotel.service;

import com.hotel.hotel.entity.GuestProfile;
import com.hotel.hotel.repository.GuestProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class GuestProfileService {

    private final GuestProfileRepository profileRepository;

    @Autowired
    public GuestProfileService(GuestProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    /**
     * 根据会员ID查询客户画像
     * @param memberId 酒店会员ID
     * @return 客户画像实体
     */
    public Optional<GuestProfile> getProfileByMemberId(String memberId) {
        return profileRepository.findByMemberId(memberId);
    }

    /**
     * 更新或创建客户画像（例如，入住或退房时调用）
     * @param profile 要保存或更新的客户画像
     * @return 保存后的客户画像
     */
    public GuestProfile saveOrUpdateProfile(GuestProfile profile) {
        // 在实际应用中，这里会有复杂的逻辑来合并新数据和旧数据
        return profileRepository.save(profile);
    }

    /**
     * 提取用于 AI 预测模型的特征数据
     * * ⚠️ 【核心与AI的接口】
     * 在生产环境中，这里会调用一个独立的AI服务或本地模型。
     * * @param memberId 酒店会员ID
     * @return 预测模型所需的特征数据（简化为JSON字符串）
     */
    public String extractFeaturesForPrediction(String memberId) {
        GuestProfile profile = getProfileByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("客户画像不存在: " + memberId));

        // 模拟提取和格式化特征数据
        String features = String.format(
                "{\"memberId\":\"%s\", \"tags\":\"%s\", \"lastCheckIn\":\"%s\", \"avgSpend\":%s}",
                profile.getMemberId(),
                profile.getPreferenceTags() != null ? profile.getPreferenceTags() : "",
                profile.getLastCheckIn(),
                profile.getAvgSpend()
        );
        return features;
    }

    /**
     * 根据会员ID获取客户画像
     * @param memberId 会员ID
     * @return 客户画像
     */
    public GuestProfile getGuestProfileByMemberId(String memberId) {
        return profileRepository.findByMemberId(memberId)
                .orElseThrow(() -> new RuntimeException("客户画像不存在: " + memberId));
    }

    /**
     * 更新客户画像
     * @param memberId 会员ID
     * @param preferences 偏好列表
     * @param tags 标签列表
     * @param lastStayRoom 最近入住房间
     * @param remarks 备注
     * @return 更新后的客户画像
     */
    @Transactional
    public GuestProfile updateGuestProfile(String memberId, List<String> preferences,
                                         List<String> tags, String lastStayRoom, String remarks) {
        GuestProfile profile = getGuestProfileByMemberId(memberId);

        // 更新偏好标签
        if (preferences != null) {
            String preferenceStr = String.join(",", preferences);
            profile.setPreferenceTags(preferenceStr);
        }

        // 更新客户标签
        if (tags != null) {
            String tagStr = String.join(",", tags);
            profile.setTags(tagStr);
        }

        // 更新最近入住房间
        if (lastStayRoom != null) {
            profile.setLastStayRoom(lastStayRoom);
        }

        // 更新备注
        if (remarks != null) {
            profile.setRemarks(remarks);
        }

        // 更新最后修改时间
        profile.setUpdatedAt(LocalDateTime.now());

        return profileRepository.save(profile);
    }

    /**
     * 添加客户偏好
     * @param memberId 会员ID
     * @param preference 偏好标签
     * @return 更新后的客户画像
     */
    @Transactional
    public GuestProfile addPreference(String memberId, String preference) {
        GuestProfile profile = getGuestProfileByMemberId(memberId);

        // 获取现有偏好
        String existingPreferences = profile.getPreferenceTags();
        List<String> preferenceList = new ArrayList<>();

        if (existingPreferences != null && !existingPreferences.trim().isEmpty()) {
            preferenceList = Arrays.asList(existingPreferences.split(","));
        }

        // 添加新偏好（避免重复）
        if (!preferenceList.contains(preference)) {
            preferenceList.add(preference);
        }

        // 保存更新
        profile.setPreferenceTags(String.join(",", preferenceList));
        profile.setUpdatedAt(LocalDateTime.now());

        return profileRepository.save(profile);
    }

    /**
     * 获取客户历史记录
     * @param memberId 会员ID
     * @return 历史记录信息
     */
    public Map<String, Object> getGuestHistory(String memberId) {
        GuestProfile profile = getGuestProfileByMemberId(memberId);

        Map<String, Object> history = new HashMap<>();

        // 基本信息
        history.put("memberId", profile.getMemberId());
        history.put("totalStay", profile.getTotalStay());
        history.put("lastCheckIn", profile.getLastCheckIn());
        history.put("lastStayRoom", profile.getLastStayRoom());

        // 消费信息
        history.put("avgSpend", profile.getAvgSpend());
        history.put("totalSpend", profile.getTotalSpend());

        // 偏好历史
        if (profile.getPreferenceTags() != null) {
            history.put("preferences", Arrays.asList(profile.getPreferenceTags().split(",")));
        }

        // 标签信息
        if (profile.getTags() != null) {
            history.put("tags", Arrays.asList(profile.getTags().split(",")));
        }

        return history;
    }

    /**
     * 搜索客户画像
     * @param keyword 关键词
     * @param preference 偏好标签
     * @return 匹配的客户画像列表
     */
    public List<GuestProfile> searchGuestProfiles(String keyword, String preference) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            // 根据会员ID或标签搜索
            return profileRepository.findByMemberIdContainingIgnoreCaseOrTagsContainingIgnoreCase(keyword, keyword);
        }

        if (preference != null && !preference.trim().isEmpty()) {
            // 根据偏好标签搜索
            return profileRepository.findByPreferenceTagsContainingIgnoreCase(preference);
        }

        // 如果没有搜索条件，返回前100个客户
        return profileRepository.findTop100ByOrderByUpdatedAtDesc();
    }
}