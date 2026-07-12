package com.assetflow.organization.category.repository;

import com.assetflow.organization.category.entity.AssetCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AssetCategoryRepository extends JpaRepository<AssetCategory, Long> {
    boolean existsByName(String name);
    boolean existsByCode(String code);
    Page<AssetCategory> findByNameContainingIgnoreCaseAndCodeContainingIgnoreCase(String name, String code, Pageable pageable);
}
