package com.rememberfit.backend.domain.study.repository;

import com.rememberfit.backend.domain.study.entity.StudyLog;
import com.rememberfit.backend.domain.study.repository.projection.DailyStudyCountProjection;
import com.rememberfit.backend.domain.study.repository.projection.QualityCountProjection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StudyLogRepository extends JpaRepository<StudyLog, Long> {

    @Query("""
            SELECT s.studyDate AS studyDate, COUNT(s) AS count
            FROM StudyLog s
            WHERE s.studyDate BETWEEN :startDate AND :endDate
            GROUP BY s.studyDate
            ORDER BY s.studyDate ASC
            """)
    List<DailyStudyCountProjection> countDailyBetween(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT s.quality AS quality, COUNT(s) AS count
            FROM StudyLog s
            GROUP BY s.quality
            ORDER BY s.quality ASC
            """)
    List<QualityCountProjection> countByQuality();

    @Query("SELECT DISTINCT s.studyDate FROM StudyLog s ORDER BY s.studyDate DESC")
    List<LocalDate> findDistinctStudyDatesDescending();
}
