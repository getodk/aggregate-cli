package org.opendatakit.aggregate.cli.operations;

import static org.opendatakit.aggregate.cli.reused.Json.parseNode;
import static org.opendatakit.aggregate.cli.reused.Json.toMap;

import com.fasterxml.jackson.databind.JsonNode;
import com.mitchellbosecke.pebble.PebbleEngine;
import com.mitchellbosecke.pebble.loader.ClasspathLoader;
import com.mitchellbosecke.pebble.template.PebbleTemplate;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.opendatakit.aggregate.cli.operations.Exceptions.ConfigurationException;

class EnvironmentConfiguration {

  private final Path home;
  private final JdbcConfiguration jdbc;
  private final SecurityConfiguration security;
  final TomcatConfiguration tomcat;
  private static final PebbleEngine PEEBLE = new PebbleEngine.Builder()
      .newLineTrimming(false)
      .loader(new ClasspathLoader())
      .build();
  private static final PebbleTemplate CONF_TPL = PEEBLE.getTemplate("configuration.tpl.json");
  private static final PebbleTemplate JDBC_TPL = PEEBLE.getTemplate("jdbc.tpl.properties");
  private static final PebbleTemplate SECURITY_TPL = PEEBLE.getTemplate("security.tpl.properties");
  public static final DateTimeFormatter BACKUP_DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
      .appendValue(ChronoField.YEAR, 4)
      .appendValue(ChronoField.MONTH_OF_YEAR, 2)
      .appendValue(ChronoField.DAY_OF_MONTH, 2)
      .appendLiteral("-")
      .appendValue(ChronoField.HOUR_OF_DAY, 2)
      .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
      .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
      .toFormatter();

  private EnvironmentConfiguration(Path home, JdbcConfiguration jdbc, SecurityConfiguration security, TomcatConfiguration tomcat) {
    this.home = home;
    this.jdbc = jdbc;
    this.security = security;
    this.tomcat = tomcat;
  }


  static EnvironmentConfiguration from(String configurationFilePath) {
    try {
      JsonNode root = parseNode(Paths.get(configurationFilePath));
      return new EnvironmentConfiguration(
          Paths.get(root.get("home").asText()),
          JdbcConfiguration.from(toMap(root.get("jdbc"))),
          SecurityConfiguration.from(toMap(root.get("security"))),
          TomcatConfiguration.from(toMap(root.get("tomcat")))
      );
    } catch (NullPointerException e) {
      throw new ConfigurationException("Missing values");
    } catch (UncheckedIOException e) {
      throw new ConfigurationException(e.getCause() instanceof NoSuchFileException
          ? "Provided configuration file doesn't exist"
          : "Can't parse the provided configuration file");
    }
  }

  Path getBackupPath() {
    return home.resolve("backup");
  }

  Path buildBackupFilePath(LocalDateTime date) {
    return getBackupPath().resolve("aggregate-" + date.format(BACKUP_DATE_TIME_FORMATTER) + ".zip");
  }

  Path getVersionFilePath() {
    return home.resolve("aggregate.version");
  }

  Path getRootWebappPath() {
    return tomcat.webappsLocation.resolve("ROOT");
  }

  Path getJdbcConfigurationPath() {
    return getRootWebappPath().resolve("WEB-INF/classes/jdbc.properties");
  }

  Path getSecurityConfigurationPath() {
    return getRootWebappPath().resolve("WEB-INF/classes/security.properties");
  }

  static String renderConfigurationTemplate() {
    return renderTpl(CONF_TPL, Collections.emptyMap());
  }

  String renderJdbcConfiguration() {
    return renderTpl(JDBC_TPL, jdbc.asMap());
  }

  String renderSecurityConfiguration() {
    return renderTpl(SECURITY_TPL, security.asMap());
  }

  private static String renderTpl(PebbleTemplate jdbcTpl, Map<String, Object> context) {
    Writer writer = new StringWriter();
    try {
      jdbcTpl.evaluate(writer, context);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
    return writer.toString();
  }


  static class JdbcConfiguration {
    final String host;
    final int port;
    final String db;
    final String schema;
    final String user;
    final String password;

    JdbcConfiguration(String host, int port, String db, String schema, String user, String password) {

      this.host = host;
      this.port = port;
      this.db = db;
      this.schema = schema;
      this.user = user;
      this.password = password;
    }

    static JdbcConfiguration from(Map<String, Object> data) {
      return new JdbcConfiguration(
          (String) Objects.requireNonNull(data.get("host")),
          (Integer) Objects.requireNonNull(data.get("port")),
          (String) Objects.requireNonNull(data.get("db")),
          (String) Objects.requireNonNull(data.get("schema")),
          (String) Objects.requireNonNull(data.get("user")),
          (String) Objects.requireNonNull(data.get("password"))
      );
    }

    Map<String, Object> asMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("host", host);
      map.put("port", port);
      map.put("db", db);
      map.put("schema", schema);
      map.put("user", user);
      map.put("password", password);
      return map;
    }
  }

  static class SecurityConfiguration {
    final boolean forceHttpsLinks;
    final int port;
    final int securePort;

    SecurityConfiguration(boolean forceHttpsLinks, int port, int securePort) {
      this.forceHttpsLinks = forceHttpsLinks;
      this.port = port;
      this.securePort = securePort;
    }

    static SecurityConfiguration from(Map<String, Object> data) {
      return new SecurityConfiguration(
          (Boolean) Objects.requireNonNull(data.get("forceHttpsLinks")),
          (Integer) Objects.requireNonNull(data.get("port")),
          (Integer) Objects.requireNonNull(data.get("securePort"))
      );
    }

    Map<String, Object> asMap() {
      Map<String, Object> map = new HashMap<>();
      map.put("forceHttpsLinks", forceHttpsLinks ? "true" : "false");
      map.put("port", port);
      map.put("securePort", securePort);
      return map;
    }
  }

  public static class TomcatConfiguration {
    final String uid;
    final String gid;
    final Path webappsLocation;

    TomcatConfiguration(String uid, String gid, Path webappsLocation) {
      this.uid = uid;
      this.gid = gid;
      this.webappsLocation = webappsLocation;
    }

    public static TomcatConfiguration from(Map<String, Object> tomcat) {
      return new TomcatConfiguration(
          (String) Objects.requireNonNull(tomcat.get("uid")),
          (String) Objects.requireNonNull(tomcat.get("gid")),
          Paths.get((String) Objects.requireNonNull(tomcat.get("webappsPath")))
      );
    }
  }
}
