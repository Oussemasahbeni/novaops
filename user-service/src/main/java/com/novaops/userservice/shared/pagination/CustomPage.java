package com.novaops.userservice.shared.pagination;

import java.util.List;

public record CustomPage<T>(
        List<T> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize,
        boolean isFirst,
        boolean isLast,
        boolean isEmpty,
        CustomSortInfo sortInfo) {}
