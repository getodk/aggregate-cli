package org.opendatakit.aggregateupdater.releases;

import static org.opendatakit.aggregateupdater.reused.HttpHelpers.url;

import java.net.URL;
import java.util.Map;
import java.util.Optional;

public class Author {
  private final String login;
  private final int id;
  private final String nodeId;
  private final URL avatarUrl;
  private final Optional<String> gravatarId;
  private final URL url;
  private final URL htmlUrl;
  private final URL followersUrl;
  private final URL followingUrl;
  private final URL gistsUrl;
  private final URL starredUrl;
  private final URL subscriptionsUrl;
  private final URL organizationsUrl;
  private final URL reposUrl;
  private final URL eventsUrl;
  private final URL receivedEventsUrl;
  private final String type;
  private final boolean siteAdmin;

  public Author(String login, int id, String nodeId, URL avatarUrl, Optional<String> gravatarId, URL url, URL htmlUrl, URL followersUrl, URL followingUrl, URL gistsUrl, URL starredUrl, URL subscriptionsUrl, URL organizationsUrl, URL reposUrl, URL eventsUrl, URL receivedEventsUrl, String type, boolean siteAdmin) {
    this.login = login;
    this.id = id;
    this.nodeId = nodeId;
    this.avatarUrl = avatarUrl;
    this.gravatarId = gravatarId;
    this.url = url;
    this.htmlUrl = htmlUrl;
    this.followersUrl = followersUrl;
    this.followingUrl = followingUrl;
    this.gistsUrl = gistsUrl;
    this.starredUrl = starredUrl;
    this.subscriptionsUrl = subscriptionsUrl;
    this.organizationsUrl = organizationsUrl;
    this.reposUrl = reposUrl;
    this.eventsUrl = eventsUrl;
    this.receivedEventsUrl = receivedEventsUrl;
    this.type = type;
    this.siteAdmin = siteAdmin;
  }

  public static Author from(Map<String, Object> json) {
    return new Author(
        (String) json.get("login"),
        (Integer) json.get("id"),
        (String) json.get("node_id"),
        url((String) json.get("avatar_url")),
        Optional.ofNullable((String) json.get("gravatar_id")),
        url((String) json.get("url")),
        url((String) json.get("html_url")),
        url((String) json.get("followers_url")),
        url((String) json.get("following_url")),
        url((String) json.get("gists_url")),
        url((String) json.get("starred_url")),
        url((String) json.get("subscriptions_url")),
        url((String) json.get("organizations_url")),
        url((String) json.get("repos_url")),
        url((String) json.get("events_url")),
        url((String) json.get("received_events_url")),
        (String) json.get("type"),
        (Boolean) json.get("site_admin")
    );
  }
}
