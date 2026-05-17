package com.sky.controller.user;

import com.sky.dto.ChatMessageDTO;
import com.sky.result.Result;
import com.sky.service.ChatService;
import com.sky.vo.ChatMessageVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController("userChatController")
@RequestMapping("/user/chat")
@Api(tags = "C端-AI客服接口")
@Slf4j
public class ChatController {

    @Autowired
    private ChatService chatService;

    /**
     * 发送消息给AI客服
     * @param chatMessageDTO 对话请求
     * @return AI回复
     */
    @PostMapping("/send")
    @ApiOperation("发送消息给AI客服")
    public Result<ChatMessageVO> sendMessage(@RequestBody ChatMessageDTO chatMessageDTO) {
        log.info("用户发送消息给AI客服: {}", chatMessageDTO);
        ChatMessageVO response = chatService.chat(chatMessageDTO);
        return Result.success(response);
    }

}
