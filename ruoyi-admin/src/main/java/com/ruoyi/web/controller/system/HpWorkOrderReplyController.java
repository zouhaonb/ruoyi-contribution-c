package com.ruoyi.web.controller.system;

import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.common.annotation.Log;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.core.controller.BaseController;
import com.ruoyi.common.core.domain.AjaxResult;
import com.ruoyi.common.core.domain.entity.SysUser;
import com.ruoyi.common.enums.BusinessType;
import com.ruoyi.common.utils.SecurityUtils;
import com.ruoyi.common.utils.uuid.UUID;
import com.ruoyi.system.domain.HpWorkOrderReply;
import com.ruoyi.system.service.IHpWorkOrderReplyService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 工单回复Controller
 *
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/reply")
public class HpWorkOrderReplyController extends BaseController
{
    @Autowired
    private IHpWorkOrderReplyService hpWorkOrderReplyService;

    /**
     * 查询工单回复列表
     */
    @PreAuthorize("@ss.hasPermi('system:reply:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpWorkOrderReply hpWorkOrderReply)
    {
        startPage();
        List<HpWorkOrderReply> list = hpWorkOrderReplyService.selectHpWorkOrderReplyList(hpWorkOrderReply);
        return getDataTable(list);
    }

    /**
     * 获取某个工单的所有回复（用于工单详情内的对话区域）
     */
    @PreAuthorize("@ss.hasPermi('system:reply:list')")
    @GetMapping("/listByWorkOrder/{workOrderId}")
    public AjaxResult listByWorkOrder(@PathVariable Long workOrderId)
    {
        List<HpWorkOrderReply> list = hpWorkOrderReplyService.selectHpWorkOrderReplyListByWorkOrderId(workOrderId);
        return success(list);
    }

    /**
     * 导出工单回复列表
     */
    @PreAuthorize("@ss.hasPermi('system:reply:export')")
    @Log(title = "工单回复", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpWorkOrderReply hpWorkOrderReply)
    {
        List<HpWorkOrderReply> list = hpWorkOrderReplyService.selectHpWorkOrderReplyList(hpWorkOrderReply);
        ExcelUtil<HpWorkOrderReply> util = new ExcelUtil<HpWorkOrderReply>(HpWorkOrderReply.class);
        util.exportExcel(response, list, "工单回复数据");
    }

    /**
     * 获取工单回复详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:reply:query')")
    @GetMapping(value = "/{replyId}")
    public AjaxResult getInfo(@PathVariable("replyId") Long replyId)
    {
        return success(hpWorkOrderReplyService.selectHpWorkOrderReplyByReplyId(replyId));
    }

    /**
     * 新增工单回复
     */
    @PreAuthorize("@ss.hasPermi('system:reply:add')")
    @Log(title = "工单回复", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpWorkOrderReply hpWorkOrderReply)
    {
        // 自动填充回复人信息
        SysUser currentUser = SecurityUtils.getLoginUser().getUser();
        hpWorkOrderReply.setReplyerId(currentUser.getUserId());
        hpWorkOrderReply.setReplyerName(currentUser.getNickName() != null ? currentUser.getNickName() : currentUser.getUserName());
        // 回复人类型：1=客服/医生（系统用户）
        hpWorkOrderReply.setReplyerType("1");
        // 回复类型：1=客服回复
        hpWorkOrderReply.setReplyType("1");
        // 默认公开
        if (hpWorkOrderReply.getIsPublic() == null) {
            hpWorkOrderReply.setIsPublic("1");
        }
        // 设置删除标志（0=存在）
        hpWorkOrderReply.setDelFlag("0");
        // 设置创建人
        hpWorkOrderReply.setCreateBy(SecurityUtils.getUsername());
        return toAjax(hpWorkOrderReplyService.insertHpWorkOrderReply(hpWorkOrderReply));
    }

    /**
     * 修改工单回复（一般不允许修改，仅管理员可用）
     */
    @PreAuthorize("@ss.hasPermi('system:reply:edit')")
    @Log(title = "工单回复", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpWorkOrderReply hpWorkOrderReply)
    {
        hpWorkOrderReply.setUpdateBy(SecurityUtils.getUsername());
        return toAjax(hpWorkOrderReplyService.updateHpWorkOrderReply(hpWorkOrderReply));
    }

    /**
     * 删除工单回复（一般不允许删除，仅管理员可用）
     */
    @PreAuthorize("@ss.hasPermi('system:reply:remove')")
    @Log(title = "工单回复", businessType = BusinessType.DELETE)
    @DeleteMapping("/{replyIds}")
    public AjaxResult remove(@PathVariable Long[] replyIds)
    {
        return toAjax(hpWorkOrderReplyService.deleteHpWorkOrderReplyByReplyIds(replyIds));
    }

    /**
     * 上传工单回复图片/附件
     */
    @PreAuthorize("@ss.hasPermi('system:reply:add')")
    @Log(title = "工单回复文件上传", businessType = BusinessType.INSERT)
    @PostMapping("/upload")
    public AjaxResult upload(@RequestParam("file") MultipartFile file)
    {
        if (file.isEmpty())
        {
            return error("上传文件不能为空");
        }
        String fileName = file.getOriginalFilename();
        String filePath = RuoYiConfig.getUploadPath();
        String suffix = fileName.substring(fileName.lastIndexOf("."));

        // 生成新文件名防止冲突
        String newFileName = UUID.fastUUID().toString(true) + suffix;
        try
        {
            // 保存文件
            java.io.File destFile = new java.io.File(filePath, newFileName);
            file.transferTo(destFile);

            // 返回可访问的URL
            String url = "/profile/upload/" + newFileName;
            AjaxResult ajax = success();
            ajax.put("url", url);
            return ajax;
        }
        catch (IOException e)
        {
            return error("上传失败：" + e.getMessage());
        }
    }
}
