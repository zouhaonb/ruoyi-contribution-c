package com.ruoyi.web.controller.system;

import java.util.List;
import java.util.stream.Collectors;
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
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.system.domain.HpWorkOrder;
import com.ruoyi.system.domain.HpWorkOrderVisit;
import com.ruoyi.system.service.IHpWorkOrderService;
import com.ruoyi.system.service.IHpWorkOrderVisitService;
import com.ruoyi.system.service.ISysUserService;
import com.ruoyi.common.utils.poi.ExcelUtil;

/**
 * 工单主表Controller
 *
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/order")
public class HpWorkOrderController extends BaseController
{
    @Autowired
    private IHpWorkOrderService hpWorkOrderService;

    @Autowired
    private IHpWorkOrderVisitService hpWorkOrderVisitService;

    @Autowired
    private ISysUserService userService;

    /**
     * 查询工单主表列表
     */
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpWorkOrder hpWorkOrder)
    {
        startPage();
        List<HpWorkOrder> list = hpWorkOrderService.selectHpWorkOrderList(hpWorkOrder);
        return getDataTable(list);
    }

    /**
     * 导出工单主表列表
     */
    @PreAuthorize("@ss.hasPermi('system:order:export')")
    @Log(title = "工单主表", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpWorkOrder hpWorkOrder)
    {
        List<HpWorkOrder> list = hpWorkOrderService.selectHpWorkOrderList(hpWorkOrder);
        ExcelUtil<HpWorkOrder> util = new ExcelUtil<HpWorkOrder>(HpWorkOrder.class);
        util.exportExcel(response, list, "工单主表数据");
    }

    /**
     * 获取工单主表详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:order:query')")
    @GetMapping(value = "/{workOrderId}")
    public AjaxResult getInfo(@PathVariable("workOrderId") Long workOrderId)
    {
        return success(hpWorkOrderService.selectHpWorkOrderByWorkOrderId(workOrderId));
    }

    /**
     * 新增工单主表
     */
    @PreAuthorize("@ss.hasPermi('system:order:add')")
    @Log(title = "工单主表", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpWorkOrder hpWorkOrder)
    {
        // 设置创建人
        hpWorkOrder.setCreateBy(SecurityUtils.getUsername());
        // 自动生成工单编号：WO + yyyyMMddHHmmss + 4位随机数
        String orderNo = "WO" + new java.text.SimpleDateFormat("yyyyMMddHHmmss").format(new java.util.Date())
                + String.format("%04d", new java.util.Random().nextInt(10000));
        hpWorkOrder.setOrderNo(orderNo);
        return toAjax(hpWorkOrderService.insertHpWorkOrder(hpWorkOrder));
    }

    /**
     * 修改工单主表
     */
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "工单主表", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpWorkOrder hpWorkOrder)
    {
        // 设置更新人
        hpWorkOrder.setUpdateBy(SecurityUtils.getUsername());

        // 获取原工单信息，用于判断状态变更
        HpWorkOrder originalOrder = hpWorkOrderService.selectHpWorkOrderByWorkOrderId(hpWorkOrder.getWorkOrderId());
        String originalStatus = originalOrder != null ? originalOrder.getStatus() : null;

        // 业务校验：归档时间只能在工单状态为"已归档"(4)时才能设置
        if (hpWorkOrder.getStatus() != null && !"4".equals(hpWorkOrder.getStatus())) {
            // 非归档状态，清空归档时间，防止数据异常
            hpWorkOrder.setArchiveTime(null);
            hpWorkOrder.setArchiveRemark(null);
        } else if ("4".equals(hpWorkOrder.getStatus()) && !"4".equals(originalStatus)) {
            // 状态变更为已归档时，自动设置归档时间
            hpWorkOrder.setArchiveTime(new java.util.Date());
        }

        return toAjax(hpWorkOrderService.updateHpWorkOrder(hpWorkOrder));
    }

    /**
     * 删除工单主表
     */
    @PreAuthorize("@ss.hasPermi('system:order:remove')")
    @Log(title = "工单主表", businessType = BusinessType.DELETE)
    @DeleteMapping("/{workOrderIds}")
    public AjaxResult remove(@PathVariable Long[] workOrderIds)
    {
        return toAjax(hpWorkOrderService.deleteHpWorkOrderByWorkOrderIds(workOrderIds));
    }

    /**
     * 查询患者账号列表（用于选择患者）
     * 根据用户昵称、账号或手机号搜索
     */
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/patient/list")
    public TableDataInfo listPatients(SysUser user)
    {
        startPage();
        // 设置角色ID为3（患者角色），如果不需要按角色过滤可以移除
        // user.setRoleId(3L);
        // 同时支持userName（账号/姓名）和phonenumber（手机号）搜索
        List<SysUser> list = userService.selectUserList(user);
        return getDataTable(list);
    }

    /**
     * 根据用户ID获取用户信息（用于回填患者信息）
     */
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/patient/{userId}")
    public AjaxResult getPatientInfo(@PathVariable("userId") Long userId)
    {
        SysUser user = userService.selectUserById(userId);
        if (user == null)
        {
            return error("患者不存在");
        }
        // 只返回需要的字段
        AjaxResult result = AjaxResult.success();
        result.put("userId", user.getUserId());
        result.put("userName", user.getUserName());
        result.put("nickName", user.getNickName());
        result.put("phonenumber", user.getPhonenumber());
        return result;
    }

    /**
     * 查询医生列表（用于分配工单处理人）
     * 根据用户昵称搜索，支持按角色过滤
     */
    @PreAuthorize("@ss.hasPermi('system:order:list')")
    @GetMapping("/doctor/list")
    public TableDataInfo listDoctors(SysUser user)
    {
        startPage();
        // 设置角色ID为2（医生角色），如果你的医生角色ID不是2，请修改此处
        user.setRoleId(2L);
        List<SysUser> list = userService.selectUserListByRoleId(user);
        return getDataTable(list);
    }

    /**
     * 提交回访（医生认为已处理完毕，等待患者确认）
     * 处理中(1) → 待回访(2)
     */
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "工单提交回访", businessType = BusinessType.UPDATE)
    @PutMapping("/submitVisit/{workOrderId}")
    public AjaxResult submitVisit(@PathVariable Long workOrderId)
    {
        HpWorkOrder order = hpWorkOrderService.selectHpWorkOrderByWorkOrderId(workOrderId);
        if (order == null) {
            return error("工单不存在");
        }
        if (!"1".equals(order.getStatus())) {
            return error("只有处理中的工单才能提交回访");
        }
        // 校验权限：当前用户必须是处理医生或管理员
        Long currentUserId = SecurityUtils.getLoginUser().getUser().getUserId();
        boolean isAdmin = SecurityUtils.getLoginUser().getUser().isAdmin();
        if (!isAdmin && !currentUserId.equals(order.getHandlerId())) {
            return error("无权操作：您不是该工单的处理医生");
        }

        HpWorkOrder updateOrder = new HpWorkOrder();
        updateOrder.setWorkOrderId(workOrderId);
        updateOrder.setStatus("2"); // 待回访
        updateOrder.setUpdateBy(SecurityUtils.getUsername());

        // 自动创建回访记录
        HpWorkOrderVisit visitRecord = new HpWorkOrderVisit();
        visitRecord.setWorkOrderId(workOrderId);
        visitRecord.setVisitStatus("0"); // 待回访
        visitRecord.setVisitorId(currentUserId);
        visitRecord.setVisitorName(SecurityUtils.getLoginUser().getUser().getNickName());
        visitRecord.setDeptId(SecurityUtils.getLoginUser().getUser().getDeptId());
        visitRecord.setCreateBy(SecurityUtils.getUsername());
        hpWorkOrderVisitService.insertHpWorkOrderVisit(visitRecord);

        return toAjax(hpWorkOrderService.updateHpWorkOrder(updateOrder));
    }

    /**
     * 办结工单（患者确认或超时自动触发）
     * 待回访(2) → 已完成(3)
     */
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "工单办结", businessType = BusinessType.UPDATE)
    @PutMapping("/complete/{workOrderId}")
    public AjaxResult complete(@PathVariable Long workOrderId, @RequestBody HpWorkOrder order)
    {
        HpWorkOrder existingOrder = hpWorkOrderService.selectHpWorkOrderByWorkOrderId(workOrderId);
        if (existingOrder == null) {
            return error("工单不存在");
        }
        // 校验状态：只能是待回访(2)状态才能办结
        if (!"2".equals(existingOrder.getStatus())) {
            return error("只有待回访状态的工单才能办结");
        }

        HpWorkOrder updateOrder = new HpWorkOrder();
        updateOrder.setWorkOrderId(workOrderId);
        updateOrder.setStatus("3"); // 已完成
        updateOrder.setCompleteTime(new java.util.Date());
        if (order != null && order.getSatisfaction() != null) {
            updateOrder.setSatisfaction(order.getSatisfaction());
        }
        updateOrder.setUpdateBy(SecurityUtils.getUsername());

        // 同步更新关联的回访记录状态
        HpWorkOrderVisit visit = hpWorkOrderVisitService.selectHpWorkOrderVisitByWorkOrderId(workOrderId);
        if (visit != null) {
            HpWorkOrderVisit updateVisit = new HpWorkOrderVisit();
            updateVisit.setVisitId(visit.getVisitId());
            updateVisit.setVisitStatus("1"); // 已回访
            updateVisit.setUpdateBy(SecurityUtils.getUsername());
            hpWorkOrderVisitService.updateHpWorkOrderVisit(updateVisit);
        }

        return toAjax(hpWorkOrderService.updateHpWorkOrder(updateOrder));
    }

    /**
     * 重新打开工单（管理员权限）
     * 已完成(3) → 处理中(1)
     */
    @PreAuthorize("@ss.hasPermi('system:order:edit')")
    @Log(title = "工单重新打开", businessType = BusinessType.UPDATE)
    @PutMapping("/reopen/{workOrderId}")
    public AjaxResult reopen(@PathVariable Long workOrderId)
    {
        // 只有管理员可以重新打开工单
        if (!SecurityUtils.getLoginUser().getUser().isAdmin()) {
            return error("只有管理员可以重新打开工单");
        }
        HpWorkOrder order = hpWorkOrderService.selectHpWorkOrderByWorkOrderId(workOrderId);
        if (order == null) {
            return error("工单不存在");
        }
        if (!"3".equals(order.getStatus())) {
            return error("只能重新打开已完成的工单");
        }

        HpWorkOrder updateOrder = new HpWorkOrder();
        updateOrder.setWorkOrderId(workOrderId);
        updateOrder.setStatus("1"); // 处理中
        updateOrder.setCompleteTime(null); // 清空完成时间
        updateOrder.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(hpWorkOrderService.updateHpWorkOrder(updateOrder));
    }
}
