package com.assetflow.organization.category.service;

import com.assetflow.exception.ConflictException;
import com.assetflow.exception.ResourceNotFoundException;
import com.assetflow.organization.category.dto.CategoryResponse;
import com.assetflow.organization.category.dto.CreateCategoryRequest;
import com.assetflow.organization.category.entity.AssetCategory;
import com.assetflow.organization.category.repository.AssetCategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AssetCategoryService {

    private final AssetCategoryRepository categoryRepository;

    @Transactional
    public CategoryResponse createCategory(CreateCategoryRequest request) {
        if (categoryRepository.existsByName(request.getName())) {
            throw new ConflictException("Category name already exists");
        }
        if (categoryRepository.existsByCode(request.getCode())) {
            throw new ConflictException("Category code already exists");
        }

        AssetCategory category = AssetCategory.builder()
                .name(request.getName())
                .code(request.getCode())
                .description(request.getDescription())
                .warrantyPeriodMonths(request.getWarrantyPeriodMonths())
                .build();

        return mapToResponse(categoryRepository.save(category));
    }

    public Page<CategoryResponse> getCategories(String name, String code, Pageable pageable) {
        String searchName = name == null ? "" : name;
        String searchCode = code == null ? "" : code;
        return categoryRepository.findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(searchName, searchCode, pageable)
                .map(this::mapToResponse);
    }

    public CategoryResponse getCategory(Long id) {
        AssetCategory category = categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        return mapToResponse(category);
    }

    private CategoryResponse mapToResponse(AssetCategory category) {
        return CategoryResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .code(category.getCode())
                .description(category.getDescription())
                .warrantyPeriodMonths(category.getWarrantyPeriodMonths())
                .status(category.getStatus())
                .build();
    }
}
