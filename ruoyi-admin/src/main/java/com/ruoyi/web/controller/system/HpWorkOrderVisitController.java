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
import org.springframework.web.bind.annotation.RestController;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.HpWorkOrder;
import com.ruoyi.system.domain.HpWorkOrderVisit;
import com.ruoyi.system.service.IHpWorkOrderService;
import com.ruoyi.system.service.IHpWorkOrderVisitService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 工单回访记录Controller
 * 
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/visit")
public class HpWorkOrderVisitController extends BaseController
{
    @Autowired
    private IHpWorkOrderVisitService hpWorkOrderVisitService;

    @Autowired
    private IHpWorkOrderService hpWorkOrderService;

    /**
     * 查询工单回访记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:visit:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpWorkOrderVisit hpWorkOrderVisit)
    {
        startPage();
        List<HpWorkOrderVisit> list = hpWorkOrderVisitService.selectHpWorkOrderVisitList(hpWorkOrderVisit);
        return getDataTable(list);
    }

    /**
     * 导出工单回访记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:visit:export')")
    @Log(title = "工单回访记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpWorkOrderVisit hpWorkOrderVisit)
    {
        List<HpWorkOrderVisit> list = hpWorkOrderVisitService.selectHpWorkOrderVisitList(hpWorkOrderVisit);
        ExcelUtil<HpWorkOrderVisit> util = new ExcelUtil<HpWorkOrderVisit>(HpWorkOrderVisit.class);
        util.exportExcel(response, list, "工单回访记录数据");
    }

    /**
     * 获取工单回访记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:visit:query')")
    @GetMapping(value = "/{visitId}")
    public AjaxResult getInfo(@PathVariable("visitId") Long visitId)
    {
        return success(hpWorkOrderVisitService.selectHpWorkOrderVisitByVisitId(visitId));
    }

    /**
     * 新增工单回访记录
     */
    @PreAuthorize("@ss.hasPermi('system:visit:add')")
    @Log(title = "工单回访记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpWorkOrderVisit hpWorkOrderVisit)
    {
        return toAjax(hpWorkOrderVisitService.insertHpWorkOrderVisit(hpWorkOrderVisit));
    }

    /**
     * 修改工单回访记录
     */
    @PreAuthorize("@ss.hasPermi('system:visit:edit')")
    @Log(title = "工单回访记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpWorkOrderVisit hpWorkOrderVisit)
    {
        int result = hpWorkOrderVisitService.updateHpWorkOrderVisit(hpWorkOrderVisit);

        // 联动更新工单主表状态：已回访/无法回访 → 已完成
        if (result > 0 && ("1".equals(hpWorkOrderVisit.getVisitStatus()) || "2".equals(hpWorkOrderVisit.getVisitStatus()))) {
            HpWorkOrder order = new HpWorkOrder();
            order.setWorkOrderId(hpWorkOrderVisit.getWorkOrderId());
            order.setStatus("3"); // 已完成
            order.setUpdateBy(SecurityUtils.getUsername());
            hpWorkOrderService.updateHpWorkOrder(order);
        }

        return toAjax(result);
    }

    /**
     * 删除工单回访记录
     */
    @PreAuthorize("@ss.hasPermi('system:visit:remove')")
    @Log(title = "工单回访记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{visitIds}")
    public AjaxResult remove(@PathVariable Long[] visitIds)
    {
        return toAjax(hpWorkOrderVisitService.deleteHpWorkOrderVisitByVisitIds(visitIds));
    }
}
