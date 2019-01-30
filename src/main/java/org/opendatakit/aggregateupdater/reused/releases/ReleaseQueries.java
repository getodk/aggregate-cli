package org.opendatakit.aggregateupdater.reused.releases;

import static java.util.Comparator.reverseOrder;
import static java.util.stream.Collectors.toList;
import static org.opendatakit.aggregateupdater.reused.http.HttpHelpers.url;

import java.util.List;
import org.opendatakit.aggregateupdater.reused.http.Request;

public class ReleaseQueries {
  public static Request<List<Release>> all() {
    return Request.getJsonList(url("https://api.github.com/repos/opendatakit/aggregate/releases"))
        .header("Accept", "application/vnd.github.v3+json")
        .header("User-Agent", "Aggregate Updater")
        .withMapper(jsonObjects -> jsonObjects.stream()
            .map(Release::from)
            .filter(Release::isUpdateable)
            .sorted(reverseOrder())
            .collect(toList()));
  }

  public static Request<Release> latest() {
    return all().withMapper(releases -> releases.get(0));
  }
}
