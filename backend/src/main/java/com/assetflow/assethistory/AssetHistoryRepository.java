package com.assetflow.assethistory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AssetHistoryRepository extends JpaRepository<AssetHistory, Long> {
    List<AssetHistory> findByAssetIdOrderByEventTimeDescCreatedAtDescIdDesc(Long assetId);
}
