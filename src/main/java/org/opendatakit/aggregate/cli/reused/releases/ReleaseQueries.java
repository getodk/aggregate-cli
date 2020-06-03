package org.opendatakit.aggregate.cli.reused.releases;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.opendatakit.aggregate.cli.reused.http.HttpHelpers.url;

import java.util.List;
import org.opendatakit.aggregate.cli.reused.http.Request;

public class ReleaseQueries {
  public static Request<List<Release>> all(boolean includePreReleases) {
    return Request.getJsonList(url("https://api.github.com/repos/getodk/aggregate/releases"))
        .header("Accept", "application/vnd.github.v3+json")
        .header("User-Agent", "Aggregate Updater")
        .withMapper(jsonObjects -> jsonObjects.stream()
            .map(Release::from)
            .filter(Release::isUpdateable)
            .filter(r -> includePreReleases || !r.isPreRelease())
            .sorted(reverseOrder())
            .collect(toList()));
  }

  public static Request<Release> latest(boolean includePreReleases) {
    return all(includePreReleases).withMapper(releases -> releases.get(0));
  }
}
