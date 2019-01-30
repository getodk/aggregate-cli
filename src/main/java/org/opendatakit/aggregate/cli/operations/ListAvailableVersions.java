package org.opendatakit.aggregate.cli.operations;

import static java.lang.String.format;
import static java.time.format.DateTimeFormatter.ofLocalizedDateTime;
import static java.time.format.FormatStyle.LONG;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.opendatakit.aggregate.cli.operations.CommonParams.ALWAYS_YES;
import static org.opendatakit.aggregate.cli.operations.CommonParams.INCLUDE_PRE_RELEASES;
import static org.opendatakit.aggregate.cli.operations.CommonParams.VERBOSE;
import static org.opendatakit.cli.Param.flag;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.opendatakit.aggregate.cli.reused.http.Http;
import org.opendatakit.aggregate.cli.reused.releases.Release;
import org.opendatakit.aggregate.cli.reused.releases.ReleaseQueries;
import org.opendatakit.cli.Args;
import org.opendatakit.cli.Console;
import org.opendatakit.cli.Operation;

public class ListAvailableVersions {

  public static Operation build(Http http) {
    return Operation.of(
        flag("l", "list", "List available versions"),
        (console, args) -> execute(http, console, args),
        emptyList(),
        singletonList(INCLUDE_PRE_RELEASES)
    );
  }

  private static void execute(Http http, Console console, Args args) {
    console.setVerboseMode(args.has(VERBOSE));
    console.setAlwaysYesMode(args.has(ALWAYS_YES));

    console.out("List of available releases:");
    console.out();

    List<Release> releases = http.execute(ReleaseQueries.all(args.has(INCLUDE_PRE_RELEASES))).orElse(emptyList());

    if (releases.isEmpty()) {
      console.out(format(
          "No releases are available at this moment. Run with %s to include pre-release versions",
          INCLUDE_PRE_RELEASES.getShortCodeSyntax()
      ));
      console.exit();
    }

    console.table(releases.stream()
        .filter(Release::isNotLegacy)
        .filter(Release::isUpdateable)
        .sorted(((Comparator<Release>) Release::compareTo).reversed())
        .map(r -> Arrays.asList(r.getVersion().toString(), r.getPublishedAt().format(ofLocalizedDateTime(LONG))))
        .collect(Collectors.toList()), "Version", "Publish date");
  }

}
