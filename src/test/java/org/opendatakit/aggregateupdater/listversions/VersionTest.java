package org.opendatakit.aggregateupdater.listversions;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.lessThan;
import static org.hamcrest.Matchers.lessThanOrEqualTo;

import org.junit.jupiter.api.Test;
import org.opendatakit.aggregateupdater.releases.Version;

class VersionTest {
  @Test
  void name() {
    Version v = Version.from("1.2.3");
    assertThat(v.getMajor(), is(1));
    assertThat(v.getMinor(), is(2));
    assertThat(v.getPatch(), is(3));
  }

  @Test
  void name0() {
    Version v = Version.from("v1.2.3");
    assertThat(v.getMajor(), is(1));
    assertThat(v.getMinor(), is(2));
    assertThat(v.getPatch(), is(3));
  }

  @Test
  void name1() {
    assertThat(Version.from("0.0.1"), is(lessThan(Version.from("0.0.2"))));
    assertThat(Version.from("0.0.1"), is(lessThan(Version.from("0.1.0"))));
    assertThat(Version.from("0.1.0"), is(lessThan(Version.from("0.1.1"))));
    assertThat(Version.from("0.1.0"), is(lessThan(Version.from("0.2.0"))));
    assertThat(Version.from("1.0.0"), is(lessThan(Version.from("1.0.1"))));
    assertThat(Version.from("1.0.0"), is(lessThan(Version.from("1.1.0"))));
    assertThat(Version.from("1.0.0"), is(lessThan(Version.from("2.0.0"))));
    assertThat(Version.from("2.2.2"), is(greaterThan(Version.from("2.2.1"))));
    assertThat(Version.from("2.2.2"), is(greaterThan(Version.from("2.1.2"))));
    assertThat(Version.from("2.2.2"), is(greaterThan(Version.from("1.2.2"))));
  }

  @Test
  void name2() {
    Version v = Version.from("v2.1.3-beta.4");
    assertThat(v.getMajor(), is(2));
    assertThat(v.getMinor(), is(1));
    assertThat(v.getPatch(), is(3));
    assertThat(v.isBeta(), is(true));
    assertThat(v.getIteration(), is(4));
  }

  @Test
  void name3() {
    assertThat(Version.from("0.0.1"), is(lessThan(Version.from("0.0.2"))));
    assertThat(Version.from("0.0.1"), is(lessThanOrEqualTo(Version.from("0.0.1"))));
    assertThat(Version.from("0.0.1"), is(equalTo(Version.from("0.0.1"))));
    assertThat(Version.from("0.0.2"), is(greaterThanOrEqualTo(Version.from("0.0.2"))));
    assertThat(Version.from("0.0.2"), is(greaterThan(Version.from("0.0.1"))));
  }

  @Test
  void name4() {
    assertThat(Version.from("1.0.0-beta.0"), is(lessThan(Version.from("1.0.0"))));
    assertThat(Version.from("1.0.0-beta.0"), is(lessThan(Version.from("1.0.0-beta.1"))));
    assertThat(Version.from("1.0.1-beta.0"), is(greaterThan(Version.from("1.0.0"))));
  }

  @Test
  void name5() {
    assertThat(Version.from("0.0.1-beta.0"), is(lessThan(Version.from("0.0.1-beta.1"))));
    assertThat(Version.from("0.0.1-beta.0"), is(lessThanOrEqualTo(Version.from("0.0.1-beta.0"))));
    assertThat(Version.from("0.0.1-beta.1"), is(equalTo(Version.from("0.0.1-beta.1"))));
    assertThat(Version.from("0.0.1-beta.1"), is(greaterThanOrEqualTo(Version.from("0.0.1-beta.1"))));
    assertThat(Version.from("0.0.1-beta.1"), is(greaterThan(Version.from("0.0.1-beta.0"))));
  }

  @Test
  void name6() {
    assertThat(Version.from("v2.0.0-beta.0-9-gf72dfeed-dirty"), is(Version.from("v2.0.0-beta.0")));
  }
}
