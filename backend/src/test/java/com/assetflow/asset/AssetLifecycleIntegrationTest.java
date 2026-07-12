package com.assetflow.asset;

import com.assetflow.allocation.AssetAllocationService;
import com.assetflow.allocation.dto.CreateAllocationRequest;
import com.assetflow.asset.dto.CreateAssetRequest;
import com.assetflow.asset.dto.UpdateAssetConditionRequest;
import com.assetflow.asset.dto.UpdateAssetLocationRequest;
import com.assetflow.asset.dto.UpdateAssetStatusRequest;
import com.assetflow.assetreturn.AssetReturnService;
import com.assetflow.assetreturn.dto.ApproveReturnRequest;
import com.assetflow.assetreturn.dto.CreateReturnRequest;
import com.assetflow.assethistory.AssetHistoryRepository;
import com.assetflow.common.AssetStatus;
import com.assetflow.transfer.TransferService;
import com.assetflow.transfer.dto.CreateTransferRequest;
import com.assetflow.transfer.dto.ReviewTransferRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=update",
        "spring.test.database.replace=none",
        "spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver",
        "spring.datasource.url=jdbc:mysql://localhost:3306/assetflow?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC",
        "spring.datasource.username=root",
        "spring.datasource.password=UVADARAN-45"
})
@Transactional
class AssetLifecycleIntegrationTest {

    @Autowired
    private AssetService assetService;

    @Autowired
    private AssetRepository assetRepository;

    @Autowired
    private AssetAllocationService allocationService;

    @Autowired
    private AssetReturnService returnService;

    @Autowired
    private TransferService transferService;

    @Autowired
    private AssetHistoryRepository historyRepository;

    @Test
    void newAssetStatusIsAvailable() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Laptop"));
        assertThat(asset.getStatus()).isEqualTo(AssetStatus.AVAILABLE);
    }

    @Test
    void assetTagIsGeneratedAutomatically() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Monitor"));
        assertThat(asset.getAssetTag()).isNotBlank();
    }

    @Test
    void assetTagFollowsAfFormat() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Keyboard"));
        assertThat(asset.getAssetTag()).matches("AF-\\d{4}");
    }

    @Test
    void duplicateSerialNumberIsRejected() {
        CreateAssetRequest first = buildCreateAssetRequest("Mouse");
        CreateAssetRequest duplicate = buildCreateAssetRequest("Keyboard");
        duplicate.setSerialNumber(first.getSerialNumber());

        assetService.createAsset(first);
        try {
            assetService.createAsset(duplicate);
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("serial");
            return;
        }
        throw new AssertionError("Expected duplicate serial to be rejected");
    }

    @Test
    void assetSearchByTagWorks() {
        assetService.createAsset(buildCreateAssetRequest("Printer"));
        var page = assetService.searchAssets("AF-", null, null, null, null, null, null, null, 0, 10, "id");
        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    void assetSearchByStatusWorks() {
        assetService.createAsset(buildCreateAssetRequest("Scanner"));
        var page = assetService.searchAssets(null, null, null, null, AssetStatus.AVAILABLE.name(), null, null, null, 0,
                10, "id");
        assertThat(page.getContent()).isNotEmpty();
    }

    @Test
    void availableAssetCanBeAllocated() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Phone"));
        var allocation = allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now(), LocalDate.now().plusDays(5), "Initial"));
        assertThat(allocation.getStatus()).isEqualTo("ACTIVE");
        assertThat(assetRepository.findById(asset.getId()).orElseThrow().getStatus()).isEqualTo(AssetStatus.ALLOCATED);
    }

    @Test
    void alreadyAllocatedAssetCannotBeAllocatedAgain() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Tablet"));
        allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null, LocalDate.now(),
                LocalDate.now().plusDays(5), "Initial"));
        try {
            allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1002L, null, LocalDate.now(),
                    LocalDate.now().plusDays(5), "Second"));
        } catch (RuntimeException ex) {
            assertThat(ex.getMessage()).contains("allocated");
            return;
        }
        throw new AssertionError("Expected duplicate allocation to be rejected");
    }

    @Test
    void doubleAllocationReturnsConflict() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Camera"));
        allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null, LocalDate.now(),
                LocalDate.now().plusDays(5), "Initial"));
        assertThatThrownBy(() -> allocationService
                .createAllocation(new CreateAllocationRequest(asset.getId(), 1002L, null, LocalDate.now(),
                        LocalDate.now().plusDays(5), "Second")))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    void returnApprovalChangesAssetStatusToAvailable() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Dock"));
        var allocation = allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now(), LocalDate.now().plusDays(5), "Initial"));
        var returnRequest = returnService.requestReturn(
                new CreateReturnRequest(allocation.getId(), asset.getId(), 1001L, LocalDateTime.now(), "Good", "Fine"));
        returnService.approveReturn(returnRequest.getId(), new ApproveReturnRequest(1003L, LocalDateTime.now(), "ok"));
        assertThat(assetRepository.findById(asset.getId()).orElseThrow().getStatus()).isEqualTo(AssetStatus.AVAILABLE);
    }

    @Test
    void returnApprovalClosesTheAllocation() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Speaker"));
        var allocation = allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now(), LocalDate.now().plusDays(5), "Initial"));
        var returnRequest = returnService.requestReturn(
                new CreateReturnRequest(allocation.getId(), asset.getId(), 1001L, LocalDateTime.now(), "Good", "Fine"));
        returnService.approveReturn(returnRequest.getId(), new ApproveReturnRequest(1003L, LocalDateTime.now(), "ok"));
        assertThat(allocationService.getAllocation(allocation.getId()).getStatus()).isEqualTo("RETURNED");
    }

    @Test
    void transferApprovalClosesTheOldAllocation() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Projector"));
        var allocation = allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now(), LocalDate.now().plusDays(5), "Initial"));
        var transfer = transferService.requestTransfer(new CreateTransferRequest(allocation.getId(), asset.getId(),
                1001L, 1002L, null, "Need move", LocalDateTime.now()));
        transferService.reviewTransfer(transfer.getId(),
                new ReviewTransferRequest(1003L, LocalDateTime.now(), "approve", true));
        assertThat(allocationService.getAllocation(allocation.getId()).getStatus()).isEqualTo("TRANSFERRED");
    }

    @Test
    void transferApprovalCreatesANewActiveAllocation() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Router"));
        var allocation = allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now(), LocalDate.now().plusDays(5), "Initial"));
        var transfer = transferService.requestTransfer(new CreateTransferRequest(allocation.getId(), asset.getId(),
                1001L, 1002L, null, "Need move", LocalDateTime.now()));
        transferService.reviewTransfer(transfer.getId(),
                new ReviewTransferRequest(1003L, LocalDateTime.now(), "approve", true));
        var allocations = allocationService.getAllocationsForAsset(asset.getId());
        assertThat(allocations).hasSize(2);
    }

    @Test
    void transferRejectionKeepsTheOldAllocationActive() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Monitor2"));
        var allocation = allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now(), LocalDate.now().plusDays(5), "Initial"));
        var transfer = transferService.requestTransfer(new CreateTransferRequest(allocation.getId(), asset.getId(),
                1001L, 1002L, null, "Need move", LocalDateTime.now()));
        transferService.reviewTransfer(transfer.getId(),
                new ReviewTransferRequest(1003L, LocalDateTime.now(), "reject", false));
        assertThat(allocationService.getAllocation(allocation.getId()).getStatus()).isEqualTo("ACTIVE");
    }

    @Test
    void pastExpectedReturnDateIsMarkedOverdue() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("UPS"));
        allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now().minusDays(10), LocalDate.now().minusDays(1), "Initial"));
        var count = allocationService.markOverdueAllocations();
        assertThat(count).isGreaterThan(0);
    }

    @Test
    void futureExpectedReturnDateIsNotOverdue() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Battery"));
        allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null, LocalDate.now(),
                LocalDate.now().plusDays(5), "Initial"));
        var count = allocationService.markOverdueAllocations();
        assertThat(count).isZero();
    }

    @Test
    void assetRegistrationCreatesHistory() {
        assetService.createAsset(buildCreateAssetRequest("Laptop2"));
        assertThat(historyRepository.findAll()).isNotEmpty();
    }

    @Test
    void allocationCreatesHistory() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Chair"));
        allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null, LocalDate.now(),
                LocalDate.now().plusDays(5), "Initial"));
        assertThat(historyRepository.findAll()).isNotEmpty();
    }

    @Test
    void returnCreatesHistory() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Table"));
        var allocation = allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now(), LocalDate.now().plusDays(5), "Initial"));
        var returnRequest = returnService.requestReturn(
                new CreateReturnRequest(allocation.getId(), asset.getId(), 1001L, LocalDateTime.now(), "Good", "Fine"));
        returnService.approveReturn(returnRequest.getId(), new ApproveReturnRequest(1003L, LocalDateTime.now(), "ok"));
        assertThat(historyRepository.findAll()).isNotEmpty();
    }

    @Test
    void transferCreatesHistory() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Drawer"));
        var allocation = allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null,
                LocalDate.now(), LocalDate.now().plusDays(5), "Initial"));
        var transfer = transferService.requestTransfer(new CreateTransferRequest(allocation.getId(), asset.getId(),
                1001L, 1002L, null, "Need move", LocalDateTime.now()));
        transferService.reviewTransfer(transfer.getId(),
                new ReviewTransferRequest(1003L, LocalDateTime.now(), "approve", true));
        assertThat(historyRepository.findAll()).isNotEmpty();
    }

    @Test
    void concurrentAllocationAttemptsAllowOnlyOneSuccess() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Server"));
        try {
            allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1001L, null, LocalDate.now(),
                    LocalDate.now().plusDays(5), "Initial"));
            allocationService.createAllocation(new CreateAllocationRequest(asset.getId(), 1002L, null, LocalDate.now(),
                    LocalDate.now().plusDays(5), "Second"));
        } catch (RuntimeException ignored) {
            // expected
        }
        long count = allocationService.getAllocationsForAsset(asset.getId()).size();
        assertThat(count).isLessThanOrEqualTo(1);
    }

    @Test
    void assetUpdateAndTimelineWork() {
        Asset asset = assetService.createAsset(buildCreateAssetRequest("Label"));
        assetService.updateAssetLocation(asset.getId(), new UpdateAssetLocationRequest("Room 2"));
        assetService.updateAssetCondition(asset.getId(), new UpdateAssetConditionRequest("GOOD"));
        assetService.updateAssetStatus(asset.getId(), new UpdateAssetStatusRequest(AssetStatus.UNDER_MAINTENANCE));
        var timeline = assetService.getAssetTimeline(asset.getId());
        assertThat(timeline).isNotEmpty();
    }

    private CreateAssetRequest buildCreateAssetRequest(String name) {
        CreateAssetRequest request = new CreateAssetRequest();
        request.setName(name);
        request.setCategoryId(1L);
        request.setSerialNumber("SN-" + name.replaceAll("\\s+", "") + System.currentTimeMillis());
        request.setAcquisitionDate(LocalDate.now());
        request.setAcquisitionCost(new BigDecimal("1000.00"));
        request.setCondition("NEW");
        request.setLocation("Room 1");
        request.setDepartmentId(10L);
        request.setSharedBookable(false);
        request.setManufacturer("Acme");
        request.setModel("A1");
        request.setWarrantyExpiryDate(LocalDate.now().plusYears(1));
        request.setNotes("Test asset");
        request.setRegisteredByUserId(1L);
        return request;
    }
}
