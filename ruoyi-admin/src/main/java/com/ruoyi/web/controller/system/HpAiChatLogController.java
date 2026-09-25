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
import com.ruoyi.system.domain.HpAiChatLog;
import com.ruoyi.system.service.IHpAiChatLogService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * AI聊天日志Controller
 * 
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/log")
public class HpAiChatLogController extends BaseController
{
    @Autowired
    private IHpAiChatLogService hpAiChatLogService;

    /**
     * 查询AI聊天日志列表
     */
    @PreAuthorize("@ss.hasPermi('system:log:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpAiChatLog hpAiChatLog)
    {
        startPage();
        List<HpAiChatLog> list = hpAiChatLogService.selectHpAiChatLogList(hpAiChatLog);
        return getDataTable(list);
    }

    /**
     * 导出AI聊天日志列表
     */
    @PreAuthorize("@ss.hasPermi('system:log:export')")
    @Log(title = "AI聊天日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpAiChatLog hpAiChatLog)
    {
        List<HpAiChatLog> list = hpAiChatLogService.selectHpAiChatLogList(hpAiChatLog);
        ExcelUtil<HpAiChatLog> util = new ExcelUtil<HpAiChatLog>(HpAiChatLog.class);
        util.exportExcel(response, list, "AI聊天日志数据");
    }

    /**
     * 获取AI聊天日志详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:log:query')")
    @GetMapping(value = "/{logId}")
    public AjaxResult getInfo(@PathVariable("logId") Long logId)
    {
        return success(hpAiChatLogService.selectHpAiChatLogByLogId(logId));
    }

    /**
     * 新增AI聊天日志
     */
    @PreAuthorize("@ss.hasPermi('system:log:add')")
    @Log(title = "AI聊天日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpAiChatLog hpAiChatLog)
    {
        return toAjax(hpAiChatLogService.insertHpAiChatLog(hpAiChatLog));
    }

    /**
     * 修改AI聊天日志
     */
    @PreAuthorize("@ss.hasPermi('system:log:edit')")
    @Log(title = "AI聊天日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpAiChatLog hpAiChatLog)
    {
        return toAjax(hpAiChatLogService.updateHpAiChatLog(hpAiChatLog));
    }

    /**
     * 删除AI聊天日志
     */
    @PreAuthorize("@ss.hasPermi('system:log:remove')")
    @Log(title = "AI聊天日志", businessType = BusinessType.DELETE)
	@DeleteMapping("/{logIds}")
    public AjaxResult remove(@PathVariable Long[] logIds)
    {
        return toAjax(hpAiChatLogService.deleteHpAiChatLogByLogIds(logIds));
    }
}
