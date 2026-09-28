package com.rememberfit.backend.domain.statistics.dto;

import java.time.LocalDate;

public record DailyStudyCountDto(
        LocalDate date,
        long count
) {
}
