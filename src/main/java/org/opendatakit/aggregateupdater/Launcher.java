package org.opendatakit.aggregateupdater;

import org.opendatakit.cli.Cli;

public class Launcher {
  public static void main(String[] args) {
    new Cli()
        .run(args);
  }
}
