package com.sky.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * AI客服对话响应VO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageVO implements Serializable {

    private static final long serialVersionUID = 1L;

    // AI回复内容
    private String reply;

    // 会话ID
    private String sessionId;

    // 是否推荐菜品
    private Boolean hasRecommendation;

    // 推荐的菜品或套餐ID列表
    private java.util.List<Long> recommendedIds;

    // 推荐类型：dish-菜品，setmeal-套餐
    private String recommendationType;

}
