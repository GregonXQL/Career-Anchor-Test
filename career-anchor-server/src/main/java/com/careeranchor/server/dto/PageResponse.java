package com.careeranchor.server.dto;

import java.util.List;

public record PageResponse<T>(long total, int page, int size, List<T> records) {
}
