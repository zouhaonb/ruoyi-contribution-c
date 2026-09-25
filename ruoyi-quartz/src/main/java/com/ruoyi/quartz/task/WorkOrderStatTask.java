package com.ruoyi.quartz.task;

import java.util.Date;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.system.mapper.HpWorkOrderMapper;
import com.ruoyi.common.utils.DateUtils;

/**
 * 工单统计定时任务
 * 每天凌晨1点执行，统计昨日工单数据
 *
 * @author ruoyi
 */
@Component("workOrderStatTask")
public class WorkOrderStatTask
{
    @Autowired
    private HpWorkOrderMapper hpWorkOrderMapper;

    /**
     * 执行工单统计任务
     * 统计昨日数据，按日维度写入统计表
     */
    public void execute()
    {
        // 统计昨日日期
        String statDate = DateUtils.parseDateToStr("yyyy-MM-dd", DateUtils.addDays(new Date(), -1));

        // 按分类汇总统计
        hpWorkOrderMapper.statWorkOrderByCategory(statDate);
    }

    /**
     * 手动触发统计（供管理员调用）
     * @param statDate 统计日期
     */
    public void executeForDate(String statDate)
    {
        hpWorkOrderMapper.statWorkOrderByCategory(statDate);
    }
}
