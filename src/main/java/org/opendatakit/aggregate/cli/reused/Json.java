package org.opendatakit.aggregate.cli.reused;

import static org.opendatakit.aggregate.cli.reused.fs.UncheckedFiles.readAllBytes;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class Json {
  private static final TypeReference<Map<String, Object>> MAP = new TypeReference<Map<String, Object>>() {
  };
  public static final TypeReference<List<Map<String, Object>>> LIST = new TypeReference<List<Map<String, Object>>>() {
  };
  private static final ObjectMapper MAPPER = new ObjectMapper();

  public static Map<String, Object> toMap(JsonNode node) {
    return MAPPER.convertValue(node, MAP);
  }

  public static JsonNode parseNode(Path file) {
    try {
      return MAPPER.readTree(readAllBytes(file));
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  public static List<Map<String, Object>> parseList(InputStream is) {
    try {
      return MAPPER.readValue(is, LIST);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
