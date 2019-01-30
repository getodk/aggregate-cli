package org.opendatakit.aggregateupdater.operations;

import static org.opendatakit.cli.Param.flag;

import org.opendatakit.cli.Param;

public final class CommonParams {
  static final Param<Void> FORCE = flag("f", "force", "Force update");
  static final Param<Void> ALWAYS_YES = flag("y", "yes", "Always answer 'yes' to confirm prompts");
  static final Param<Void> VERBOSE = flag("vv", "verbose", "Verbose mode. Shows all commands");
  static final Param<Void> INCLUDE_BETA_VERSIONS = Param.flag("ib", "include-beta", "Include beta versions");
}
