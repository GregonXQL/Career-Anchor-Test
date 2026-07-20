package com.careeranchor.server.dto;

public record AdminStatsResponse(long totalResults, long todayResults,
                                 long totalUsers, long activeInvites) {}
