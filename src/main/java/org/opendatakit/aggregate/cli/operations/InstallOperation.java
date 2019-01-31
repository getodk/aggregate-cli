package org.opendatakit.aggregate.cli.operations;

import static java.lang.String.format;
import static java.nio.file.Files.exists;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.opendatakit.aggregate.cli.operations.CommonParams.ALWAYS_YES;
import static org.opendatakit.aggregate.cli.operations.CommonParams.CONFIGURATION_PATH;
import static org.opendatakit.aggregate.cli.operations.CommonParams.FORCE;
import static org.opendatakit.aggregate.cli.operations.CommonParams.INCLUDE_PRE_RELEASES;
import static org.opendatakit.aggregate.cli.operations.CommonParams.VERBOSE;
import static org.opendatakit.aggregate.cli.operations.Install.install;
import static org.opendatakit.cli.Param.arg;
import static org.opendatakit.cli.Param.flag;

import java.util.Optional;
import org.opendatakit.aggregate.cli.operations.Exceptions.OperationException;
import org.opendatakit.aggregate.cli.reused.Pair;
import org.opendatakit.aggregate.cli.reused.http.Http;
import org.opendatakit.aggregate.cli.reused.releases.Release;
import org.opendatakit.aggregate.cli.reused.releases.ReleaseQueries;
import org.opendatakit.aggregate.cli.reused.releases.Version;
import org.opendatakit.cli.Args;
import org.opendatakit.cli.Console;
import org.opendatakit.cli.Operation;
import org.opendatakit.cli.Param;

public class InstallOperation {
  private static final Param<String> CUSTOM_URL = arg(
      "cu",
      "custom-url",
      "Custom URL to download the Aggregate WAR package"
  );
  private static final Param<Version> CUSTOM_VERSION = arg(
      "cv",
      "custom-version",
      "Version that the custom Aggregate WAR URL corresponds to",
      Version::from
  );

  public static Operation build(Http http) {
    return Operation.of(
        flag("i", "install", "Install ODK Aggregate"),
        (console, args) -> execute(http, console, args),
        singletonList(CONFIGURATION_PATH),
        asList(INCLUDE_PRE_RELEASES, ALWAYS_YES, FORCE, VERBOSE, CUSTOM_URL, CUSTOM_VERSION)
    );
  }

  private static void execute(Http http, Console console, Args args) {
    console.setVerboseMode(args.has(VERBOSE));
    console.setAlwaysYesMode(args.has(ALWAYS_YES));

    console.requireSuperuser();

    // Parse the configuration and check some flag/arg combinations before starting
    EnvironmentConfiguration conf = args.get(CONFIGURATION_PATH);

    if (exists(conf.getVersionFilePath()) && !args.has(FORCE))
      throw new OperationException(format("Aggregate %s is already installed. Add -f to force the installation.",
          Version.read(conf.getVersionFilePath())
      ));

    if (args.getOptional(CUSTOM_URL).isPresent() && !args.getOptional(CUSTOM_VERSION).isPresent())
      throw new OperationException(format("You need to provide both %s and %s params",
          CUSTOM_URL.getShortCodeSyntax(),
          CUSTOM_VERSION.getShortCodeSyntax()
      ));

    // Inform the user and ask for confirmation
    console.out("Installing ODK Aggregate");
    console.out();
    console.out("Please, read carefully:");
    console.out("- The ROOT webapp will be replaced with the latest available Aggregate release or a custom build you provide.");
    console.out("- Tomcat will be stopped during the whole process.");
    console.out();
    if (!console.confirm("Are you ready to continue?"))
      console.exit();

    // Do the installation
    Pair<Version, String> selectedVersion = resolveSelectedVersion(http, args);

    console.out("Installing Aggregate " + selectedVersion.getLeft() + " from " + selectedVersion.getRight());
    console.out();

    install(console, conf, selectedVersion.getLeft(), selectedVersion.getRight());

    console.exit();
  }

  private static Pair<Version, String> resolveSelectedVersion(Http http, Args args) {
    Optional<String> maybeCustomUrl = args.getOptional(CUSTOM_URL).filter(s -> !s.trim().isEmpty());
    Optional<Version> maybeCustomVersion = args.getOptional(CUSTOM_VERSION);
    if (maybeCustomVersion.isPresent() && maybeCustomUrl.isPresent())
      return Pair.of(maybeCustomVersion.get(), maybeCustomUrl.get());

    Version latestVersion = http.execute(ReleaseQueries.latest(args.has(INCLUDE_PRE_RELEASES))
        .withMapper(Release::getVersion))
        .orElseThrow(() -> new OperationException(format(
            "No available versions found. Run with %s to include pre-release versions",
            INCLUDE_PRE_RELEASES.getShortCodeSyntax()
        )));
    return Pair.of(latestVersion, latestVersion.buildGitHubDownloadUrl());
  }

  public static String getConfigurationTemplate() {
    return EnvironmentConfiguration.renderConfigurationTemplate();
  }
}
