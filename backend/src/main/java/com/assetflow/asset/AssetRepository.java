package com.assetflow.asset;

import com.assetflow.common.AssetStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface AssetRepository extends JpaRepository<Asset, Long> {
    Optional<Asset> findByAssetTag(String assetTag);
    boolean existsBySerialNumber(String serialNumber);
    long countByStatus(AssetStatus status);

    @Query("select a from Asset a where " +
            "(:keyword is null or lower(a.assetTag) like lower(concat('%', :keyword, '%')) or lower(a.name) like lower(concat('%', :keyword, '%')) or lower(a.serialNumber) like lower(concat('%', :keyword, '%')) or lower(a.location) like lower(concat('%', :keyword, '%')) or lower(a.manufacturer) like lower(concat('%', :keyword, '%')) or lower(a.model) like lower(concat('%', :keyword, '%'))) and " +
            "(:assetTag is null or lower(a.assetTag) like lower(concat('%', :assetTag, '%'))) and " +
            "(:serialNumber is null or lower(a.serialNumber) like lower(concat('%', :serialNumber, '%'))) and " +
            "(:categoryId is null or a.categoryId = :categoryId) and " +
            "(:status is null or a.status = :status) and " +
            "(:departmentId is null or a.departmentId = :departmentId) and " +
            "(:location is null or lower(a.location) like lower(concat('%', :location, '%'))) and " +
            "(:sharedBookable is null or a.sharedBookable = :sharedBookable)")
    Page<Asset> search(@Param("keyword") String keyword,
                       @Param("assetTag") String assetTag,
                       @Param("serialNumber") String serialNumber,
                       @Param("categoryId") Long categoryId,
                       @Param("status") AssetStatus status,
                       @Param("departmentId") Long departmentId,
                       @Param("location") String location,
                       @Param("sharedBookable") Boolean sharedBookable,
                       Pageable pageable);
}
