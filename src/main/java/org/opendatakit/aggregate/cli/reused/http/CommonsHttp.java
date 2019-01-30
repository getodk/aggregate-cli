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

import static org.apache.http.client.config.CookieSpecs.STANDARD;
import static org.apache.http.client.config.RequestConfig.custom;
import static org.opendatakit.aggregate.cli.reused.http.HttpHelpers.uri;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.fluent.Executor;
import org.apache.http.conn.ConnectTimeoutException;
import org.apache.http.conn.HttpHostConnectException;
import org.apache.http.impl.client.HttpClientBuilder;

public class CommonsHttp implements Http {
  @Override
  public <T> Response<T> execute(Request<T> request) {
    // Always instantiate a new Executor to avoid side-effects between executions
    Executor executor = Executor.newInstance(HttpClientBuilder
        .create()
        .setDefaultRequestConfig(custom().setCookieSpec(STANDARD).build())
        .build());
    // Apply auth settings if credentials are received
    request.ifCredentials((URL url, Credentials credentials) -> executor.auth(
        HttpHost.create(url.getHost()),
        credentials.getUsername(),
        credentials.getPassword()
    ));
    // get the response body and let the Request map it
    return uncheckedExecute(request, executor);
  }

  private <T> Response<T> uncheckedExecute(Request<T> request, Executor executor) {
    org.apache.http.client.fluent.Request commonsRequest = getCommonsRequest(request);
    commonsRequest.connectTimeout(10_000);
    commonsRequest.socketTimeout(10_000);
    commonsRequest.addHeader("X-OpenRosa-Version", "1.0");
    request.headers.forEach(pair -> commonsRequest.addHeader(pair.getLeft(), pair.getRight()));
    try {
      return executor
          .execute(commonsRequest)
          .handleResponse(res -> {
            int statusCode = res.getStatusLine().getStatusCode();
            String statusPhrase = res.getStatusLine().getReasonPhrase();
            if (statusCode >= 500)
              return new Response.ServerError<>(statusCode, statusPhrase);
            if (statusCode >= 400)
              return new Response.ClientError<>(statusCode, statusPhrase);
            if (statusCode >= 300)
              return new Response.Redirection<>(statusCode, statusPhrase);
            return Response.Success.of(request, res);
          });
    } catch (HttpHostConnectException e) {
      throw new HttpException("Connection refused");
    } catch (SocketTimeoutException | ConnectTimeoutException e) {
      throw new HttpException("The connection has timed out");
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private InputStream extractOutput(HttpResponse res) {
    return Optional.ofNullable(res.getEntity())
        .map(this::uncheckedGetContent)
        .orElse(new ByteArrayInputStream("".getBytes(StandardCharsets.UTF_8)));
  }

  private static org.apache.http.client.fluent.Request getCommonsRequest(Request<?> request) {
    switch (request.getMethod()) {
      case GET:
        return org.apache.http.client.fluent.Request.Get(uri(request.getUrl()));
      case HEAD:
        return org.apache.http.client.fluent.Request.Head(uri(request.getUrl()));
      default:
        throw new HttpException("Method " + request.getMethod() + " is not supported");
    }
  }

  private InputStream uncheckedGetContent(HttpEntity entity) {
    try {
      return entity.getContent();
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }
}
