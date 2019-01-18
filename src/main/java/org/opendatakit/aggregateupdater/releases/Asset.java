package org.opendatakit.aggregateupdater.releases;

import static org.opendatakit.aggregateupdater.reused.HttpHelpers.url;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Optional;

public class Asset {
  private final URL url;
  private final int id;
  private final String nodeId;
  private final String name;
  private final Optional<String> label;
  private final Author uploader;
  private final String contentType;
  private final String state;
  private final int size;
  private final int downloadCount;
  private final ZonedDateTime createdAt;
  private final ZonedDateTime updatedAt;
  private final URL browserDownloadUrl;

  public Asset(URL url, int id, String nodeId, String name, Optional<String> label, Author uploader, String contentType, String state, int size, int downloadCount, ZonedDateTime createdAt, ZonedDateTime updatedAt, URL browserDownloadUrl) {
    this.url = url;
    this.id = id;
    this.nodeId = nodeId;
    this.name = name;
    this.label = label;
    this.uploader = uploader;
    this.contentType = contentType;
    this.state = state;
    this.size = size;
    this.downloadCount = downloadCount;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
    this.browserDownloadUrl = browserDownloadUrl;
  }

  public static Asset from(Map<String, Object> json) {
    return new Asset(
        url((String) json.get("url")),
        (Integer) json.get("id"),
        (String) json.get("node_id"),
        (String) json.get("name"),
        Optional.ofNullable((String) json.get("label")).filter(s -> !s.equals("null")),
        Author.from((Map<String, Object>) json.get("uploader")),
        (String) json.get("content_type"),
        (String) json.get("state"),
        (Integer) json.get("size"),
        (Integer) json.get("download_count"),
        OffsetDateTime.parse((String) json.get("created_at")).toZonedDateTime(),
        OffsetDateTime.parse((String) json.get("updated_at")).toZonedDateTime(),
        url((String) json.get("browser_download_url"))
    );
  }
}
