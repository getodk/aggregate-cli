package org.opendatakit.aggregateupdater.operations;

import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.LONG;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.opendatakit.aggregateupdater.operations.CommonParams.INCLUDE_BETA_VERSIONS;
import static org.opendatakit.cli.Param.flag;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.opendatakit.aggregateupdater.reused.http.Http;
import org.opendatakit.aggregateupdater.reused.releases.Release;
import org.opendatakit.aggregateupdater.reused.releases.ReleaseQueries;
import org.opendatakit.cli.Args;
import org.opendatakit.cli.Console;
import org.opendatakit.cli.Operation;

public class ListAvailableVersions {

  public static Operation build(Http http) {
    return Operation.of(
        flag("l", "list", "List available versions"),
        (console, args) -> execute(http, console, args),
        emptyList(),
        singletonList(INCLUDE_BETA_VERSIONS)
    );
  }

  private static void execute(Http http, Console console, Args args) {
    List<Release> releases = http.execute(ReleaseQueries.all()).orElse(emptyList());

    if (releases.isEmpty()) {
      console.out("No releases are available at this moment. Please try again after some time");
      console.exit();
    }

    console.out("List of available releases:");
    console.table(releases.stream()
        .filter(Release::isNotLegacy)
        .filter(Release::isUpdateable)
        .filter(r -> args.has(INCLUDE_BETA_VERSIONS) || r.isNotBeta())
        .sorted(((Comparator<Release>) Release::compareTo).reversed())
        .map(r -> Arrays.asList(r.getVersion().toString(), r.getPublishedAt().format(ofLocalizedDateTime(LONG))))
        .collect(Collectors.toList()), "Version", "Publish date");
  }


}
