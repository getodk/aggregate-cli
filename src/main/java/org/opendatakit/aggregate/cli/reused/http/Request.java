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

package org.opendatakit.aggregate.cli.reused.http;

import static java.util.Collections.emptyList;
import static org.opendatakit.aggregate.cli.reused.http.HttpHelpers.url;
import static org.opendatakit.aggregate.cli.reused.http.Request.Method.GET;

import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.opendatakit.aggregate.cli.reused.Json;
import org.opendatakit.aggregate.cli.reused.Pair;

/**
 * This Value Object class represents an HTTP request to some {@link URL}, maybe using
 * some {@link Credentials} for authentication.
 * <p>
 * It also gives type hints about the result calling sites would be able to expect
 * when executed.
 */
public class Request<T> {
  private final Method method;
  private final URL url;
  private final Optional<Credentials> credentials;
  private final Function<InputStream, T> contentMapper;
  final List<Pair<String, String>> headers;

  private Request(Method method, URL url, Optional<Credentials> credentials, Function<InputStream, T> contentMapper, List<Pair<String, String>> headers) {
    this.method = method;
    this.url = url;
    this.credentials = credentials;
    this.contentMapper = contentMapper;
    this.headers = headers;
  }

  public static Request<List<Map<String, Object>>> getJsonList(URL url) {
    return new Request<>(GET, url, Optional.empty(), Json::parseList, emptyList());
  }

  public Request<T> resolve(String path) {
    // Normalize slashes to ensure that the resulting url
    // has exactly one slash before the input path
    String newUrl = url.toString()
        + (!url.toString().endsWith("/") ? "/" : "")
        + (path.startsWith("/") ? path.substring(1) : path);
    return new Request<>(method, url(newUrl), credentials, contentMapper, headers);
  }

  void ifCredentials(BiConsumer<URL, Credentials> consumer) {
    credentials.ifPresent(c -> consumer.accept(url, c));
  }

  public URL getUrl() {
    return url;
  }

  public Method getMethod() {
    return method;
  }

  public T map(InputStream contents) {
    return contentMapper.apply(contents);
  }

  public <U> Request<U> withMapper(Function<T, U> newBodyMapper) {
    return new Request<>(method, url, credentials, contentMapper.andThen(newBodyMapper), headers);
  }

  public Request<T> header(String key, String value) {
    List<Pair<String, String>> newHeaders = new ArrayList<>();
    newHeaders.addAll(headers);
    newHeaders.add(Pair.of(key, value));
    return new Request<>(method, url, credentials, contentMapper, newHeaders);
  }

  enum Method {
    GET, HEAD
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;
    Request<?> request = (Request<?>) o;
    return Objects.equals(url, request.url) &&
        Objects.equals(credentials, request.credentials);
  }

  @Override
  public int hashCode() {
    return Objects.hash(url, credentials);
  }

  @Override
  public String toString() {
    return method + " " + url + " " + credentials.map(Credentials::toString).orElse("(no credentials)");
  }

}
