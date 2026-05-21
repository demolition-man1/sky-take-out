package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.sky.context.BaseContext;
import com.sky.dto.ChatMessageDTO;
import com.sky.dto.OrdersPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.Orders;
import com.sky.entity.Setmeal;
import com.sky.entity.ShoppingCart;
import com.sky.mapper.DishMapper;
import com.sky.mapper.OrderDetailMapper;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.mapper.ShoppingCartMapper;
import com.sky.service.ChatService;
import com.sky.utils.DeepSeekUtil;
import com.sky.vo.ChatMessageVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@Slf4j
public class ChatServiceImpl implements ChatService {

    @Autowired
    private DeepSeekUtil deepSeekUtil;

    @Autowired
    private DishMapper dishMapper;

    @Autowired
    private SetmealMapper setmealMapper;

    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private ShoppingCartMapper shoppingCartMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    // 会话过期时间（30分钟）
    private static final long SESSION_EXPIRE_TIME = 30;

    /**
     * 处理用户对话请求
     */
    @Override
    public ChatMessageVO chat(ChatMessageDTO chatMessageDTO) {
        String sessionId = chatMessageDTO.getSessionId();
        String userMessage = chatMessageDTO.getMessage();

        // 如果没有会话ID，生成新的
        if (sessionId == null || sessionId.isEmpty()) {
            sessionId = UUID.randomUUID().toString().replace("-", "");
        }

        log.info("收到用户消息，会话ID: {}, 消息: {}", sessionId, userMessage);

        // 获取或创建会话上下文
        List<Map<String, String>> messageHistory = getMessageHistory(sessionId);

        // 添加用户消息到历史
        Map<String, String> userMsg = new HashMap<>();
        userMsg.put("role", "user");
        userMsg.put("content", userMessage);
        messageHistory.add(userMsg);

        // 检查是否为特殊意图（订单查询、加菜减菜）
        String specialResponse = handleSpecialIntent(userMessage);
        String aiReply;
        
        if (specialResponse != null) {
            // 特殊意图已处理，直接使用返回的回复
            aiReply = specialResponse;
        } else {
            // 调用DeepSeek API
            aiReply = deepSeekUtil.chat(messageHistory);
        }

        // 添加AI回复到历史
        Map<String, String> aiMsg = new HashMap<>();
        aiMsg.put("role", "assistant");
        aiMsg.put("content", aiReply);
        messageHistory.add(aiMsg);

        // 保存会话上下文到Redis
        saveMessageHistory(sessionId, messageHistory);

        // 分析回复中是否包含推荐
        ChatMessageVO response = analyzeRecommendation(aiReply, sessionId);

        log.info("AI回复: {}", aiReply);

        return response;
    }

    /**
     * 处理特殊意图（订单查询、加菜减菜）
     * @return 如果处理了特殊意图则返回回复内容，否则返回null
     */
    private String handleSpecialIntent(String userMessage) {
        // 1. 订单状态查询
        if (userMessage.contains("订单") && (userMessage.contains("查询") || userMessage.contains("状态") || userMessage.contains("进度"))) {
            return handleOrderQuery(userMessage);
        }
        
        // 2. 智能加菜
        if ((userMessage.contains("加") || userMessage.contains("添加")) && (userMessage.contains("菜") || userMessage.contains("份") || userMessage.contains("个"))) {
            return handleAddDish(userMessage);
        }
        
        // 3. 智能减菜
        if ((userMessage.contains("减") || userMessage.contains("减少") || userMessage.contains("去掉")) && (userMessage.contains("菜") || userMessage.contains("份") || userMessage.contains("个"))) {
            return handleSubDish(userMessage);
        }
        
        return null;
    }

    /**
     * 处理订单查询
     */
    private String handleOrderQuery(String userMessage) {
        try {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return "请先登录后再查询订单哦～";
            }

            // 提取订单号（假设用户输入中包含数字）
            Pattern pattern = Pattern.compile("\\d+");
            Matcher matcher = pattern.matcher(userMessage);
            
            Orders order = null;
            
            // 尝试从用户消息中提取订单号并查询
            while (matcher.find()) {
                String num = matcher.group();
                // 尝试作为订单号查询
                order = orderMapper.getByNumber(num);
                if (order != null && order.getUserId().equals(userId)) {
                    break;
                }
                order = null;
            }
            
            // 如果没有找到指定订单，查询最近的订单
            if (order == null) {
                OrdersPageQueryDTO queryDTO = new OrdersPageQueryDTO();
                queryDTO.setUserId(userId);
                queryDTO.setPage(1);
                queryDTO.setPageSize(1);
                Page<Orders> page = orderMapper.pageQuery(queryDTO);
                if (page.getResult() != null && !page.getResult().isEmpty()) {
                    order = page.getResult().get(0);
                }
            }
            
            if (order != null) {
                String statusText = getOrderStatusText(order.getStatus());
                String estimatedTime = "";
                if (order.getEstimatedDeliveryTime() != null) {
                    String timeStr = order.getEstimatedDeliveryTime().toString();
                    if (timeStr.length() >= 16) {
                        estimatedTime = "，预计" + timeStr.substring(11, 16) + "送达";
                    }
                }
                
                return String.format("您的订单（订单号：%s）当前状态为：%s%s。订单金额：%.2f元。",
                        order.getNumber(),
                        statusText,
                        estimatedTime,
                        order.getAmount());
            }
            
            return "抱歉，我没有找到您的订单信息。请提供正确的订单号，或者您可以说\"查询最近订单\"。";
            
        } catch (Exception e) {
            log.error("订单查询异常", e);
            return "抱歉，查询订单时出现异常，请稍后重试。";
        }
    }

    /**
     * 获取订单状态文本
     */
    private String getOrderStatusText(Integer status) {
        switch (status) {
            case 1: return "待付款";
            case 2: return "待接单";
            case 3: return "已接单";
            case 4: return "配送中";
            case 5: return "已完成";
            case 6: return "已取消";
            default: return "未知状态";
        }
    }

    /**
     * 处理加菜
     */
    private String handleAddDish(String userMessage) {
        try {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return "请先登录后再加菜哦～";
            }

            // 查找用户消息中提到的菜品名称
            List<Dish> dishes = dishMapper.list(Dish.builder().status(1).build());
            Dish matchedDish = null;
            
            for (Dish dish : dishes) {
                if (userMessage.contains(dish.getName())) {
                    matchedDish = dish;
                    break;
                }
            }
            
            if (matchedDish != null) {
                // 构建购物车对象
                ShoppingCart shoppingCart = ShoppingCart.builder()
                        .userId(userId)
                        .dishId(matchedDish.getId())
                        .name(matchedDish.getName())
                        .image(matchedDish.getImage())
                        .amount(matchedDish.getPrice())
                        .number(1)
                        .createTime(LocalDateTime.now())
                        .build();
                
                // 检查是否已存在
                List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
                if (list != null && !list.isEmpty()) {
                    // 已存在，数量+1
                    ShoppingCart cart = list.get(0);
                    cart.setNumber(cart.getNumber() + 1);
                    shoppingCartMapper.updateNumberById(cart);
                    return String.format("好的！已为您增加一份%s，现在购物车中有%d份。", matchedDish.getName(), cart.getNumber());
                } else {
                    // 不存在，新增
                    shoppingCartMapper.insert(shoppingCart);
                    return String.format("好的！已为您添加%s到购物车，价格%.2f元。", matchedDish.getName(), matchedDish.getPrice());
                }
            }
            
            return "抱歉，我没有找到您说的菜品。您可以告诉我具体的菜品名称，比如\"帮我加一份宫保鸡丁\"。";
            
        } catch (Exception e) {
            log.error("加菜异常", e);
            return "抱歉，加菜时出现异常，请稍后重试。";
        }
    }

    /**
     * 处理减菜
     */
    private String handleSubDish(String userMessage) {
        try {
            Long userId = BaseContext.getCurrentId();
            if (userId == null) {
                return "请先登录后再减菜哦～";
            }

            // 查找用户消息中提到的菜品名称
            List<Dish> dishes = dishMapper.list(Dish.builder().status(1).build());
            Dish matchedDish = null;
            
            for (Dish dish : dishes) {
                if (userMessage.contains(dish.getName())) {
                    matchedDish = dish;
                    break;
                }
            }
            
            if (matchedDish != null) {
                ShoppingCart shoppingCart = ShoppingCart.builder()
                        .userId(userId)
                        .dishId(matchedDish.getId())
                        .build();
                
                List<ShoppingCart> list = shoppingCartMapper.list(shoppingCart);
                if (list != null && !list.isEmpty()) {
                    ShoppingCart cart = list.get(0);
                    if (cart.getNumber() == 1) {
                        shoppingCartMapper.deleteById(cart.getId());
                        return String.format("好的！已从购物车中移除%s。", matchedDish.getName());
                    } else {
                        cart.setNumber(cart.getNumber() - 1);
                        shoppingCartMapper.updateNumberById(cart);
                        return String.format("好的！已减少一份%s，现在购物车中还有%d份。", matchedDish.getName(), cart.getNumber());
                    }
                } else {
                    return String.format("抱歉，您的购物车中没有%s。", matchedDish.getName());
                }
            }
            
            return "抱歉，我没有找到您说的菜品。您可以告诉我具体的菜品名称，比如\"帮我减一份宫保鸡丁\"。";
            
        } catch (Exception e) {
            log.error("减菜异常", e);
            return "抱歉，减菜时出现异常，请稍后重试。";
        }
    }

    /**
     * 获取会话历史
     */
    @SuppressWarnings("unchecked")
    private List<Map<String, String>> getMessageHistory(String sessionId) {
        String key = "chat_session:" + sessionId;
        Object history = redisTemplate.opsForValue().get(key);

        if (history != null) {
            return (List<Map<String, String>>) history;
        }

        // 创建新的会话，添加系统提示词
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 获取菜单信息并添加到系统提示中
        String menuInfo = buildMenuContext();
        
        Map<String, String> systemMessage = new HashMap<>();
        systemMessage.put("role", "system");
        systemMessage.put("content", buildSystemPrompt(menuInfo));
        messages.add(systemMessage);

        return messages;
    }

    /**
     * 保存会话历史
     */
    private void saveMessageHistory(String sessionId, List<Map<String, String>> messages) {
        String key = "chat_session:" + sessionId;
        redisTemplate.opsForValue().set(key, messages, SESSION_EXPIRE_TIME, TimeUnit.MINUTES);
    }

    /**
     * 构建菜单上下文信息
     */
    private String buildMenuContext() {
        StringBuilder menuInfo = new StringBuilder();
        
        // 获取所有启用的菜品
        List<Dish> dishes = dishMapper.list(Dish.builder().status(1).build());
        menuInfo.append("\n【在售菜品】:\n");
        for (Dish dish : dishes) {
            // 查询销量
            Integer sales = orderDetailMapper.countDishSales(dish.getId());
            menuInfo.append(String.format("- %s (ID:%d): ¥%.2f, 已售%d份, %s\n",
                    dish.getName(),
                    dish.getId(),
                    dish.getPrice(),
                    sales,
                    dish.getDescription() != null ? dish.getDescription() : "无描述"));
        }

        // 获取所有启用的套餐
        List<Setmeal> setmeals = setmealMapper.list(Setmeal.builder().status(1).build());
        menuInfo.append("\n【在售套餐】:\n");
        for (Setmeal setmeal : setmeals) {
            // 查询销量
            Integer sales = orderDetailMapper.countSetmealSales(setmeal.getId());
            menuInfo.append(String.format("- %s (ID:%d): ¥%.2f, 已售%d份, %s\n",
                    setmeal.getName(),
                    setmeal.getId(),
                    setmeal.getPrice(),
                    sales,
                    setmeal.getDescription() != null ? setmeal.getDescription() : "无描述"));
        }

        return menuInfo.toString();
    }

    /**
     * 构建系统提示词
     */
    private String buildSystemPrompt(String menuInfo) {
        return "你是一个智能餐厅客服助手，名叫'小厨助手'。你的职责是：\n" +
                "1. 热情友好地回答顾客关于菜品、套餐的咨询\n" +
                "2. 根据顾客的口味偏好推荐合适的菜品或套餐\n" +
                "3. 帮助顾客了解菜品的详细信息（价格、口味、特色、销量等）\n" +
                "4. 协助顾客完成点单流程\n" +
                "5. 回答关于订单、配送等问题\n\n" +
                "回答要求：\n" +
                "- 语气亲切自然，像朋友一样交流\n" +
                "- 简洁明了，不要过于冗长\n" +
                "- 主动询问顾客的喜好和需求\n" +
                "- 推荐时要说明推荐理由\n" +
                "- 如果顾客表达模糊，要主动澄清\n" +
                "- 推荐菜品时，请按照格式标注：[推荐菜品:ID] 或 [推荐套餐:ID]\n" +
                "- 可以根据销量数据推荐热销菜品\n\n" +
                "当前菜单信息（包含销量）：\n" + menuInfo + "\n\n" +
                "注意：你只能提供建议和推荐，不能直接操作订单，需要引导顾客在界面上完成操作。";
    }

    /**
     * 分析AI回复中的推荐信息
     */
    private ChatMessageVO analyzeRecommendation(String reply, String sessionId) {
        ChatMessageVO vo = ChatMessageVO.builder()
                .reply(reply)
                .sessionId(sessionId)
                .hasRecommendation(false)
                .build();

        // 匹配推荐菜品：[推荐菜品:ID]
        Pattern dishPattern = Pattern.compile("\\[推荐菜品:(\\d+)\\]");
        Matcher dishMatcher = dishPattern.matcher(reply);
        List<Long> dishIds = new ArrayList<>();
        while (dishMatcher.find()) {
            dishIds.add(Long.parseLong(dishMatcher.group(1)));
        }

        // 匹配推荐套餐：[推荐套餐:ID]
        Pattern setmealPattern = Pattern.compile("\\[推荐套餐:(\\d+)\\]");
        Matcher setmealMatcher = setmealPattern.matcher(reply);
        List<Long> setmealIds = new ArrayList<>();
        while (setmealMatcher.find()) {
            setmealIds.add(Long.parseLong(setmealMatcher.group(1)));
        }

        // 设置推荐信息
        if (!dishIds.isEmpty()) {
            vo.setHasRecommendation(true);
            vo.setRecommendedIds(dishIds);
            vo.setRecommendationType("dish");
        } else if (!setmealIds.isEmpty()) {
            vo.setHasRecommendation(true);
            vo.setRecommendedIds(setmealIds);
            vo.setRecommendationType("setmeal");
        }

        return vo;
    }
}
