package org.opendatakit.aggregate.cli.operations;

import static java.lang.String.format;
import static org.opendatakit.aggregate.cli.reused.fs.UncheckedFiles.createTempDirectory;
import static org.opendatakit.aggregate.cli.reused.fs.UncheckedFiles.write;

import java.nio.file.Path;
import org.opendatakit.aggregate.cli.reused.releases.Version;
import org.opendatakit.cli.Console;

final class Install {
  static void install(Console console, EnvironmentConfiguration conf, Version version, String warUrl) {
    console.block("Stopping Tomcat", () -> stopTomcat(console));
    console.block("Cleaning ROOT webapp", () -> cleanRootWebapp(console, conf));
    console.block("Deploying Aggregate", () -> deploy(console, conf, warUrl));
    console.block("Deploying configuration", () -> deployConfiguration(conf));
    console.block("Completing installation & cleanup", () -> {
      fixWebappsPermissions(console, conf);
      writeVersion(conf, version);
    });
    console.block("Starting Tomcat", () -> startTomcat(console));
  }

  private static void startTomcat(Console console) {
    console.execute("service tomcat8 start", true);
  }

  private static void writeVersion(EnvironmentConfiguration conf, Version version) {
    write(conf.getVersionFilePath(), version.toString().getBytes());
  }

  private static void fixWebappsPermissions(Console console, EnvironmentConfiguration conf) {
    console.execute(format("chown -R %s:%s %s", conf.tomcat.uid, conf.tomcat.gid, conf.getRootWebappPath()), true);
  }

  private static void deployConfiguration(EnvironmentConfiguration conf) {
    write(conf.getJdbcConfigurationPath(), conf.renderJdbcConfiguration().getBytes());
    write(conf.getSecurityConfigurationPath(), conf.renderSecurityConfiguration().getBytes());
  }

  private static void stopTomcat(Console console) {
    console.execute("service tomcat8 stop", true);
  }

  private static void cleanRootWebapp(Console console, EnvironmentConfiguration conf) {
    console.execute(format("rm -rf %s", conf.getRootWebappPath().toAbsolutePath()));
  }

  private static void deploy(Console console, EnvironmentConfiguration conf, String warUrl) {
    Path tmpDir = createTempDirectory("aggregate-cli");
    Path tmpAggregateWar = tmpDir.resolve("aggregate.war");
    console.execute(format("wget -O %s %s", tmpAggregateWar, warUrl));
    console.execute(format("unzip -qq %s -d %s", tmpAggregateWar, conf.getRootWebappPath()));
    console.execute(format("rm -rf %s", tmpDir));
  }
}
