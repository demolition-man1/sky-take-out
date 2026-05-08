package com.sky.service.impl;

import com.sky.dto.GoodsSalesDTO;
import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.mapper.UserMapper;
import com.sky.service.ReportService;
import com.sky.vo.OrderReportVO;
import com.sky.vo.SalesTop10ReportVO;
import com.sky.vo.TurnoverReportVO;
import com.sky.vo.UserReportVO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {
    @Autowired
    private OrderMapper orderMapper;
    @Autowired
    private UserMapper userMapper;

    private List<LocalDate> generateDateList(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }
        return dateList;
    }

    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = generateDateList(begin, end);
        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX);

        List<Map<String, Object>> turnoverListFromDB = orderMapper.sumByDateRange(beginTime, endTime, Orders.COMPLETED);

        Map<String, Double> turnoverMap = new HashMap<>();
        for (Map<String, Object> map : turnoverListFromDB) {
            String dateKey = (String) map.get("dateKey");
            Double turnover = ((Number) map.get("turnover")).doubleValue();
            turnoverMap.put(dateKey, turnover);
        }

        List<Double> turnOverList = new ArrayList<>();
        for (LocalDate localDate : dateList) {
            String dateKey = localDate.toString();
            Double turnover = turnoverMap.getOrDefault(dateKey, 0.0);
            turnOverList.add(turnover);
        }
        return TurnoverReportVO.builder().
                dateList(StringUtils.join(dateList,",")).
                turnoverList(StringUtils.join(turnOverList,",")).
                build();
    }

    @Override
    public UserReportVO getUserStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = generateDateList(begin, end);
        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX);
        
        List<Map<String, Object>> newUserCountMap = userMapper.sumNewUser(beginTime, endTime);
        
        Map<String, Integer> newUserMap = new HashMap<>();
        for (Map<String, Object> map : newUserCountMap) {
            String dateKey = (String) map.get("dateKey");
            Integer count = ((Number) map.get("total")).intValue();
            newUserMap.put(dateKey, count);
        }

        // 查询截止到结束时间的总用户数
        Integer endTotalUser = userMapper.getTotalUserCount(endTime);
        if (endTotalUser == null) {
            endTotalUser = 0;
        }

        List<Integer> newUserList = new ArrayList<>();
        List<Integer> totalUserList = new ArrayList<>();
        
        // 计算查询期间的新用户总数
        Integer periodNewUser = 0;
        for (LocalDate localDate : dateList) {
            String dateKey = localDate.toString();
            Integer newUserCount = newUserMap.getOrDefault(dateKey, 0);
            newUserList.add(newUserCount);
            periodNewUser += newUserCount;
        }
        
        // 计算起始日之前的用户数
        Integer startTotalUser = endTotalUser - periodNewUser;
        
        // 生成每天的累计用户数
        Integer currentTotal = startTotalUser;
        for (int i = 0; i < dateList.size(); i++) {
            currentTotal += newUserList.get(i);
            totalUserList.add(currentTotal);
        }
        
         return UserReportVO.builder().
                 dateList(StringUtils.join(dateList,",")).
                 newUserList(StringUtils.join(newUserList,",")).
                 totalUserList(StringUtils.join(totalUserList,",")).
                 build();

    }

    @Override
    public OrderReportVO getOrderStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)) {
            begin = begin.plusDays(1);
            dateList.add(begin);
        }

        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX);

        List<Map<String, Object>> orderCountMap = orderMapper.countByDateRange(beginTime, endTime);
        List<Map<String, Object>> validOrderCountMap = orderMapper.countValidByDateRange(beginTime, endTime);

        Map<String, Integer> orderCountByDate = new HashMap<>();
        for (Map<String, Object> map : orderCountMap) {
            String dateKey = (String) map.get("dateKey");
            Integer count = ((Number) map.get("count")).intValue();
            orderCountByDate.put(dateKey, count);
        }

        Map<String, Integer> validOrderCountByDate = new HashMap<>();
        for (Map<String, Object> map : validOrderCountMap) {
            String dateKey = (String) map.get("dateKey");
            Integer count = ((Number) map.get("count")).intValue();
            validOrderCountByDate.put(dateKey, count);
        }

        List<Integer> orderCountList = new ArrayList<>();
        List<Integer> validOrderCountList = new ArrayList<>();
        Integer totalOrderCount = 0;
        Integer validOrderCount = 0;

        for (LocalDate localDate : dateList) {
            String dateKey = localDate.toString();
            Integer orderCount = orderCountByDate.getOrDefault(dateKey, 0);
            Integer validOrderCountTemp = validOrderCountByDate.getOrDefault(dateKey, 0);
            
            orderCountList.add(orderCount);
            validOrderCountList.add(validOrderCountTemp);
            totalOrderCount += orderCount;
            validOrderCount += validOrderCountTemp;
        }

        Double orderCompletionRate = totalOrderCount == 0 ? 0.0 : (double) validOrderCount / totalOrderCount;

        return OrderReportVO.builder()
                .dateList(StringUtils.join(dateList, ","))
                .orderCountList(StringUtils.join(orderCountList, ","))
                .validOrderCountList(StringUtils.join(validOrderCountList, ","))
                .totalOrderCount(totalOrderCount)
                .validOrderCount(validOrderCount)
                .orderCompletionRate(orderCompletionRate)
                .build();
    }

    @Override
    public SalesTop10ReportVO getSalesTop10(LocalDate begin, LocalDate end) {
        LocalDateTime beginTime = LocalDateTime.of(begin, LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(end, LocalTime.MAX);
        List<String> names = new ArrayList<>();
        List<Integer> numbers = new ArrayList<>();
        List<GoodsSalesDTO> salesTop10 = orderMapper.getSalesTop10(beginTime, endTime);
        for (GoodsSalesDTO goodsSalesDTO : salesTop10) {
            names.add(goodsSalesDTO.getName());
            numbers.add(goodsSalesDTO.getNumber());
        }
        return SalesTop10ReportVO.builder()
                .nameList(StringUtils.join(names, ","))
                .numberList(StringUtils.join(numbers, ","))
                .build();
    }
}
