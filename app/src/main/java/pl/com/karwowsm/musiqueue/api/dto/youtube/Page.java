package pl.com.karwowsm.musiqueue.api.dto.youtube;

import java.util.List;

import lombok.Value;

@Value
public class Page<T> {

    private final List<T> items;

    private final PageInfo pageInfo;

    @Value
    public static class PageInfo {

        private final int totalResults;

        private final int resultsPerPage;
    }
}
