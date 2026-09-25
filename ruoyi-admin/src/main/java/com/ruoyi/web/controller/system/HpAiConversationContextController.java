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
import com.ruoyi.system.domain.HpAiConversationContext;
import com.ruoyi.system.service.IHpAiConversationContextService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * AI对话上下文Controller
 * 
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/context")
public class HpAiConversationContextController extends BaseController
{
    @Autowired
    private IHpAiConversationContextService hpAiConversationContextService;

    /**
     * 查询AI对话上下文列表
     */
    @PreAuthorize("@ss.hasPermi('system:context:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpAiConversationContext hpAiConversationContext)
    {
        startPage();
        List<HpAiConversationContext> list = hpAiConversationContextService.selectHpAiConversationContextList(hpAiConversationContext);
        return getDataTable(list);
    }

    /**
     * 导出AI对话上下文列表
     */
    @PreAuthorize("@ss.hasPermi('system:context:export')")
    @Log(title = "AI对话上下文", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpAiConversationContext hpAiConversationContext)
    {
        List<HpAiConversationContext> list = hpAiConversationContextService.selectHpAiConversationContextList(hpAiConversationContext);
        ExcelUtil<HpAiConversationContext> util = new ExcelUtil<HpAiConversationContext>(HpAiConversationContext.class);
        util.exportExcel(response, list, "AI对话上下文数据");
    }

    /**
     * 获取AI对话上下文详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:context:query')")
    @GetMapping(value = "/{contextId}")
    public AjaxResult getInfo(@PathVariable("contextId") Long contextId)
    {
        return success(hpAiConversationContextService.selectHpAiConversationContextByContextId(contextId));
    }

    /**
     * 新增AI对话上下文
     */
    @PreAuthorize("@ss.hasPermi('system:context:add')")
    @Log(title = "AI对话上下文", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpAiConversationContext hpAiConversationContext)
    {
        return toAjax(hpAiConversationContextService.insertHpAiConversationContext(hpAiConversationContext));
    }

    /**
     * 修改AI对话上下文
     */
    @PreAuthorize("@ss.hasPermi('system:context:edit')")
    @Log(title = "AI对话上下文", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpAiConversationContext hpAiConversationContext)
    {
        return toAjax(hpAiConversationContextService.updateHpAiConversationContext(hpAiConversationContext));
    }

    /**
     * 删除AI对话上下文
     */
    @PreAuthorize("@ss.hasPermi('system:context:remove')")
    @Log(title = "AI对话上下文", businessType = BusinessType.DELETE)
	@DeleteMapping("/{contextIds}")
    public AjaxResult remove(@PathVariable Long[] contextIds)
    {
        return toAjax(hpAiConversationContextService.deleteHpAiConversationContextByContextIds(contextIds));
    }
}
