package org.opendatakit.aggregate.cli.reused.releases;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.opendatakit.aggregate.cli.reused.releases.Version.Type.LEGACY;
import static org.opendatakit.aggregate.cli.reused.releases.Version.Type.NORMAL;
import static org.opendatakit.aggregate.cli.reused.releases.Version.Type.PRERELEASE;

import java.nio.file.Path;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.opendatakit.aggregate.cli.reused.fs.UncheckedFiles;

public class Version implements Comparable<Version> {
  private final String semver;
  private final int major;
  private final int minor;
  private final int patch;
  private final Type type;
  private final Optional<Integer> iteration;

  public Version(String semver, int major, int minor, int patch, Type type, Optional<Integer> iteration) {
    this.semver = semver;
    this.major = major;
    this.minor = minor;
    this.patch = patch;
    this.type = type;
    this.iteration = iteration;
  }

  public static Version from(String semver) {
    Pattern p = Pattern.compile("^v?(\\d+?)\\.(\\d+?)\\.(\\d+).*");
    Matcher matcher = p.matcher(semver);
    if (!matcher.matches())
      throw new IllegalArgumentException("Given semver string can't be parsed: " + semver);
    int major = Integer.parseInt(matcher.group(1));
    int minor = Integer.parseInt(matcher.group(2));
    int patch = Integer.parseInt(matcher.group(3));
    Type type = semver.contains("-")
        ? Type.parse(semver.substring(semver.indexOf("-") + 1, semver.lastIndexOf(".")))
        : NORMAL;
    Optional<Integer> iteration = semver.contains("-")
        ? Optional.of(Integer.parseInt(semver.substring(semver.lastIndexOf(".") + 1).split("-")[0]))
        : Optional.empty();
    return new Version(semver, major, minor, patch, type, iteration);
  }

  public static Version read(Path versionFile) {
    return Version.from(new String(UncheckedFiles.readAllBytes(versionFile), UTF_8).trim());
  }

  public int getMajor() {
    return major;
  }

  public int getMinor() {
    return minor;
  }

  public int getPatch() {
    return patch;
  }

  public boolean isPreRelease() {
    return type == PRERELEASE;
  }

  public int getIteration() {
    return iteration.orElseThrow(RuntimeException::new);
  }

  @Override
  public String toString() {
    return semver;
  }

  @Override
  public int compareTo(Version other) {
    // Legacy versions are always less than any other version type
    if (type == LEGACY)
      return -1;
    if (other.type == LEGACY)
      return 1;

    // Next case: semver comparison
    if (major > other.major)
      return 1;
    if (major == other.major && minor > other.minor)
      return 1;
    if (major == other.major && minor == other.minor && patch > other.patch)
      return 1;

    // Next case: Same semver, but this one is a beta
    // Beta is less than no beta within the same semver
    if (major == other.major && minor == other.minor && patch == other.patch && isPreRelease() && !other.isPreRelease())
      return -1;

    // Next case: both have the same semver and both are beta versions.
    // We need to compare the beta iteration
    if (major == other.major && minor == other.minor && patch == other.patch && isPreRelease() && other.isPreRelease())
      return Integer.compare(getIteration(), other.getIteration());

    // Next case: both have the same semver
    if (major == other.major && minor == other.minor && patch == other.patch)
      return 0;

    // Default case: this one is less than the other
    return -1;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Version version = (Version) o;
    return major == version.major &&
        minor == version.minor &&
        patch == version.patch &&
        type == version.type &&
        Objects.equals(iteration, version.iteration);
  }

  @Override
  public int hashCode() {
    return Objects.hash(major, minor, patch, type, iteration);
  }

  public boolean isLegacy() {
    return type == LEGACY;
  }

  public String buildGitHubDownloadUrl() {
    return String.format(
        "https://github.com/opendatakit/aggregate/releases/download/%s/ODK-Aggregate-%s.war",
        semver,
        semver
    );
  }

  public static class NullVersion extends Version {
    public NullVersion(String semver) {
      super(semver, 0, 0, 0, Type.LEGACY, Optional.empty());
    }
  }

  enum Type {
    NORMAL, PRERELEASE, LEGACY;

    public static Type parse(String type) {
      if (type.equalsIgnoreCase("beta"))
        return PRERELEASE;
      throw new IllegalArgumentException("Version type " + type + " not supported");
    }

  }
}
