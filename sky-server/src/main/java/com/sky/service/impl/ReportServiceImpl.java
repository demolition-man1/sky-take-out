package com.sky.service.impl;

import com.sky.entity.Orders;
import com.sky.mapper.OrderMapper;
import com.sky.service.ReportService;
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
    /**
     * 营业额统计
     * @param begin
     * @param end
     * @return
     */
    @Override
    public TurnoverReportVO getTurnoverStatistics(LocalDate begin, LocalDate end) {
        List<LocalDate> dateList = new ArrayList<>();
        dateList.add(begin);
        while (!begin.equals(end)){
        begin=begin.plusDays(1);
        dateList.add( begin);
        }
        //方案一：在for循环里面调用sql
    // 存放每天的营业额
//        List<Double> turnOverList = new ArrayList<>();
//        for (LocalDate localDate : dateList) {
//            // 查询date日期对应的营业额数据，营业额是指：订单状态为“已完成”的订单金额总和
//            LocalDateTime beginTime = LocalDateTime.of(localDate, LocalTime.MIN);
//            LocalDateTime endTime = LocalDateTime.of(localDate, LocalTime.MAX);
//
//            Double turnover = orderMapper.sumByMap(beginTime, endTime, Orders.COMPLETED);
            // 如果营业额为null，则设置为0.0
//            turnover = turnover == null ? 0.0 : turnover;
//            turnOverList.add(turnover);
//        }
        //方案二：一次性查询所有日期范围内的订单数据，然后在内存中按日期分组统计，返回一个Map，key是日期字符串，value是营业额
        LocalDateTime beginTime = LocalDateTime.of(dateList.get(0), LocalTime.MIN);
        LocalDateTime endTime = LocalDateTime.of(dateList.get(dateList.size() - 1), LocalTime.MAX);

        List<Map<String, Object>> turnoverListFromDB = orderMapper.sumByDateRange(beginTime, endTime, Orders.COMPLETED);

        // 将查询结果转换为 Map<String, Double>
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

        return null;
    }
}
