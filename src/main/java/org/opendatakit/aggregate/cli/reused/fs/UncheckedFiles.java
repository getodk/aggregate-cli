package org.opendatakit.aggregate.cli.reused.fs;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;

public final class UncheckedFiles {
  public static byte[] readAllBytes(Path path) {
    try {
      return Files.readAllBytes(path);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static void write(Path path, byte[] bytes, OpenOption... options) {
    try {
      Files.write(path, bytes, options);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public static Path createTempDirectory(String prefix) {
    try {
      return Files.createTempDirectory(prefix);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
