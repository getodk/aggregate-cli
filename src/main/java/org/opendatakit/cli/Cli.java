/*
 * Copyright (C) 2018 Nafundi
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.opendatakit.cli;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toSet;
import static org.opendatakit.briefcase.buildconfig.BuildConfig.NAME;
import static org.opendatakit.briefcase.buildconfig.BuildConfig.VERSION;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.MissingArgumentException;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.UnrecognizedOptionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Cli is a command line adapter. It helps define executable operations and their
 * required and optional params.
 * <p>
 * It defines some default operations like "show help" and "show version"
 */
public class Cli {
  private static final Logger log = LoggerFactory.getLogger(Cli.class);
  private static final Param<Void> SHOW_HELP = Param.flag("h", "help", "Show help");
  private static final Param<Void> SHOW_VERSION = Param.flag("v", "version", "Show version");

  private final Set<Operation> requiredOperations = new HashSet<>();
  private final Set<Operation> operations = new HashSet<>();
  private final Set<BiConsumer<Cli, CommandLine>> otherwiseCallbacks = new HashSet<>();
  private final Set<Operation> executedOperations = new HashSet<>();

  private final List<Consumer<Throwable>> onErrorCallbacks = new ArrayList<>();

  public Cli() {
    register(Operation.of(SHOW_HELP, (console, args) -> printHelp()));
    register(Operation.of(SHOW_VERSION, (console, args) -> printVersion()));
  }

  /**
   * Prints the help message with all the registered operations and their paramsº
   */
  public void printHelp() {
    CustomHelpFormatter.printHelp(requiredOperations, operations);
  }

  /**
   * Marks a Param for deprecation and assigns an alternative operation.
   * <p>
   * When Briefcase detects this param, it will show a message, output the help and
   * exit with a non-zero status
   */
  public Cli deprecate(Param<?> oldParam, Operation alternative) {
    operations.add(Operation.deprecated(oldParam, (console, args) -> {
      log.warn("Trying to run deprecated param -{}", oldParam.shortCode);
      System.out.println("The param -" + oldParam.shortCode + " has been deprecated. Run Briefcase again with -" + alternative.param.shortCode + " instead");
      printHelp();
      System.exit(1);
    }));
    return this;
  }

  public Cli register(Operation operation) {
    operations.add(operation);
    return this;
  }

  /**
   * Register a {@link Runnable} block that will be executed if no {@link Operation}
   * is executed. For example, if the user passes no arguments when executing this program
   */
  public Cli otherwise(BiConsumer<Cli, CommandLine> callback) {
    otherwiseCallbacks.add(callback);
    return this;
  }

  /**
   * Runs the command line program
   */
  public void run(String[] args) {
    Set<Param> allParams = getAllParams();
    CommandLine cli = getCli(args, allParams);
    Console console = Console.std();
    try {
      requiredOperations.forEach(operation -> {
        checkForMissingParams(cli, operation.requiredParams);
        operation.accept(console, Args.from(cli, operation.requiredParams));
      });

      operations.forEach(operation -> {
        if (cli.hasOption(operation.param.shortCode)) {
          checkForMissingParams(cli, operation.requiredParams);
          operation.accept(console, Args.from(cli, operation.getAllParams()));
          executedOperations.add(operation);
        }
      });

      if (executedOperations.isEmpty())
        otherwiseCallbacks.forEach(callback -> callback.accept(this, cli));
    } catch (Throwable t) {
      if (!onErrorCallbacks.isEmpty())
        onErrorCallbacks.forEach(callback -> callback.accept(t));
      else {
        System.err.println("Error: " + t.getMessage());
        System.err.println("No error callbacks have been defined");
        log.error("Error", t);
        System.exit(1);
      }
    }
  }

  /**
   * This method lets third parties react when the launched operations produce an
   * uncaught exception that raises up to this class.
   */
  public Cli onError(Consumer<Throwable> callback) {
    onErrorCallbacks.add(callback);
    return this;
  }

  private Set<Param> getAllParams() {
    return Stream.of(
        requiredOperations.stream().flatMap(operation -> operation.requiredParams.stream()),
        operations.stream().flatMap(operation -> operation.getAllParams().stream())
    ).flatMap(Function.identity()).collect(toSet());
  }

  private CommandLine getCli(String[] args, Set<Param> params) {
    try {
      return new DefaultParser().parse(mapToOptions(params), args, false);
    } catch (UnrecognizedOptionException | MissingArgumentException e) {
      System.err.println("Error: " + e.getMessage());
      log.error("Error", e);
      printHelp();
      System.exit(1);
      return null;
    } catch (Throwable t) {
      t.printStackTrace();
      System.exit(1);
      return null;
    }
  }

  private void checkForMissingParams(CommandLine cli, Set<Param> paramsToCheck) {
    Set<Param> missingParams = paramsToCheck.stream().filter(param -> !cli.hasOption(param.shortCode)).collect(toSet());
    if (!missingParams.isEmpty()) {
      System.out.print("Missing params: ");
      System.out.print(missingParams.stream().map(param -> "-" + param.shortCode).collect(joining(", ")));
      System.out.println("");
      printHelp();
      System.exit(1);
    }
  }

  static Options mapToOptions(Set<Param> params) {
    Options options = new Options();
    params.forEach(param -> options.addOption(param.option));
    return options;
  }

  private static void printVersion() {
    System.out.println(NAME + " " + VERSION);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Cli cli = (Cli) o;
    return Objects.equals(requiredOperations, cli.requiredOperations) &&
        Objects.equals(operations, cli.operations) &&
        Objects.equals(otherwiseCallbacks, cli.otherwiseCallbacks) &&
        Objects.equals(executedOperations, cli.executedOperations);
  }

  @Override
  public int hashCode() {
    return Objects.hash(requiredOperations, operations, otherwiseCallbacks, executedOperations);
  }

  @Override
  public String toString() {
    return "Cli{" +
        "requiredOperations=" + requiredOperations +
        ", operations=" + operations +
        ", otherwiseCallbacks=" + otherwiseCallbacks +
        ", executedOperations=" + executedOperations +
        '}';
  }
}
