package org.opendatakit.aggregate.cli;


import static org.opendatakit.aggregate.cli.operations.CommonParams.CONFIGURATION_PATH;

import org.opendatakit.aggregate.cli.operations.Exceptions.ConfigurationException;
import org.opendatakit.aggregate.cli.operations.InstallOperation;
import org.opendatakit.aggregate.cli.operations.ListAvailableVersions;
import org.opendatakit.aggregate.cli.operations.UpdateOperation;
import org.opendatakit.aggregate.cli.reused.http.CommonsHttp;
import org.opendatakit.aggregate.cli.reused.http.Http;
import org.opendatakit.cli.Cli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
  private static final Logger log = LoggerFactory.getLogger(Launcher.class);

  public static void main(String[] args) {
    Http http = new CommonsHttp();

    Cli.std("aggregate-cli")
        .register(ListAvailableVersions.build(http))
        .register(UpdateOperation.build(http))
        .register(InstallOperation.build(http))
        .onMissingParam((params, console) -> {
          if (params.contains(CONFIGURATION_PATH))
            throw new ConfigurationException("Configuration not provided");
        })
        .onError((t, console) -> {
          if (t instanceof ConfigurationException) {
            console.error("Configuration error: " + t.getMessage());
            console.error();
            console.error("Use this template to produce a file compatible with the " + CONFIGURATION_PATH.getShortCodeSyntax() + " param:");
            console.error(InstallOperation.getConfigurationTemplate());
          } else
            console.error("Error: " + t.getMessage());
          console.error();
        })
        .run(args);
  }
}
