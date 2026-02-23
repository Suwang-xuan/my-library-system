package com.library.controller;

import com.library.common.CommonResult;
import com.library.entity.BookCategory;
import com.library.service.BookCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/category")
public class BookCategoryController {

    private static final Logger logger = LoggerFactory.getLogger(BookCategoryController.class);

    @Autowired
    private BookCategoryService bookCategoryService;

    @GetMapping("/list")
    public CommonResult<List<BookCategory>> getAllCategories() {
        List<BookCategory> categories = bookCategoryService.getAllCategories();
        return CommonResult.success(categories);
    }

    @GetMapping("/{categoryId}")
    public CommonResult<BookCategory> getCategoryById(@PathVariable Long categoryId) {
        BookCategory category = bookCategoryService.getCategoryById(categoryId);
        return CommonResult.success(category);
    }

    @PostMapping("/add")
    public CommonResult<Boolean> addCategory(@RequestBody BookCategory category) {
        boolean result = bookCategoryService.saveCategory(category);
        return result ? CommonResult.success("添加成功", true) : CommonResult.error("添加失败");
    }

    @PutMapping("/update")
    public CommonResult<Boolean> updateCategory(@RequestBody BookCategory category) {
        boolean result = bookCategoryService.updateCategory(category);
        return result ? CommonResult.success("更新成功", true) : CommonResult.error("更新失败");
    }

    @DeleteMapping("/delete/{categoryId}")
    public CommonResult<Boolean> deleteCategory(@PathVariable Long categoryId) {
        boolean result = bookCategoryService.deleteCategory(categoryId);
        return result ? CommonResult.success("删除成功", true) : CommonResult.error("删除失败");
    }

}
