package com.lemon.controller.system;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.lemon.common.annotation.Log;
import com.lemon.common.core.controller.BaseController;
import com.lemon.common.core.domain.R;
import com.lemon.common.core.domain.entity.SysDictType;
import com.lemon.common.core.page.PageQuery;
import com.lemon.common.core.page.TableDataInfo;
import com.lemon.common.enums.BusinessType;
import com.lemon.system.service.ISysDictTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 数据字典信息
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController extends BaseController {
    private final ISysDictTypeService dictTypeService;

    /**
     * 查询字典类型列表
     */
    @GetMapping("/list")
    public TableDataInfo<SysDictType> list(SysDictType dictType, PageQuery pageQuery) {
        return dictTypeService.selectPageDictTypeList(dictType, pageQuery);
    }

    /**
     * 查询字典类型详细
     *
     * @param dictId 字典ID
     */
    @GetMapping(value = "/{dictId}")
    public R<SysDictType> getInfo(@PathVariable Long dictId) {
        return R.ok(dictTypeService.selectDictTypeById(dictId));
    }

    /**
     * 新增字典类型
     */
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping
    public R<Void> add(@Validated @RequestBody SysDictType dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return R.fail("新增字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dictTypeService.insertDictType(dict);
        return R.ok();
    }

    /**
     * 修改字典类型
     */
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public R<Void> edit(@Validated @RequestBody SysDictType dict) {
        if (!dictTypeService.checkDictTypeUnique(dict)) {
            return R.fail("修改字典'" + dict.getDictName() + "'失败，字典类型已存在");
        }
        dictTypeService.updateDictType(dict);
        return R.ok();
    }

    /**
     * 删除字典类型
     *
     * @param dictIds 字典ID串
     */
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictIds}")
    public R<Void> remove(@PathVariable Long[] dictIds) {
        dictTypeService.deleteDictTypeByIds(dictIds);
        return R.ok();
    }

    /**
     * 刷新字典缓存
     */
    @Log(title = "字典类型", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public R<Void> refreshCache() {
        dictTypeService.resetDictCache();
        return R.ok();
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping("/optionselect")
    public R<List<SysDictType>> optionselect() {
        List<SysDictType> dictTypes = dictTypeService.selectDictTypeAll();
        return R.ok(dictTypes);
    }
}
