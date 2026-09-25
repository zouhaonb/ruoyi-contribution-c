package com.ruoyi.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.HpWorkOrderStatistics;
import com.ruoyi.system.service.IHpWorkOrderStatisticsService;
import com.ruoyi.system.service.IHpWorkOrderService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 工单统计Controller
 *
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/statistics")
public class HpWorkOrderStatisticsController extends BaseController
{
    @Autowired
    private IHpWorkOrderStatisticsService hpWorkOrderStatisticsService;

    @Autowired
    private IHpWorkOrderService hpWorkOrderService;

    /**
     * 查询工单统计列表（预计算数据）
     * 仅管理员可访问
     */
    @PreAuthorize("@ss.hasPermi('system:statistics:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpWorkOrderStatistics hpWorkOrderStatistics)
    {
        // 权限校验：仅管理员可访问统计功能
        if (!SecurityUtils.getLoginUser().getUser().isAdmin())
        {
            return getDataTable(null);
        }
        startPage();
        List<HpWorkOrderStatistics> list = hpWorkOrderStatisticsService.selectHpWorkOrderStatisticsList(hpWorkOrderStatistics);
        return getDataTable(list);
    }

    /**
     * 实时统计工单数据（绕过预计算表）
     * 仅管理员可访问
     */
    @PreAuthorize("@ss.hasPermi('system:statistics:list')")
    @GetMapping("/realtime")
    public AjaxResult realtimeStatistics(
            @RequestParam(required = false) String beginDate,
            @RequestParam(required = false) String endDate,
            @RequestParam(required = false) Long categoryId)
    {
        // 权限校验：仅管理员可访问
        if (!SecurityUtils.getLoginUser().getUser().isAdmin())
        {
            return error("无权限访问工单统计");
        }
        List<HpWorkOrderStatistics> list = hpWorkOrderService.selectWorkOrderRealtimeStatistics(beginDate, endDate, categoryId);
        return success(list);
    }

    /**
     * 获取统计概览数据（指标卡片用）
     */
    @PreAuthorize("@ss.hasPermi('system:statistics:list')")
    @GetMapping("/overview")
    public AjaxResult overview()
    {
        if (!SecurityUtils.getLoginUser().getUser().isAdmin())
        {
            return error("无权限访问工单统计");
        }
        return success(hpWorkOrderService.selectWorkOrderOverview());
    }

    /**
     * 导出工单统计列表
     */
    @PreAuthorize("@ss.hasPermi('system:statistics:export')")
    @Log(title = "工单统计", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpWorkOrderStatistics hpWorkOrderStatistics)
    {
        if (!SecurityUtils.getLoginUser().getUser().isAdmin())
        {
            return;
        }
        List<HpWorkOrderStatistics> list = hpWorkOrderStatisticsService.selectHpWorkOrderStatisticsList(hpWorkOrderStatistics);
        ExcelUtil<HpWorkOrderStatistics> util = new ExcelUtil<HpWorkOrderStatistics>(HpWorkOrderStatistics.class);
        util.exportExcel(response, list, "工单统计数据");
    }

    /**
     * 获取工单统计详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:statistics:query')")
    @GetMapping(value = "/{statId}")
    public AjaxResult getInfo(@PathVariable("statId") Long statId)
    {
        if (!SecurityUtils.getLoginUser().getUser().isAdmin())
        {
            return error("无权限访问工单统计");
        }
        return success(hpWorkOrderStatisticsService.selectHpWorkOrderStatisticsByStatId(statId));
    }

    /**
     * 新增工单统计
     */
    @PreAuthorize("@ss.hasPermi('system:statistics:add')")
    @Log(title = "工单统计", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpWorkOrderStatistics hpWorkOrderStatistics)
    {
        if (!SecurityUtils.getLoginUser().getUser().isAdmin())
        {
            return error("无权限访问工单统计");
        }
        return toAjax(hpWorkOrderStatisticsService.insertHpWorkOrderStatistics(hpWorkOrderStatistics));
    }

    /**
     * 修改工单统计
     */
    @PreAuthorize("@ss.hasPermi('system:statistics:edit')")
    @Log(title = "工单统计", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpWorkOrderStatistics hpWorkOrderStatistics)
    {
        if (!SecurityUtils.getLoginUser().getUser().isAdmin())
        {
            return error("无权限访问工单统计");
        }
        return toAjax(hpWorkOrderStatisticsService.updateHpWorkOrderStatistics(hpWorkOrderStatistics));
    }

    /**
     * 删除工单统计
     */
    @PreAuthorize("@ss.hasPermi('system:statistics:remove')")
    @Log(title = "工单统计", businessType = BusinessType.DELETE)
    @DeleteMapping("/{statIds}")
    public AjaxResult remove(@PathVariable Long[] statIds)
    {
        if (!SecurityUtils.getLoginUser().getUser().isAdmin())
        {
            return error("无权限访问工单统计");
        }
        return toAjax(hpWorkOrderStatisticsService.deleteHpWorkOrderStatisticsByStatIds(statIds));
    }
}
