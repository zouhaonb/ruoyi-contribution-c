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
import com.ruoyi.system.domain.HpAiKnowledgeDoc;
import com.ruoyi.system.service.IHpAiKnowledgeDocService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * AI知识库文档Controller
 * 
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/doc")
public class HpAiKnowledgeDocController extends BaseController
{
    @Autowired
    private IHpAiKnowledgeDocService hpAiKnowledgeDocService;

    /**
     * 查询AI知识库文档列表
     */
    @PreAuthorize("@ss.hasPermi('system:doc:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpAiKnowledgeDoc hpAiKnowledgeDoc)
    {
        startPage();
        List<HpAiKnowledgeDoc> list = hpAiKnowledgeDocService.selectHpAiKnowledgeDocList(hpAiKnowledgeDoc);
        return getDataTable(list);
    }

    /**
     * 导出AI知识库文档列表
     */
    @PreAuthorize("@ss.hasPermi('system:doc:export')")
    @Log(title = "AI知识库文档", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpAiKnowledgeDoc hpAiKnowledgeDoc)
    {
        List<HpAiKnowledgeDoc> list = hpAiKnowledgeDocService.selectHpAiKnowledgeDocList(hpAiKnowledgeDoc);
        ExcelUtil<HpAiKnowledgeDoc> util = new ExcelUtil<HpAiKnowledgeDoc>(HpAiKnowledgeDoc.class);
        util.exportExcel(response, list, "AI知识库文档数据");
    }

    /**
     * 获取AI知识库文档详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:doc:query')")
    @GetMapping(value = "/{docId}")
    public AjaxResult getInfo(@PathVariable("docId") Long docId)
    {
        return success(hpAiKnowledgeDocService.selectHpAiKnowledgeDocByDocId(docId));
    }

    /**
     * 新增AI知识库文档
     */
    @PreAuthorize("@ss.hasPermi('system:doc:add')")
    @Log(title = "AI知识库文档", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpAiKnowledgeDoc hpAiKnowledgeDoc)
    {
        return toAjax(hpAiKnowledgeDocService.insertHpAiKnowledgeDoc(hpAiKnowledgeDoc));
    }

    /**
     * 修改AI知识库文档
     */
    @PreAuthorize("@ss.hasPermi('system:doc:edit')")
    @Log(title = "AI知识库文档", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpAiKnowledgeDoc hpAiKnowledgeDoc)
    {
        return toAjax(hpAiKnowledgeDocService.updateHpAiKnowledgeDoc(hpAiKnowledgeDoc));
    }

    /**
     * 删除AI知识库文档
     */
    @PreAuthorize("@ss.hasPermi('system:doc:remove')")
    @Log(title = "AI知识库文档", businessType = BusinessType.DELETE)
	@DeleteMapping("/{docIds}")
    public AjaxResult remove(@PathVariable Long[] docIds)
    {
        return toAjax(hpAiKnowledgeDocService.deleteHpAiKnowledgeDocByDocIds(docIds));
    }
}
