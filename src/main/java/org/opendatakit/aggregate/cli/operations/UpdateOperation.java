package org.opendatakit.aggregate.cli.operations;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.time.LocalDateTime.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.joining;
import static org.opendatakit.aggregate.cli.operations.CommonParams.ALWAYS_YES;
import static org.opendatakit.aggregate.cli.operations.CommonParams.CONFIGURATION_PATH;
import static org.opendatakit.aggregate.cli.operations.CommonParams.FORCE;
import static org.opendatakit.aggregate.cli.operations.CommonParams.INCLUDE_PRE_RELEASES;
import static org.opendatakit.aggregate.cli.operations.CommonParams.VERBOSE;
import static org.opendatakit.aggregate.cli.operations.Install.install;
import static org.opendatakit.aggregate.cli.reused.Optionals.race;
import static org.opendatakit.cli.Param.arg;
import static org.opendatakit.cli.Param.flag;

import java.util.List;
import java.util.Optional;
import org.opendatakit.aggregate.cli.reused.http.Http;
import org.opendatakit.aggregate.cli.reused.releases.Release;
import org.opendatakit.aggregate.cli.reused.releases.ReleaseQueries;
import org.opendatakit.aggregate.cli.reused.releases.Version;
import org.opendatakit.cli.Args;
import org.opendatakit.cli.Console;
import org.opendatakit.cli.Operation;
import org.opendatakit.cli.Param;

public class UpdateOperation {
  private static final Param<Version> REQUESTED_VERSION = arg("rv", "requested-version", "Requested version (latest by default)", Version::from);

  public static Operation build(Http http) {
    return Operation.of(
        flag("u", "update", "Update ODK Aggregate"),
        (console, args) -> execute(http, console, args),
        singletonList(CONFIGURATION_PATH),
        asList(REQUESTED_VERSION, INCLUDE_PRE_RELEASES, FORCE, ALWAYS_YES, VERBOSE)
    );
  }

  private static void execute(Http http, Console console, Args args) {
    console.setVerboseMode(args.has(VERBOSE));
    console.setAlwaysYesMode(args.has(ALWAYS_YES));

    console.requireSuperuser();

    // Parse the configuration and check some flag/arg combinations before starting
    EnvironmentConfiguration conf = args.get(CONFIGURATION_PATH);

    if (!exists(conf.getVersionFilePath()))
      throw new Exceptions.OperationException("Aggregate is not installed. Run the install (-i) operation instead");

    // Check versions
    Version installedVersion = Version.read(conf.getVersionFilePath());
    Version selectedVersion = resolveSelectedVersion(http, args);
    if (installedVersion.equals(selectedVersion) && !args.has(FORCE))
      throw new Exceptions.OperationException(format(
          "Aggregate %s is already installed. Run this operation with -f to force the update",
          installedVersion
      ));

    // Inform the user and ask for confirmation
    console.out("Updating ODK Aggregate");
    console.out();
    console.out("Please, read carefully:");
    console.out("- A backup of the currently deployed Aggregate will be created at " + conf.getBackupPath());
    console.out("- The ROOT webapp will be replaced with the selected Aggregate release.");
    console.out("- Tomcat will be stopped during the whole process.");
    console.out();
    console.out("Version information:");
    console.out("- Currently installed: " + installedVersion);
    console.out("- Selected for the update: " + selectedVersion);
    console.out();
    if (!console.confirm("Are you ready to continue?"))
      console.exit();

    // Backup aggregate
    console.block("Backing up Aggregate", () -> {
      console.execute(format("mkdir -p %s", conf.getBackupPath()), true);
      console.execute(format(
          "zip -q -r %s %s",
          conf.buildBackupFilePath(now()),
          conf.getRootWebappPath()
      ));
    });

    // Install selected version
    install(console, conf, selectedVersion, selectedVersion.buildGitHubDownloadUrl());

    console.exit();
  }

  private static Version resolveSelectedVersion(Http http, Args args) {
    Optional<Version> requestedVersion = args.getOptional(REQUESTED_VERSION);
    List<Release> availableReleases = http.execute(ReleaseQueries.all(args.has(INCLUDE_PRE_RELEASES))).orElse(emptyList());
    Optional<Version> latestVersion = availableReleases.stream()
        .map(Release::getVersion)
        .max(Version::compareTo);

    requestedVersion.ifPresent(version -> {
      if (availableReleases.stream().noneMatch(release -> release.isVersion(version)))
        throw new Exceptions.OperationException(format(
            "Requested version %s is not available. Please choose one between: %s",
            version,
            availableReleases.stream().map(r -> r.getVersion().toString()).collect(joining(", "))
        ));
    });

    return race(requestedVersion, latestVersion)
        .orElseThrow(() -> new Exceptions.OperationException(format(
            "No available released versions have been found. Run with %s to include pre-release versions",
            INCLUDE_PRE_RELEASES.getShortCodeSyntax()
        )));
  }

}
