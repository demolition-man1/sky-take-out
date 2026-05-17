package com.sky.service;

import com.sky.dto.ChatMessageDTO;
import com.sky.vo.ChatMessageVO;

public interface ChatService {

    /**
     * 处理用户对话请求
     * @param chatMessageDTO 对话请求
     * @return 对话响应
     */
    ChatMessageVO chat(ChatMessageDTO chatMessageDTO);

}
