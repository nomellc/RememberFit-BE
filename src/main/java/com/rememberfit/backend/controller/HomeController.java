package com.rememberfit.backend.controller;

import com.rememberfit.backend.dto.HomeStatsResponseDto;
import com.rememberfit.backend.service.HomeService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/home")
@RequiredArgsConstructor
public class HomeController {
    private final HomeService homeService;

    @GetMapping("/stats")
    public HomeStatsResponseDto getStats() {
        return homeService.getHomeStats();
    }
}
