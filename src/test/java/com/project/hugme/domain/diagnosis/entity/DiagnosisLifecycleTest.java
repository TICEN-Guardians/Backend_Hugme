package com.project.hugme.domain.diagnosis.entity;

import com.project.hugme.domain.diagnosis.enums.DiagnosisMode;
import com.project.hugme.domain.diagnosis.enums.DiagnosisStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DiagnosisLifecycleTest {

    @Test
    void quickDiagnosisBecomesReadyAfterDetails() {
        Diagnosis diagnosis = Diagnosis.create(null, DiagnosisMode.QUICK, "hash", null);
        fillDetails(diagnosis);
        diagnosis.markDetailsReady(false);
        assertEquals(DiagnosisStatus.READY, diagnosis.getStatus());
    }

    @Test
    void confirmedAddressCanPrecedeRegistryUpload() {
        Diagnosis diagnosis = Diagnosis.create(null, DiagnosisMode.DETAILED, null, null);
        diagnosis.updateAddress(
                "서울특별시 강서구 화곡동 359-20",
                null,
                null,
                null,
                false
        );
        diagnosis.markAddressConfirmed(false);
        assertEquals(DiagnosisStatus.CREATED, diagnosis.getStatus());
        diagnosis.markRegistryProcessed(true);
        assertEquals(DiagnosisStatus.REGISTRY_ANALYZED, diagnosis.getStatus());
    }

    @Test
    void detailedDiagnosisAllowsEitherDetailsOrRegistryFirst() {
        Diagnosis detailsFirst = Diagnosis.create(null, DiagnosisMode.DETAILED, null, null);
        fillDetails(detailsFirst);
        detailsFirst.markDetailsReady(false);
        assertEquals(DiagnosisStatus.DETAILS_READY, detailsFirst.getStatus());
        detailsFirst.markRegistryProcessed(true);
        assertEquals(DiagnosisStatus.READY, detailsFirst.getStatus());

        Diagnosis registryFirst = Diagnosis.create(null, DiagnosisMode.DETAILED, null, null);
        registryFirst.markRegistryProcessed(true);
        assertEquals(DiagnosisStatus.REGISTRY_ANALYZED, registryFirst.getStatus());
        fillDetails(registryFirst);
        registryFirst.markDetailsReady(true);
        assertEquals(DiagnosisStatus.READY, registryFirst.getStatus());
    }

    @Test
    void registryUploadResetsPartialAddressConfirmation() {
        Diagnosis diagnosis = Diagnosis.create(
                null,
                DiagnosisMode.DETAILED,
                null,
                null
        );
        diagnosis.updateAddress(
                "서울특별시 중구 세종대로 110",
                null,
                null,
                null,
                true
        );
        assertTrue(diagnosis.isRegistryAddressReviewConfirmed());
        diagnosis.resetRegistryAddressReviewConfirmation();
        assertFalse(diagnosis.isRegistryAddressReviewConfirmed());
    }

    private void fillDetails(Diagnosis diagnosis) {
        diagnosis.updateDetails(
                "서울특별시 중구 세종대로 110",
                null,
                null,
                100_000_000L,
                LocalDate.of(2026, 8, 21),
                null,
                new BigDecimal("59.83"),
                null,
                null,
                null,
                false
        );
    }
}
