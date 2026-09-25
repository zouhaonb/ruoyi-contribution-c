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
import com.ruoyi.system.domain.HpWorkOrderCategory;
import com.ruoyi.system.service.IHpWorkOrderCategoryService;
import com.ruoyi.common.utils.poi.ExcelUtil;
import com.ruoyi.common.core.page.TableDataInfo;

/**
 * 工单分类Controller
 *
 * @author pyz
 * @date 2026-09-12
 */
@RestController
@RequestMapping("/system/category")
public class HpWorkOrderCategoryController extends BaseController
{
    @Autowired
    private IHpWorkOrderCategoryService hpWorkOrderCategoryService;

    /**
     * 查询工单分类列表
     */
    @PreAuthorize("@ss.hasPermi('system:category:list')")
    @GetMapping("/list")
    public TableDataInfo list(HpWorkOrderCategory hpWorkOrderCategory)
    {
        startPage();
        List<HpWorkOrderCategory> list = hpWorkOrderCategoryService.selectHpWorkOrderCategoryList(hpWorkOrderCategory);
        return getDataTable(list);
    }

    /**
     * 导出工单分类列表
     */
    @PreAuthorize("@ss.hasPermi('system:category:export')")
    @Log(title = "工单分类", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, HpWorkOrderCategory hpWorkOrderCategory)
    {
        List<HpWorkOrderCategory> list = hpWorkOrderCategoryService.selectHpWorkOrderCategoryList(hpWorkOrderCategory);
        ExcelUtil<HpWorkOrderCategory> util = new ExcelUtil<HpWorkOrderCategory>(HpWorkOrderCategory.class);
        util.exportExcel(response, list, "工单分类数据");
    }

    /**
     * 获取工单分类详细信息
     */
    @PreAuthorize("@ss.hasPermi('system:category:query')")
    @GetMapping(value = "/{categoryId}")
    public AjaxResult getInfo(@PathVariable("categoryId") Long categoryId)
    {
        return success(hpWorkOrderCategoryService.selectHpWorkOrderCategoryByCategoryId(categoryId));
    }

    /**
     * 新增工单分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:add')")
    @Log(title = "工单分类", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody HpWorkOrderCategory hpWorkOrderCategory)
    {
        // 检查分类编码唯一性
        if (!hpWorkOrderCategoryService.checkCategoryCodeUnique(hpWorkOrderCategory.getCategoryCode(), null))
        {
            return error("分类编码已存在");
        }
        return toAjax(hpWorkOrderCategoryService.insertHpWorkOrderCategory(hpWorkOrderCategory));
    }

    /**
     * 修改工单分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:edit')")
    @Log(title = "工单分类", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody HpWorkOrderCategory hpWorkOrderCategory)
    {
        // 检查分类编码唯一性
        if (!hpWorkOrderCategoryService.checkCategoryCodeUnique(hpWorkOrderCategory.getCategoryCode(), hpWorkOrderCategory.getCategoryId()))
        {
            return error("分类编码已存在");
        }
        return toAjax(hpWorkOrderCategoryService.updateHpWorkOrderCategory(hpWorkOrderCategory));
    }

    /**
     * 删除工单分类
     */
    @PreAuthorize("@ss.hasPermi('system:category:remove')")
    @Log(title = "工单分类", businessType = BusinessType.DELETE)
    @DeleteMapping("/{categoryIds}")
    public AjaxResult remove(@PathVariable Long[] categoryIds)
    {
        return toAjax(hpWorkOrderCategoryService.deleteHpWorkOrderCategoryByCategoryIds(categoryIds));
    }

    /**
     * 获取树形下拉列表
     */
    @PreAuthorize("@ss.hasPermi('system:category:list')")
    @GetMapping("/treeselect")
    public AjaxResult treeselect()
    {
        List<HpWorkOrderCategory> categories = hpWorkOrderCategoryService.selectAllCategoryList();
        return success(buildTreeSelect(categories));
    }

    /**
     * 构建树形下拉选项
     */
    private List<HpWorkOrderCategory> buildTreeSelect(List<HpWorkOrderCategory> categories)
    {
        return categories.stream()
                .filter(category -> category.getParentId() == null || category.getParentId() == 0)
                .peek(parent -> parent.setChildren(buildChildren(parent.getCategoryId(), categories)))
                .collect(java.util.stream.Collectors.toList());
    }

    /**
     * 递归构建子节点
     */
    private List<HpWorkOrderCategory> buildChildren(Long parentId, List<HpWorkOrderCategory> categories)
    {
        return categories.stream()
                .filter(category -> parentId.equals(category.getParentId()))
                .peek(category -> category.setChildren(buildChildren(category.getCategoryId(), categories)))
                .collect(java.util.stream.Collectors.toList());
    }
}
