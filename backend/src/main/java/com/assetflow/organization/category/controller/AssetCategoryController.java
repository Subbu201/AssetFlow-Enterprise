package com.assetflow.organization.category.controller;

import com.assetflow.common.ApiResponse;
import com.assetflow.organization.category.dto.CategoryResponse;
import com.assetflow.organization.category.dto.CreateCategoryRequest;
import com.assetflow.organization.category.service.AssetCategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/categories")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AssetCategoryController {

    private final AssetCategoryService categoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(@Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.createCategory(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Category created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<CategoryResponse>>> getCategories(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String code,
            Pageable pageable) {
        Page<CategoryResponse> responses = categoryService.getCategories(name, code, pageable);
        return ResponseEntity.ok(ApiResponse.success("Categories fetched successfully", responses));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(@PathVariable Long id) {
        CategoryResponse response = categoryService.getCategory(id);
        return ResponseEntity.ok(ApiResponse.success("Category fetched successfully", response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable Long id,
            @Valid @RequestBody CreateCategoryRequest request) {
        CategoryResponse response = categoryService.updateCategory(id, request);
        return ResponseEntity.ok(ApiResponse.success("Category updated successfully", response));
    }
}
