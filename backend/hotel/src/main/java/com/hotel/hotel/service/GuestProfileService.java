package com.hotel.hotel.service;

import com.hotel.hotel.entity.GuestProfile;
import com.hotel.hotel.repository.GuestProfileRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Optional;

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
}