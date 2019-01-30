package org.opendatakit.aggregateupdater;

import org.opendatakit.aggregateupdater.operations.ListAvailableVersions;
import org.opendatakit.aggregateupdater.reused.http.CommonsHttp;
import org.opendatakit.aggregateupdater.reused.http.Http;
import org.opendatakit.aggregateupdater.operations.UpdateOperation;
import org.opendatakit.cli.Cli;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Launcher {
  private static final Logger log = LoggerFactory.getLogger(Launcher.class);

  public static void main(String[] args) {
    Http http = new CommonsHttp();

    new Cli()
        .register(ListAvailableVersions.build(http))
        .register(UpdateOperation.build(http))
        .onError(t -> {
          log.error("Error executing ODK Aggregate Updater", t);
          System.exit(1);
        })
        .run(args);
  }
}
