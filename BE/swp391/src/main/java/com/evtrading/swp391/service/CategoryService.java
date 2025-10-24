package com.evtrading.swp391.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.evtrading.swp391.dto.CategoryDTO;
import com.evtrading.swp391.entity.Category;
import com.evtrading.swp391.repository.CategoryRepository;

@Service
public class CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    public CategoryDTO createCategory(CategoryDTO dto) {
        Category category = new Category();
        category.setCategoryName(dto.getCategoryName());
        Category savedCategory = categoryRepository.save(category);

        CategoryDTO response = new CategoryDTO();
        response.setCategoryId(savedCategory.getCategoryID());
        response.setCategoryName(savedCategory.getCategoryName());
        return response;
    }
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }
}