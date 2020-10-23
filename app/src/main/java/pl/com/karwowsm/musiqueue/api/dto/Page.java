package pl.com.karwowsm.musiqueue.api.dto;

import java.util.List;

import lombok.Value;

@Value
public class Page<T> {

    private final List<T> content;

    private final int number;

    private final int size;

    private final int numberOfElements;

    private final int totalPages;

    private final long totalElements;

    private final boolean empty;

    private final boolean first;

    private final boolean last;

    private final Sort sort;

    @Value
    static class Sort {

        private final Boolean sorted;

        private final Boolean unsorted;

        private final Boolean empty;
    }
}
