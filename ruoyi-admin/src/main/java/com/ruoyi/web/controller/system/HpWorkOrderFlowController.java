package com.ruoyi.web.controller.system;

import java.util.Date;
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
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.system.domain.HpWorkOrder;
import com.ruoyi.system.domain.HpWorkOrderFlow;
import com.ruoyi.system.service.IHpWorkOrderFlowService;
import com.ruoyi.system.service.IHpWorkOrderService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 工单流转记录Controller
 * 
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/flow")
public class HpWorkOrderFlowController extends BaseController
{
    @Autowired
    private IHpWorkOrderFlowService hpWorkOrderFlowService;

    @Autowired
    private IHpWorkOrderService hpWorkOrderService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询工单流转记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:flow:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpWorkOrderFlow hpWorkOrderFlow)
    {
        startPage();
        List<HpWorkOrderFlow> list = hpWorkOrderFlowService.selectHpWorkOrderFlowList(hpWorkOrderFlow);
        return getDataTable(list);
    }

    /**
     * 导出工单流转记录列表
     */
    @PreAuthorize("@ss.hasPermi('system:flow:export')")
    @Log(title = "工单流转记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpWorkOrderFlow hpWorkOrderFlow)
    {
        List<HpWorkOrderFlow> list = hpWorkOrderFlowService.selectHpWorkOrderFlowList(hpWorkOrderFlow);
        ExcelUtil<HpWorkOrderFlow> util = new ExcelUtil<HpWorkOrderFlow>(HpWorkOrderFlow.class);
        util.exportExcel(response, list, "工单流转记录数据");
    }

    /**
     * 获取工单流转记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:flow:query')")
    @GetMapping(value = "/{flowId}")
    public AjaxResult getInfo(@PathVariable("flowId") Long flowId)
    {
        return success(hpWorkOrderFlowService.selectHpWorkOrderFlowByFlowId(flowId));
    }

    /**
     * 新增工单流转记录
     */
    @PreAuthorize("@ss.hasPermi('system:flow:add')")
    @Log(title = "工单流转记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpWorkOrderFlow hpWorkOrderFlow)
    {
        // 校验工单ID
        if (hpWorkOrderFlow.getWorkOrderId() == null)
        {
            return error("工单ID不能为空");
        }

        // 查询工单信息
        HpWorkOrder workOrder = hpWorkOrderService.selectHpWorkOrderByWorkOrderId(hpWorkOrderFlow.getWorkOrderId());
        if (workOrder == null)
        {
            return error("工单不存在");
        }

        // 校验工单状态：仅待分配(0)或处理中(1)允许转派
        if (!"0".equals(workOrder.getStatus()) && !"1".equals(workOrder.getStatus()))
        {
            return error("仅待分配或处理中的工单允许转派");
        }

        // 校验流转原因必填
        if (StringUtils.isEmpty(hpWorkOrderFlow.getTransferReason()))
        {
            return error("流转原因不能为空");
        }

        // 校验新处理人不能和原处理人相同
        if (workOrder.getHandlerId() != null && workOrder.getHandlerId().equals(hpWorkOrderFlow.getToHandlerId()))
        {
            return error("新处理人不能与原处理人相同");
        }

        // 自动设置操作人信息（从当前登录用户获取）
        hpWorkOrderFlow.setOperatorId(SecurityUtils.getLoginUser().getUser().getUserId());
        hpWorkOrderFlow.setOperatorName(SecurityUtils.getLoginUser().getUser().getNickName());
        hpWorkOrderFlow.setOperateTime(new Date());

        // 回填原处理人信息
        hpWorkOrderFlow.setFromHandlerId(workOrder.getHandlerId());
        hpWorkOrderFlow.setFromHandlerName(workOrder.getHandlerName());

        // 设置新处理人姓名
        if (hpWorkOrderFlow.getToHandlerId() != null)
        {
            SysUser newHandler = userService.selectUserById(hpWorkOrderFlow.getToHandlerId());
            if (newHandler != null)
            {
                hpWorkOrderFlow.setToHandlerName(newHandler.getNickName() != null ? newHandler.getNickName() : newHandler.getUserName());
            }
        }

        // 插入流转记录
        int rows = hpWorkOrderFlowService.insertHpWorkOrderFlow(hpWorkOrderFlow);

        // 更新工单的处理人
        if (rows > 0 && hpWorkOrderFlow.getToHandlerId() != null)
        {
            // 根据新处理人ID查询用户信息
            SysUser newHandler = userService.selectUserById(hpWorkOrderFlow.getToHandlerId());
            String newHandlerName = newHandler != null ?
                (newHandler.getNickName() != null ? newHandler.getNickName() : newHandler.getUserName()) : null;

            HpWorkOrder updateOrder = new HpWorkOrder();
            updateOrder.setWorkOrderId(hpWorkOrderFlow.getWorkOrderId());
            updateOrder.setHandlerId(hpWorkOrderFlow.getToHandlerId());
            updateOrder.setHandlerName(newHandlerName);
            updateOrder.setUpdateBy(SecurityUtils.getUsername());
            hpWorkOrderService.updateHpWorkOrder(updateOrder);
        }

        return toAjax(rows);
    }

    /**
     * 修改工单流转记录
     */
    @PreAuthorize("@ss.hasPermi('system:flow:edit')")
    @Log(title = "工单流转记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpWorkOrderFlow hpWorkOrderFlow)
    {
        return toAjax(hpWorkOrderFlowService.updateHpWorkOrderFlow(hpWorkOrderFlow));
    }

    /**
     * 删除工单流转记录
     */
    @PreAuthorize("@ss.hasPermi('system:flow:remove')")
    @Log(title = "工单流转记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{flowIds}")
    public AjaxResult remove(@PathVariable Long[] flowIds)
    {
        return toAjax(hpWorkOrderFlowService.deleteHpWorkOrderFlowByFlowIds(flowIds));
    }
}
