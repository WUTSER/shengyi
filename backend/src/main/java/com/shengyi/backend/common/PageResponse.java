package com.shengyi.backend.common;

import java.util.List;

public record PageResponse<T>(int pageNo, int pageSize, long total, List<T> records) {
}
