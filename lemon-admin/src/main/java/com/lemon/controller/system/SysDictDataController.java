package com.lemon.controller.system;

import com.lemon.common.core.controller.BaseController;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 数据字典信息
 */
@Validated
@RequiredArgsConstructor
@RestController
@RequestMapping("/system/dict/data")
public class SysDictDataController extends BaseController {
}
