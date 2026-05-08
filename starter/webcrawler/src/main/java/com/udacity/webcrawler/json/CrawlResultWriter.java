package com.udacity.webcrawler.json;

import java.io.Writer;
import java.nio.file.Path;
import java.nio.file.Files;
import java.util.Objects;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * Utility class to write a {@link CrawlResult} to file.
 */
public final class CrawlResultWriter {
  private final CrawlResult result;

  /**
   * Creates a new {@link CrawlResultWriter} that will write the given {@link CrawlResult}.
   */
  public CrawlResultWriter(CrawlResult result) {
    this.result = Objects.requireNonNull(result);
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Path}.
   *
   * <p>If a file already exists at the path, the existing file should not be deleted; new data
   * should be appended to it.
   *
   * @param path the file path where the crawl result data should be written.
   */
  public void write(Path path) {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(path);
    try {
      Writer writer = Files.newBufferedWriter(path);
      write(writer);
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not read crawler configuration", e);
    }
  }

  /**
   * Formats the {@link CrawlResult} as JSON and writes it to the given {@link Writer}.
   *
   * @param writer the destination where the crawl result data should be written.
   */
  public void write(Writer writer) {
    // This is here to get rid of the unused variable warning.
    Objects.requireNonNull(writer);
    ObjectMapper mapper = new ObjectMapper();
    mapper.disable(com.fasterxml.jackson.core.JsonGenerator.Feature.AUTO_CLOSE_TARGET);
    try {
      mapper.writeValue(writer, this.result);
    } catch (IOException e) {
      throw new IllegalArgumentException("Could not read crawler configuration", e);
    }
  }
}
