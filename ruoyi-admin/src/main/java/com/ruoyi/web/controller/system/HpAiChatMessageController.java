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
import com.ruoyi.system.domain.HpAiChatMessage;
import com.ruoyi.system.service.IHpAiChatMessageService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * AI聊天消息Controller
 * 
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/message")
public class HpAiChatMessageController extends BaseController
{
    @Autowired
    private IHpAiChatMessageService hpAiChatMessageService;

    /**
     * 查询AI聊天消息列表
     */
    @PreAuthorize("@ss.hasPermi('system:message:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpAiChatMessage hpAiChatMessage)
    {
        startPage();
        List<HpAiChatMessage> list = hpAiChatMessageService.selectHpAiChatMessageList(hpAiChatMessage);
        return getDataTable(list);
    }

    /**
     * 导出AI聊天消息列表
     */
    @PreAuthorize("@ss.hasPermi('system:message:export')")
    @Log(title = "AI聊天消息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpAiChatMessage hpAiChatMessage)
    {
        List<HpAiChatMessage> list = hpAiChatMessageService.selectHpAiChatMessageList(hpAiChatMessage);
        ExcelUtil<HpAiChatMessage> util = new ExcelUtil<HpAiChatMessage>(HpAiChatMessage.class);
        util.exportExcel(response, list, "AI聊天消息数据");
    }

    /**
     * 获取AI聊天消息详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:message:query')")
    @GetMapping(value = "/{messageId}")
    public AjaxResult getInfo(@PathVariable("messageId") Long messageId)
    {
        return success(hpAiChatMessageService.selectHpAiChatMessageByMessageId(messageId));
    }

    /**
     * 新增AI聊天消息
     */
    @PreAuthorize("@ss.hasPermi('system:message:add')")
    @Log(title = "AI聊天消息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpAiChatMessage hpAiChatMessage)
    {
        return toAjax(hpAiChatMessageService.insertHpAiChatMessage(hpAiChatMessage));
    }

    /**
     * 修改AI聊天消息
     */
    @PreAuthorize("@ss.hasPermi('system:message:edit')")
    @Log(title = "AI聊天消息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpAiChatMessage hpAiChatMessage)
    {
        return toAjax(hpAiChatMessageService.updateHpAiChatMessage(hpAiChatMessage));
    }

    /**
     * 删除AI聊天消息
     */
    @PreAuthorize("@ss.hasPermi('system:message:remove')")
    @Log(title = "AI聊天消息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{messageIds}")
    public AjaxResult remove(@PathVariable Long[] messageIds)
    {
        return toAjax(hpAiChatMessageService.deleteHpAiChatMessageByMessageIds(messageIds));
    }
}
