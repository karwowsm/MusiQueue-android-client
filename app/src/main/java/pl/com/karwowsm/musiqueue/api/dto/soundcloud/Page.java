package pl.com.karwowsm.musiqueue.api.dto.soundcloud;

import java.util.List;

import lombok.Value;

@Value
public class Page<T> {

    private final List<T> collection;

    private final int total_results;

    private final String next_href;

    private final String query_urn;
}
