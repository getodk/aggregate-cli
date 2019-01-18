package org.opendatakit.aggregateupdater.reused;

import java.util.Objects;
import java.util.function.Function;

public class Pair<T, U> {
  private final T left;
  private final U right;

  public Pair(T left, U right) {
    this.left = left;
    this.right = right;
  }

  public static <TT, UU> Pair<TT, UU> of(TT left, UU right) {
    return new Pair<>(left, right);
  }

  public T getLeft() {
    return left;
  }

  public U getRight() {
    return right;
  }

  public <TT, UU> Pair<TT, UU> map(Function<T, TT> leftMapper, Function<U, UU> rightMapper) {
    return new Pair<>(leftMapper.apply(left), rightMapper.apply(right));
  }

  @Override
  public String toString() {
    return "Pair{" +
        "left=" + left +
        ", right=" + right +
        '}';
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    Pair<?, ?> pair = (Pair<?, ?>) o;
    return Objects.equals(left, pair.left) &&
        Objects.equals(right, pair.right);
  }

  @Override
  public int hashCode() {
    return Objects.hash(left, right);
  }

}
