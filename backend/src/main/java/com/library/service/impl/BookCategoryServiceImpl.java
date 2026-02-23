package com.library.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.library.entity.BookCategory;
import com.library.exception.BusinessException;
import com.library.mapper.BookCategoryMapper;
import com.library.service.BookCategoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class BookCategoryServiceImpl extends ServiceImpl<BookCategoryMapper, BookCategory> implements BookCategoryService {

    private static final Logger logger = LoggerFactory.getLogger(BookCategoryServiceImpl.class);

    @Override
    public List<BookCategory> getAllCategories() {
        return this.baseMapper.selectAllCategories();
    }

    @Override
    public BookCategory getCategoryById(Long categoryId) {
        BookCategory category = this.baseMapper.selectByCategoryId(categoryId);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }
        return category;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean saveCategory(BookCategory category) {
        if (!StringUtils.hasText(category.getCategoryName())) {
            throw new BusinessException(400, "分类名称不能为空");
        }

        LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BookCategory::getCategoryName, category.getCategoryName());
        if (this.count(wrapper) > 0) {
            throw new BusinessException(400, "分类名称已存在");
        }

        boolean result = this.save(category);
        if (result) {
            logger.info("新增分类: {}", category.getCategoryName());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean updateCategory(BookCategory category) {
        if (category.getCategoryId() == null) {
            throw new BusinessException(400, "分类ID不能为空");
        }

        if (StringUtils.hasText(category.getCategoryName())) {
            BookCategory existing = this.getById(category.getCategoryId());
            if (existing == null) {
                throw new BusinessException(404, "分类不存在");
            }

            if (!category.getCategoryName().equals(existing.getCategoryName())) {
                LambdaQueryWrapper<BookCategory> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(BookCategory::getCategoryName, category.getCategoryName());
                if (this.count(wrapper) > 0) {
                    throw new BusinessException(400, "分类名称已存在");
                }
            }
        }

        boolean result = this.updateById(category);
        if (result) {
            logger.info("更新分类: {}", category.getCategoryId());
        }
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean deleteCategory(Long categoryId) {
        BookCategory category = this.getById(categoryId);
        if (category == null) {
            throw new BusinessException(404, "分类不存在");
        }

        boolean result = this.removeById(categoryId);
        if (result) {
            logger.info("删除分类: {}", categoryId);
        }
        return result;
    }

}
