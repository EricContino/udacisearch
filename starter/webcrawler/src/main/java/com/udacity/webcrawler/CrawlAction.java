package com.udacity.webcrawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.RecursiveAction;
import java.time.Instant;
import com.udacity.webcrawler.parser.PageParserFactory;
import java.time.Clock;
import com.udacity.webcrawler.parser.PageParser;
import java.util.regex.Pattern;

class CrawlAction extends RecursiveAction {
    private final String url;
    private final int depth;
    private final Instant deadline;
    private final Map<String, Integer> counts;
    private final Set<String> visited;
    private final PageParserFactory parserFactory;
    private final Clock clock;
    private final List<Pattern> ignoredUrls;

    CrawlAction(
            String url,
            int depth,
            Instant deadline,
            Map<String, Integer> counts,
            Set<String> visited,
            PageParserFactory parserFactory,
            Clock clock,
            List<Pattern> ignoredUrls) {
        this.url = url;
        this.depth = depth;
        this.deadline = deadline;
        this.counts = counts;
        this.visited = visited;
        this.parserFactory = parserFactory;
        this.clock = clock;
        this.ignoredUrls = ignoredUrls;
    }

    @Override
    protected void compute() {
        if (depth <= 0 || !clock.instant().isBefore(deadline)) {
            return;
        }
        if (ignoredUrls.stream().anyMatch(p -> p.matcher(url).matches())) {
            return;
        }
        if (!visited.add(url)) {
            return;
        }

        PageParser.Result result = parserFactory.get(url).parse();

        for (Map.Entry<String, Integer> entry : result.getWordCounts().entrySet()) {
            counts.merge(entry.getKey(), entry.getValue(), Integer::sum);
        }

        List<CrawlAction> subtasks = new ArrayList<>();
        for (String link : result.getLinks()) {
            subtasks.add(new CrawlAction(
                    link, depth - 1, deadline, counts, visited, parserFactory, clock, ignoredUrls));
        }
        invokeAll(subtasks);
    }

}