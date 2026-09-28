package com.rememberfit.backend.domain.study.repository.projection;

import java.time.LocalDate;

public interface DailyStudyCountProjection {
    LocalDate getStudyDate();

    long getCount();
}
