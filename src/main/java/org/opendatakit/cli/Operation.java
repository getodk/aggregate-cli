package org.opendatakit.cli;

import static java.util.Collections.emptySet;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;

public class Operation {
  final Param param;
  final BiConsumer<Console, Args> payload;
  final Set<Param> requiredParams;
  final Set<Param> optionalParams;
  private final boolean deprecated;

  private Operation(Param param, BiConsumer<Console, Args> payload, Set<Param> requiredParams, Set<Param> optionalParams, boolean deprecated) {
    this.param = param;
    this.payload = payload;
    this.requiredParams = requiredParams;
    this.optionalParams = optionalParams;
    this.deprecated = deprecated;
  }

  public static Operation of(Param param, BiConsumer<Console, Args> argsConsumer) {
    return new Operation(param, argsConsumer, emptySet(), emptySet(), false);
  }

  public static Operation of(Param param, BiConsumer<Console, Args> argsConsumer, List<Param> requiredParams) {
    return new Operation(param, argsConsumer, new HashSet<>(requiredParams), emptySet(), false);
  }

  public static Operation of(Param param, BiConsumer<Console, Args> argsConsumer, List<Param> requiredParams, List<Param> optionalParams) {
    return new Operation(param, argsConsumer, new HashSet<>(requiredParams), new HashSet<>(optionalParams), false);
  }

  static Operation deprecated(Param param, BiConsumer<Console, Args> argsConsumer) {
    return new Operation(param, argsConsumer, emptySet(), emptySet(), true);
  }

  Set<Param> getAllParams() {
    // We need this because java.util.xyz collections are mutable
    HashSet<Param> allParams = new HashSet<>();
    allParams.add(param);
    allParams.addAll(requiredParams);
    allParams.addAll(optionalParams);
    return allParams;
  }

  boolean hasAnyParam() {
    return hasRequiredParams() || hasOptionalParams();
  }

  boolean hasOptionalParams() {
    return !optionalParams.isEmpty();
  }

  boolean hasRequiredParams() {
    return !requiredParams.isEmpty();
  }

  boolean isDeprecated() {
    return deprecated;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Operation operation = (Operation) o;
    return deprecated == operation.deprecated &&
        Objects.equals(param, operation.param) &&
        Objects.equals(payload, operation.payload) &&
        Objects.equals(requiredParams, operation.requiredParams) &&
        Objects.equals(optionalParams, operation.optionalParams);
  }

  @Override
  public int hashCode() {
    return Objects.hash(param, payload, requiredParams, optionalParams, deprecated);
  }

  public void accept(Console console, Args args) {
    payload.accept(console, args);
  }
}
