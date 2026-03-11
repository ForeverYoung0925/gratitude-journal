package com.gratitude.app.controller.admin;

import cn.dev33.satoken.annotation.SaCheckLogin;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gratitude.app.dto.admin.AdminStatVO;
import com.gratitude.app.entity.Diary;
import com.gratitude.app.entity.Order;
import com.gratitude.app.entity.User;
import com.gratitude.app.entity.VipRecord;
import com.gratitude.app.mapper.DiaryMapper;
import com.gratitude.app.mapper.OrderMapper;
import com.gratitude.app.mapper.UserMapper;
import com.gratitude.app.mapper.VipRecordMapper;
import com.gratitude.common.constants.AppConstants;
import com.gratitude.common.result.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin/stat")
@Api(tags = "管理后台-数据大盘")
@SaCheckLogin(type = "admin") // 独立校验 admin Token
public class AdminStatController {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private DiaryMapper diaryMapper;

    @Autowired
    private VipRecordMapper vipRecordMapper;

    @Autowired
    private OrderMapper orderMapper;

    @GetMapping("/daily")
    @ApiOperation("查询今日数据大盘")
    public R<AdminStatVO> getDailyStat() {
        LocalDateTime todayStart = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);

        AdminStatVO stat = new AdminStatVO();

        // 1. 今日新增用户
        stat.setNewUsersToday(userMapper.selectCount(
                new LambdaQueryWrapper<User>().ge(User::getCreateTime, todayStart)));

        // 2. 今日新增日记
        stat.setNewDiariesToday(diaryMapper.selectCount(
                new LambdaQueryWrapper<Diary>().ge(Diary::getCreateTime, todayStart)));

        // 3. 今日新增特权发放/消费条数(VIP)
        stat.setNewVipsToday(vipRecordMapper.selectCount(
                new LambdaQueryWrapper<VipRecord>().ge(VipRecord::getCreateTime, todayStart)));

        // 4. 今日充值流水(累加支付金额)
        Long amount = orderMapper.selectList(
                new LambdaQueryWrapper<Order>()
                        .eq(Order::getStatus, AppConstants.ORDER_STATUS_PAID)
                        .ge(Order::getPaidTime, todayStart))
                .stream().mapToLong(Order::getPayAmount).sum();

        stat.setRechargeAmountToday(amount);

        return R.ok(stat);
    }
}
