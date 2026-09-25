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
import com.ruoyi.system.domain.HpAiChatSession;
import com.ruoyi.system.service.IHpAiChatSessionService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * AI聊天会话Controller
 * 
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/session")
public class HpAiChatSessionController extends BaseController
{
    @Autowired
    private IHpAiChatSessionService hpAiChatSessionService;

    /**
     * 查询AI聊天会话列表
     */
    @PreAuthorize("@ss.hasPermi('system:session:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpAiChatSession hpAiChatSession)
    {
        startPage();
        List<HpAiChatSession> list = hpAiChatSessionService.selectHpAiChatSessionList(hpAiChatSession);
        return getDataTable(list);
    }

    /**
     * 导出AI聊天会话列表
     */
    @PreAuthorize("@ss.hasPermi('system:session:export')")
    @Log(title = "AI聊天会话", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpAiChatSession hpAiChatSession)
    {
        List<HpAiChatSession> list = hpAiChatSessionService.selectHpAiChatSessionList(hpAiChatSession);
        ExcelUtil<HpAiChatSession> util = new ExcelUtil<HpAiChatSession>(HpAiChatSession.class);
        util.exportExcel(response, list, "AI聊天会话数据");
    }

    /**
     * 获取AI聊天会话详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:session:query')")
    @GetMapping(value = "/{sessionId}")
    public AjaxResult getInfo(@PathVariable("sessionId") Long sessionId)
    {
        return success(hpAiChatSessionService.selectHpAiChatSessionBySessionId(sessionId));
    }

    /**
     * 新增AI聊天会话
     */
    @PreAuthorize("@ss.hasPermi('system:session:add')")
    @Log(title = "AI聊天会话", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpAiChatSession hpAiChatSession)
    {
        return toAjax(hpAiChatSessionService.insertHpAiChatSession(hpAiChatSession));
    }

    /**
     * 修改AI聊天会话
     */
    @PreAuthorize("@ss.hasPermi('system:session:edit')")
    @Log(title = "AI聊天会话", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpAiChatSession hpAiChatSession)
    {
        return toAjax(hpAiChatSessionService.updateHpAiChatSession(hpAiChatSession));
    }

    /**
     * 删除AI聊天会话
     */
    @PreAuthorize("@ss.hasPermi('system:session:remove')")
    @Log(title = "AI聊天会话", businessType = BusinessType.DELETE)
	@DeleteMapping("/{sessionIds}")
    public AjaxResult remove(@PathVariable Long[] sessionIds)
    {
        return toAjax(hpAiChatSessionService.deleteHpAiChatSessionBySessionIds(sessionIds));
    }
}
