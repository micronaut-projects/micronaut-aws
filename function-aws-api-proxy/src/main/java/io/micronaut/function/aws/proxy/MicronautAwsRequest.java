/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy;

import java.net.URI;
import java.util.Optional;

import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.convert.value.MutableConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpParameters;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.cookie.Cookies;

public class MicronautAwsRequest<T> implements HttpRequest<T> {
  private MutableConvertibleValues<Object> attributes;
  private Cookies cookies;
  private HttpHeaders headers;
  private HttpParameters parameters;
  private HttpMethod method;
  private URI uri;
  private MicronautAwsRequestBodySupplier<T> bodySupplier;

  MicronautAwsRequest(MutableConvertibleValues<Object> attributes,
                      Cookies cookies,
                      HttpHeaders headers,
                      HttpParameters parameters,
                      HttpMethod method,
                      URI uri,
                      MicronautAwsRequestBodySupplier bodySupplier) {
    this.attributes = attributes;
    this.cookies = cookies;
    this.headers = headers;
    this.parameters = parameters;
    this.method = method;
    this.uri = uri;
    this.bodySupplier = bodySupplier;
  }
  private static MutableConvertibleValues<Object> $default$attributes() {
    return new MutableConvertibleValuesMap<>();
  }

  public static <T> MicronautAwsRequestBuilder<T> builder() {
    return new MicronautAwsRequestBuilder<T>();
  }

  @Override
  public Cookies getCookies() {
    return cookies;
  }

  @Override
  public HttpParameters getParameters() {
    return parameters;
  }

  @Override
  public HttpMethod getMethod() {
    return method;
  }

  @Override
  public URI getUri() {
    return uri;
  }

  @Override
  public HttpHeaders getHeaders() {
    return headers;
  }

  @Override
  public MutableConvertibleValues<Object> getAttributes() {
    return attributes;
  }

  @Override
  public Optional<T> getBody() {
    return bodySupplier.getBody();
  }

  @Override
  public <T1> Optional<T1> getBody(Argument<T1> argument) {
    return bodySupplier.getBody(argument);
  }

  public static class MicronautAwsRequestBuilder<T> {
    private MutableConvertibleValues<Object> attributes$value;
    private boolean attributes$set;
    private Cookies cookies;
    private HttpHeaders headers;
    private HttpParameters parameters;
    private HttpMethod method;
    private URI uri;
    private MicronautAwsRequestBodySupplier bodySupplier;

    MicronautAwsRequestBuilder() {
    }

    public MicronautAwsRequestBuilder<T> attributes(MutableConvertibleValues<Object> attributes) {
      this.attributes$value = attributes;
      this.attributes$set = true;
      return this;
    }

    public MicronautAwsRequestBuilder<T> cookies(Cookies cookies) {
      this.cookies = cookies;
      return this;
    }

    public MicronautAwsRequestBuilder<T> headers(HttpHeaders headers) {
      this.headers = headers;
      return this;
    }

    public MicronautAwsRequestBuilder<T> parameters(HttpParameters parameters) {
      this.parameters = parameters;
      return this;
    }

    public MicronautAwsRequestBuilder<T> method(HttpMethod method) {
      this.method = method;
      return this;
    }

    public MicronautAwsRequestBuilder<T> uri(URI uri) {
      this.uri = uri;
      return this;
    }

    public MicronautAwsRequestBuilder<T> bodySupplier(MicronautAwsRequestBodySupplier bodySupplier) {
      this.bodySupplier = bodySupplier;
      return this;
    }

    public MicronautAwsRequest<T> build() {
      MutableConvertibleValues<Object> attributes$value = this.attributes$value;
      if (!this.attributes$set) {
        attributes$value = MicronautAwsRequest.$default$attributes();
      }
      return new MicronautAwsRequest<T>(attributes$value, cookies, headers, parameters, method, uri, bodySupplier);
    }

    public String toString() {
      return "MicronautAwsRequest.MicronautAwsRequestBuilder(attributes$value=" + this.attributes$value + ", cookies=" + this.cookies + ", headers=" + this.headers + ", parameters=" + this.parameters + ", method=" + this.method + ", uri=" + this.uri + ", bodySupplier=" + this.bodySupplier + ")";
    }
  }
}
