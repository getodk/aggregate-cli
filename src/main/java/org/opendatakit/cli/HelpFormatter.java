package org.opendatakit.cli;

import static java.util.Comparator.comparing;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static org.opendatakit.cli.Cli.mapToOptions;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Stream;
import org.apache.commons.cli.Options;

class HelpFormatter {
  private final String name;

  HelpFormatter(String name) {
    this.name = name;
  }

  public String renderHelp(Set<Operation> requiredOperations, Set<Operation> operations) {
    List<String> helpLines = new ArrayList<>();
    helpLines.add("");
    helpLines.add("Launch an operation with: " + name + " <operation> <params>");
    helpLines.add("");
    Map<String, String> helpLinesPerShortcode = getParamHelpLines(requiredOperations, operations);
    if (!requiredOperations.isEmpty())
      helpLines.addAll(renderRequiredParams(helpLinesPerShortcode, requiredOperations));
    helpLines.addAll(renderAvailableOperations(helpLinesPerShortcode, operations));
    helpLines.addAll(renderParamsPerOperation(helpLinesPerShortcode, operations));
    return String.join("\n", helpLines);
  }

  private static Map<String, String> getParamHelpLines(Set<Operation> requiredOperations, Set<Operation> operations) {
    Set<Param> allParams = Stream.of(
        requiredOperations.stream().flatMap(operation -> operation.requiredParams.stream()),
        operations.stream().flatMap(operation -> operation.getAllParams().stream())
    ).flatMap(Function.identity()).collect(toSet());
    Options options = mapToOptions(allParams);

    StringWriter out = new StringWriter();
    PrintWriter pw = new PrintWriter(out);
    org.apache.commons.cli.HelpFormatter helpFormatter = new org.apache.commons.cli.HelpFormatter();
    helpFormatter.printHelp(pw, 999, "ignore", "ignore", options, 0, 4, "ignore");
    return Stream.of(out.toString().split("\n"))
        .filter(line -> line.startsWith("-") && line.contains(","))
        .collect(toMap(
            line -> line.substring(1, line.indexOf(",")),
            Function.identity()
        ));
  }

  private static List<String> renderRequiredParams(Map<String, String> helpLinesPerShortcode, Set<Operation> requiredOperations) {
    List<String> lines = new ArrayList<>();
    lines.add("Required params:");
    lines.addAll(requiredOperations.stream()
        .flatMap(operation -> operation.requiredParams.stream())
        .sorted(comparing(param -> param.shortCode))
        .map(param -> helpLinesPerShortcode.get(param.shortCode))
        .collect(toList()));
    lines.add("");
    return lines;
  }

  private static List<String> renderAvailableOperations(Map<String, String> helpLinesPerShortcode, Set<Operation> operations) {
    List<String> lines = new ArrayList<>();
    lines.add("Available operations:");
    lines.addAll(operations.stream()
        .filter(o -> !o.isDeprecated())
        .sorted(comparing(operation -> operation.param.shortCode))
        .map(operation -> "  " + helpLinesPerShortcode.get(operation.param.shortCode))
        .collect(toList()));
    lines.add("");
    return lines;
  }

  private static List<String> renderParamsPerOperation(Map<String, String> helpLinesPerShortcode, Set<Operation> operations) {
    return operations.stream()
        .flatMap(operation -> {
          List<String> lines = new ArrayList<>();
          if (operation.hasAnyParam())
            lines.add("Params for -" + operation.param.shortCode + " operation:");
          if (operation.hasRequiredParams())
            lines.addAll(renderRequiredParams(helpLinesPerShortcode, operation));
          if (operation.hasOptionalParams())
            lines.addAll(renderOptionalParams(helpLinesPerShortcode, operation));
          if (operation.hasAnyParam())
            lines.add("");
          return lines.stream();
        })
        .collect(toList());
  }

  private static List<String> renderRequiredParams(Map<String, String> helpLinesPerShortcode, Operation operation) {
    return operation.requiredParams.stream()
        .sorted(comparing(param -> param.shortCode))
        .map(param -> "  " + helpLinesPerShortcode.get(param.shortCode))
        .collect(toList());
  }

  private static List<String> renderOptionalParams(Map<String, String> helpLinesPerShortcode, Operation operation) {
    List<String> lines = new ArrayList<>();
    lines.add("Optional params for -" + operation.param.shortCode + " operation:");
    lines.addAll(operation.optionalParams.stream()
        .sorted(comparing(param -> param.shortCode))
        .map(param -> "  " + helpLinesPerShortcode.get(param.shortCode))
        .collect(toList()));
    return lines;
  }

}
