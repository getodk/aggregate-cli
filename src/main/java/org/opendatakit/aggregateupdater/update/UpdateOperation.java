package org.opendatakit.aggregateupdater.update;

import static java.lang.String.format;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.opendatakit.aggregateupdater.listversions.ListAvailableVersions.INCLUDE_BETA_VERSIONS;
import static org.opendatakit.aggregateupdater.reused.Optionals.race;
import static org.opendatakit.cli.Param.arg;
import static org.opendatakit.cli.Param.flag;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.opendatakit.aggregateupdater.releases.Release;
import org.opendatakit.aggregateupdater.releases.ReleaseQueries;
import org.opendatakit.aggregateupdater.releases.Version;
import org.opendatakit.aggregateupdater.reused.http.Http;
import org.opendatakit.cli.Args;
import org.opendatakit.cli.Console;
import org.opendatakit.cli.Operation;
import org.opendatakit.cli.Param;

public class UpdateOperation {

  private static final Param<Version> REQUESTED_VERSION = arg("rv", "requested-version", "Requested version (latest by default)", Version::from);
  private static final Param<Void> DRY_RUN = flag("dr", "dry-run", "Dry run (emulate update process)");
  private static final Param<Void> FORCE = flag("f", "force", "Force update");
  private static final Param<Void> ALWAYS_YES = flag("y", "yes", "Always answer 'yes' to confirm prompts");
  private static final Param<Void> VERBOSE = flag("vv", "verbose", "Verbose mode. Shows all commands");
  private static final Path USER_HOME = Paths.get(System.getProperty("user.home")).toAbsolutePath();
  private static final Path VERSION_FILE = Paths.get("/var/lib/aggregate-version.txt");
  private static final Path BACKUP = USER_HOME.resolve("aggregate-backup");
  private static final Path BACKUP_CONF = BACKUP.resolve("conf");
  private static final Path BACKUP_WEBAPP = BACKUP.resolve("webapp");
  private static final Path TOMCAT_HOME = Paths.get("/var/lib/tomcat8");
  private static final Path DEPLOYED_WEBAPP = TOMCAT_HOME.resolve("webapps/ROOT");
  private static final Path DEPLOYED_CONF = DEPLOYED_WEBAPP.resolve("WEB-INF/classes");
  private static final Path JDBC_CONF = Paths.get("jdbc.properties");
  private static final Path JDBC_CONF_ORIGINAL = Paths.get("jdbc.properties.original");
  private static final Path SECURITY_CONF = Paths.get("security.properties");
  private static final Path SECURITY_CONF_ORIGINAL = Paths.get("security.properties.original");

  public static Operation build(Http http) {
    return Operation.of(
        flag("u", "update", "Update ODK Aggregate"),
        (console, args) -> execute(http, console, args),
        emptyList(),
        asList(DRY_RUN, REQUESTED_VERSION, INCLUDE_BETA_VERSIONS, FORCE, ALWAYS_YES, VERBOSE)
    );
  }

  private static void execute(Http http, Console console, Args args) {
    console.setVerboseMode(args.has(VERBOSE));
    console.setDryRunMode(args.has(DRY_RUN));
    console.setAlwaysYesMode(args.has(ALWAYS_YES));

    console.out("Update ODK Aggregate");
    console.out();

    Version installedVersion = Version.from(new String(readAllBytes(VERSION_FILE), UTF_8).trim());
    Optional<Version> requestedVersion = args.getOptional(REQUESTED_VERSION);
    List<Release> availableReleases = http.execute(ReleaseQueries.all()).orElse(emptyList());
    Optional<Version> latestVersion = availableReleases.stream()
        .filter(r -> args.has(INCLUDE_BETA_VERSIONS) || r.isNotBeta())
        .map(Release::getVersion)
        .max(Version::compareTo);

    requestedVersion.ifPresent(version -> {
      if (availableReleases.stream().noneMatch(release -> release.isVersion(version))) {
        console.error("Requested version " + version + " doesn't exist.");
        console.error("Please choose one between: ");
        availableReleases.forEach(r -> console.error("\t- " + r.getVersion().toString()));
        console.exit(1);
      }
    });

    Optional<Version> maybeSelectedVersion = race(requestedVersion, latestVersion);
    if (!maybeSelectedVersion.isPresent()) {
      console.error("Can't install the selected version");
      console.exit(1);
    }

    Version selectedVersion = maybeSelectedVersion.orElseThrow(RuntimeException::new);

    console.out("Installed version: " + installedVersion);
    console.out("Requested version: " + requestedVersion.map(Version::toString).orElse("None (defaults to latest available)"));
    console.out("   Latest version: " + latestVersion.orElseThrow(RuntimeException::new));
    console.out(" Selected version: " + selectedVersion.toString());
    console.out();

    if (selectedVersion.equals(installedVersion) && !args.has(FORCE)) {
      console.out("No action needed");
      console.exit();
    }

    if (selectedVersion.equals(installedVersion) && args.has(FORCE)) {
      console.out("(forcing the update)");
      console.out();
    }

    console.out("- Stopping Tomcat");
    console.execute("service tomcat8 stop");
    console.out("  done");
    console.out();

    console.out("- Backing up installed Aggregate");
    console.execute(format("mkdir -p %s", BACKUP_CONF));
    console.execute(format("cp %s %s", DEPLOYED_CONF.resolve(JDBC_CONF), BACKUP_CONF.resolve(JDBC_CONF)));
    console.execute(format("cp %s %s", DEPLOYED_CONF.resolve(SECURITY_CONF), BACKUP_CONF.resolve(SECURITY_CONF)));
    console.execute(format("mkdir -p %s", BACKUP_WEBAPP));
    console.execute(format(
        "zip -q -r %s/ODK-Aggregate-backup-%s.zip %s",
        BACKUP_WEBAPP,
        LocalDate.now().format(ISO_LOCAL_DATE),
        DEPLOYED_WEBAPP
    ));
    console.out("  done");
    console.out();


    console.out("- Deploying selected Aggregate version");
    console.execute(format("rm -r %s", DEPLOYED_WEBAPP));
    console.execute(format("mkdir -p %s", DEPLOYED_WEBAPP));
    console.execute(format(
        "wget -O /tmp/aggregate.war https://github.com/opendatakit/aggregate/releases/download/%s/ODK-Aggregate-%s.war",
        selectedVersion,
        selectedVersion
    ));
    console.execute(format("unzip -qq /tmp/aggregate.war -d %s", DEPLOYED_WEBAPP));
    console.execute(format("cp %s %s", DEPLOYED_CONF.resolve(JDBC_CONF), DEPLOYED_CONF.resolve(JDBC_CONF_ORIGINAL)));
    console.execute(format("cp %s %s", DEPLOYED_CONF.resolve(SECURITY_CONF), DEPLOYED_CONF.resolve(SECURITY_CONF_ORIGINAL)));
    console.execute(format("sed -i s/^#.*$//g %s", DEPLOYED_CONF.resolve(SECURITY_CONF_ORIGINAL)));
    console.execute(format("cp %s %s", BACKUP_CONF.resolve(JDBC_CONF), DEPLOYED_CONF.resolve(JDBC_CONF)));
    console.execute(format("cp %s %s", BACKUP_CONF.resolve(SECURITY_CONF), DEPLOYED_CONF.resolve(SECURITY_CONF)));
    console.execute(format("chown -R tomcat8:tomcat8 %s", DEPLOYED_WEBAPP));
    console.out("  done");
    console.out();

    console.out("- Cleaning up update assets");
    console.execute("rm /tmp/aggregate.war", true);
    console.execute(format("rm -rf %s", BACKUP_CONF), true);
    console.out("  done");
    console.out();

    // Diff conf files and ask for confirmation
    console.out();
    console.out("Update completed");
    console.out("Please, check the differences between the deployed (+) and original (-) configurations");
    console.out();
    console.execute(format("diff -u --color -B -w %s %s", DEPLOYED_CONF.resolve(JDBC_CONF), DEPLOYED_CONF.resolve(JDBC_CONF_ORIGINAL)), true);
    console.execute(format("diff -u --color -B -w %s %s", DEPLOYED_CONF.resolve(SECURITY_CONF), DEPLOYED_CONF.resolve(SECURITY_CONF_ORIGINAL)), true);

    // Write version in version file
    write(VERSION_FILE, selectedVersion.toString().getBytes(UTF_8), StandardOpenOption.TRUNCATE_EXISTING);

    console.out();
    if (console.confirm("Start Tomcat?")) {
      console.out("- Starting Tomcat");
      console.execute("service tomcat8 start");
      console.out("  done");
      console.out();
    }

    console.exit();
  }

  private static void write(Path path, byte[] bytes, OpenOption... options) {
    try {
      Files.write(path, bytes, options);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static byte[] readAllBytes(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
