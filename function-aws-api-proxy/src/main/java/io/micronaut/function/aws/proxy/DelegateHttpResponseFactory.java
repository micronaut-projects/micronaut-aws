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

import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.simple.SimpleHttpResponseFactory;
import jakarta.inject.Singleton;

@Singleton
@Primary
@Replaces(HttpResponseFactory.class)
public class DelegateHttpResponseFactory implements HttpResponseFactory {
  private final SimpleHttpResponseFactory delegate = new SimpleHttpResponseFactory();

  public DelegateHttpResponseFactory() {
  }

  @Override
  public <T> MutableHttpResponse<T> ok(T body) {
    return delegate.ok(body);
  }

  @Override
  public <T> MutableHttpResponse<T> status(HttpStatus status, String reason) {
    return delegate.status(status, reason);
  }

  @Override
  public <T> MutableHttpResponse<T> status(HttpStatus status, T body) {
    return delegate.status(status, body);
  }

}
