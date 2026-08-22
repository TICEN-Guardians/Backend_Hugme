package com.project.hugme.domain.notification.service;

import com.project.hugme.domain.checklist.entity.application.ContractType;
import com.project.hugme.domain.notification.dto.DDayResult;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

@Component
public class DDayCalculator {

    private static final ZoneId KOREA_ZONE =
            ZoneId.of("Asia/Seoul");

    public DDayResult calculate(
            ContractType contractType,
            LocalDate contractStartDate,
            LocalDate contractEndDate,
            LocalDate balancePaymentDate,
            LocalDate moveInDate
    ) {
        validateDates(
                contractType,
                contractStartDate,
                contractEndDate,
                balancePaymentDate,
                moveInDate
        );

        /*
         * 계약 종료일을 포함하여 전체 계약 일수를 계산한다.
         */
        long contractDays =
                ChronoUnit.DAYS.between(
                        contractStartDate,
                        contractEndDate.plusDays(1)
                );

        long halfContractDays =
                contractDays / 2;

        /*
         * 신규계약:
         * 잔금지급일과 전입신고일 중 늦은 날짜부터 계산
         *
         * 갱신계약:
         * 갱신 계약 시작일부터 계산
         */
        LocalDate calculationStartDate;

        if (contractType == ContractType.NEW) {
            calculationStartDate =
                    getLaterDate(
                            balancePaymentDate,
                            moveInDate
                    );
        } else if (
                contractType == ContractType.RENEWAL
        ) {
            calculationStartDate =
                    contractStartDate;
        } else {
            throw new IllegalArgumentException(
                    "지원하지 않는 계약 유형입니다."
            );
        }

        /*
         * 계약기간의 1/2이 경과하는 날짜
         */
        LocalDate halfElapsedDate =
                calculationStartDate.plusDays(
                        halfContractDays
                );

        /*
         * ‘1/2 경과하기 전’까지 신청할 수 있으므로
         * 전날을 마지막 신청 가능일로 계산한다.
         */
        LocalDate applicationDeadline =
                halfElapsedDate.minusDays(1);

        LocalDate today =
                LocalDate.now(KOREA_ZONE);

        long dDay =
                ChronoUnit.DAYS.between(
                        today,
                        applicationDeadline
                );

        return new DDayResult(
                applicationDeadline,
                dDay
        );
    }

    private LocalDate getLaterDate(
            LocalDate firstDate,
            LocalDate secondDate
    ) {
        if (firstDate.isAfter(secondDate)) {
            return firstDate;
        }

        return secondDate;
    }

    private void validateDates(
            ContractType contractType,
            LocalDate contractStartDate,
            LocalDate contractEndDate,
            LocalDate balancePaymentDate,
            LocalDate moveInDate
    ) {
        if (contractType == null) {
            throw new IllegalArgumentException(
                    "계약 유형이 필요합니다."
            );
        }

        if (contractStartDate == null
                || contractEndDate == null) {

            throw new IllegalArgumentException(
                    "계약 시작일과 종료일이 필요합니다."
            );
        }

        if (contractEndDate.isBefore(
                contractStartDate
        )) {
            throw new IllegalArgumentException(
                    "계약 종료일은 시작일보다 빠를 수 없습니다."
            );
        }

        /*
         * 신규계약일 때만 잔금지급일과
         * 전입신고일이 반드시 필요하다.
         */
        if (contractType == ContractType.NEW
                && (balancePaymentDate == null
                || moveInDate == null)) {

            throw new IllegalArgumentException(
                    "신규계약은 잔금지급일과 전입신고일이 필요합니다."
            );
        }
    }
}