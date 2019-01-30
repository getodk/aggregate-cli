package org.opendatakit.aggregate.cli.reused;

import java.util.Optional;
import java.util.function.Supplier;

public class Optionals {
  /**
   * Returns the first (the winner of the "race") {@link Optional} in the array that is present.
   */
  @SafeVarargs
  public static <T> Optional<T> race(Optional<T>... optionals) {
    for (Optional<T> maybeT : optionals)
      if (maybeT.isPresent())
        return maybeT;
    return Optional.empty();
  }

  /**
   * Returns the first (the winner of the "race") {@link Optional} in the array that is present.
   * <p>
   * Values are evaluated in order. Not all suppliers might be evaluated.
   */

  @SafeVarargs
  public static <T> Optional<T> race(Supplier<Optional<T>>... optionalSuppliers) {
    for (Supplier<Optional<T>> supplier : optionalSuppliers) {
      Optional<T> maybeT = supplier.get();
      if (maybeT.isPresent())
        return maybeT;
    }
    return Optional.empty();
  }
}
