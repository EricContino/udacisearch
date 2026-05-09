package com.udacity.webcrawler;

import com.udacity.webcrawler.json.CrawlResult;

import javax.inject.Inject;
import javax.inject.Provider;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.ForkJoinPool;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import com.udacity.webcrawler.parser.PageParserFactory;
import java.util.Comparator;
import java.util.regex.Pattern;

/**
 * A concrete implementation of {@link WebCrawler} that runs multiple threads on a
 * {@link ForkJoinPool} to fetch and process multiple web pages in parallel.
 */
final class ParallelWebCrawler implements WebCrawler {
  private final Clock clock;
  private final Duration timeout;
  private final int popularWordCount;
  private final ForkJoinPool pool;
  private final PageParserFactory parserFactory;
  private final int maxDepth;
  private final List<Pattern> ignoredUrls;

  @Inject
  ParallelWebCrawler(
      Clock clock,
      @Timeout Duration timeout,
      @PopularWordCount int popularWordCount,
      @TargetParallelism int threadCount,
      PageParserFactory parserFactory,
      @MaxDepth int maxDepth,
      @IgnoredUrls List<Pattern> ignoredUrls) {
    this.clock = clock;
    this.timeout = timeout;
    this.popularWordCount = popularWordCount;
    this.pool = new ForkJoinPool(Math.min(threadCount, getMaxParallelism()));
    this.parserFactory = parserFactory;
    this.maxDepth = maxDepth;
    this.ignoredUrls = ignoredUrls;
  }

  @Override
  public CrawlResult crawl(List<String> startingUrls) {
    Map<String, Integer> counts = new ConcurrentHashMap<>();
    Set<String> visited = ConcurrentHashMap.newKeySet();

    Instant deadline = clock.instant().plus(timeout);

    List<CrawlAction> actions = new ArrayList<>();
    for (String url : startingUrls) {
      actions.add(new CrawlAction(url, maxDepth, deadline, counts, visited, this.parserFactory, this.clock, this.ignoredUrls));
    }

    for (CrawlAction action : actions) {
      pool.execute(action);
    }
    for (CrawlAction action : actions) {
      action.join();
    }

    Map<String, Integer> finalCounts =
        counts.entrySet().stream()
        .sorted(
                Comparator.<Map.Entry<String, Integer>>comparingInt(Map.Entry::getValue)
                        .reversed()
                        .thenComparing(Map.Entry::getKey)
        )
        .limit(popularWordCount)
        .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new
        ));


    return new CrawlResult.Builder()
            .setWordCounts(finalCounts)
            .setUrlsVisited(visited.size())
            .build();
  }

  @Override
  public int getMaxParallelism() {
    return Runtime.getRuntime().availableProcessors();
  }
}
