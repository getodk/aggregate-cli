package org.opendatakit.aggregateupdater.reused.releases;

import static org.opendatakit.aggregateupdater.reused.http.HttpHelpers.url;

import java.net.URL;
import java.time.OffsetDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class Release implements Comparable<Release> {
  private final URL url;
  private final URL assetsUrl;
  private final URL uploadUrl;
  private final URL htmlUrl;
  private final int id;
  private final String nodeId;
  private final String tagName;
  private final Version version;
  private final String targetCommitish;
  private final String name;
  private final boolean draft;
  private final Author author;
  private final boolean prerelease;
  private final ZonedDateTime createdAt;
  private final ZonedDateTime publishedAt;
  private final List<Asset> assets;
  private final URL tarballUrl;
  private final URL zipballUrl;
  private final String body;

  public Release(URL url, URL assetsUrl, URL uploadUrl, URL htmlUrl, int id, String nodeId, String tagName, Version version, String targetCommitish, String name, boolean draft, Author author, boolean prerelease, ZonedDateTime createdAt, ZonedDateTime publishedAt, List<Asset> assets, URL tarballUrl, URL zipballUrl, String body) {
    this.url = url;
    this.assetsUrl = assetsUrl;
    this.uploadUrl = uploadUrl;
    this.htmlUrl = htmlUrl;
    this.id = id;
    this.nodeId = nodeId;
    this.tagName = tagName;
    this.version = version;
    this.targetCommitish = targetCommitish;
    this.name = name;
    this.draft = draft;
    this.author = author;
    this.prerelease = prerelease;
    this.createdAt = createdAt;
    this.publishedAt = publishedAt;
    this.assets = assets;
    this.tarballUrl = tarballUrl;
    this.zipballUrl = zipballUrl;
    this.body = body;
  }

  @SuppressWarnings("unchecked")
  public static Release from(Map<String, Object> json) {
    Version version;
    String tagName = (String) json.get("tag_name");
    try {
      version = Version.from(tagName);
    } catch (IllegalArgumentException e) {
      version = new Version.NullVersion(tagName);
    }
    return new Release(
        url((String) json.get("url")),
        url((String) json.get("assets_url")),
        url((String) json.get("upload_url")),
        url((String) json.get("html_url")),
        (Integer) json.get("id"),
        (String) json.get("node_id"),
        tagName,
        version,
        (String) json.get("target_commitish"),
        (String) json.get("name"),
        (Boolean) json.get("draft"),
        Author.from((Map<String, Object>) json.get("author")),
        (Boolean) json.get("prerelease"),
        OffsetDateTime.parse((String) json.get("created_at")).toZonedDateTime(),
        OffsetDateTime.parse((String) json.get("published_at")).toZonedDateTime(),
        ((List<Map<String, Object>>) json.get("assets")).stream().map(Asset::from).collect(Collectors.toList()),
        url((String) json.get("tarball_url")),
        url((String) json.get("zipball_url")),
        (String) json.get("body")
    );
  }

  @Override
  public String toString() {
    return String.format("Release %s", tagName);
  }

  @Override
  public int compareTo(Release o) {
    return version.compareTo(o.version);
  }

  public boolean isNotLegacy() {
    return !version.isLegacy();
  }

  public boolean isNotBeta() {
    return !version.isBeta();
  }

  public boolean isUpdateable() {
    // Hard lower boundary for ODK Aggregate v2 or greater versions
    // Older versions could not be updateable by this program
    return !version.isLegacy() && version.getMajor() >= 2;
  }

  public Version getVersion() {
    return version;
  }

  public ZonedDateTime getPublishedAt() {
    return publishedAt;
  }

  public boolean isVersion(Version version) {
    return this.version.equals(version);
  }
}
