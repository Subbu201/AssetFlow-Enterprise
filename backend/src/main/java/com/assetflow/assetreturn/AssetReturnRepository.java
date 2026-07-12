package com.assetflow.assetreturn;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AssetReturnRepository extends JpaRepository<AssetReturn, Long> {
    List<AssetReturn> findByStatus(String status);
    Optional<AssetReturn> findFirstByAllocationIdAndStatus(String allocationId, String status);
    Optional<AssetReturn> findFirstByAllocationIdAndStatusIn(Long allocationId, List<String> statuses);
}
