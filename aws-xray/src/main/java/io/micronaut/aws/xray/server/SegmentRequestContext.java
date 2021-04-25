/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.aws.xray.server;

import com.amazonaws.xray.entities.Segment;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpHeaders;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.simple.SimpleHttpHeaders;
import javax.servlet.AsyncContext;
import javax.servlet.DispatcherType;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletInputStream;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpUpgradeHandler;
import javax.servlet.http.Part;
import javax.validation.constraints.NotNull;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.security.Principal;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Vector;

/**
 * Segment request context holds references to created {@link Segment} and {@link HttpRequest} resp
 * {@link io.micronaut.http.HttpResponse} adapters to {@link HttpServletRequest} resp. {@link HttpServletResponse}
 * classes.
 *
 * @author Pavol Gressa
 * @since 2.7.0
 */
public class SegmentRequestContext {

    private final Segment segment;
    private final HttpRequestAdapter httpRequestAdapter;
    private final HttpResponseAdapter httpResponseAdapter;

    SegmentRequestContext(@NotNull Segment segment, @NotNull HttpRequestAdapter httpRequestAdapter, @NotNull HttpResponseAdapter httpResponseAdapter) {
        this.segment = segment;
        this.httpRequestAdapter = httpRequestAdapter;
        this.httpResponseAdapter = httpResponseAdapter;
    }

    /**
     * @return The http request
     */
    public HttpRequest<?> getHttpRequest() {
        return this.getHttpRequestAdapter().request;
    }

    /**
     * @return The http response
     */
    public MutableHttpResponse<?> getMutableHttpResponse() {
        return this.getHttpResponseAdapter().response;
    }

    /**
     * @return The segment
     */
    public Segment getSegment() {
        return segment;
    }

    /**
     * @return The adapted http request
     */
    public HttpRequestAdapter getHttpRequestAdapter() {
        return httpRequestAdapter;
    }

    /**
     * @return The adapted http response
     */
    public HttpResponseAdapter getHttpResponseAdapter() {
        return httpResponseAdapter;
    }

    /**
     * Naive adapter of {@link io.micronaut.http.HttpRequest} to {@link HttpServletRequest}
     * only for purpose of reusing existing functionality in {@link XRayHttpServerFilter}.
     */
    public static class HttpRequestAdapter implements HttpServletRequest {
        private final HttpRequest<?> request;

        public HttpRequestAdapter(@NotNull HttpRequest<?> request) {
            this.request = request;
        }

        @Override
        public Object getAttribute(String name) {
            return request.getAttribute(name);
        }

        @Override
        public Enumeration<String> getAttributeNames() {
            return new Vector<>(request.getAttributes().names()).elements();
        }

        @Override
        public String getCharacterEncoding() {
            return request.getCharacterEncoding().name();
        }

        @Override
        public void setCharacterEncoding(String env) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getContentLength() {
            return Long.valueOf(request.getContentLength()).intValue();
        }

        @Override
        public long getContentLengthLong() {
            return request.getContentLength();
        }

        @Override
        public String getContentType() {
            if (request.getContentType().isPresent()) {
                return request.getContentType().get().getName();
            } else {
                return null;
            }
        }

        @Override
        public ServletInputStream getInputStream() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getParameter(String name) {
            return request.getParameters().get(name);
        }

        @Override
        public Enumeration<String> getParameterNames() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String[] getParameterValues(String name) {
            return request.getParameters().getAll(name).toArray(new String[0]);
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getProtocol() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getScheme() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getServerName() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getServerPort() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public BufferedReader getReader() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRemoteAddr() {
            return request.getRemoteAddress().getHostString();
        }

        @Override
        public String getRemoteHost() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setAttribute(String name, Object o) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void removeAttribute(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Enumeration<Locale> getLocales() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isSecure() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public RequestDispatcher getRequestDispatcher(String path) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRealPath(String path) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getRemotePort() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getLocalName() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getLocalAddr() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getLocalPort() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public ServletContext getServletContext() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public AsyncContext startAsync() throws IllegalStateException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public AsyncContext startAsync(ServletRequest servletRequest, ServletResponse servletResponse) throws IllegalStateException {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isAsyncStarted() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isAsyncSupported() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public AsyncContext getAsyncContext() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public DispatcherType getDispatcherType() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getAuthType() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Cookie[] getCookies() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public long getDateHeader(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getHeader(String name) {
            return request.getHeaders().get(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getIntHeader(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getMethod() {
            return request.getMethodName();
        }

        @Override
        public String getPathInfo() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getPathTranslated() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getContextPath() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getQueryString() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRemoteUser() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isUserInRole(String role) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Principal getUserPrincipal() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRequestedSessionId() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getRequestURI() {
            return request.getUri().getPath();
        }

        @Override
        public StringBuffer getRequestURL() {
            return new StringBuffer(request.getUri().toString());
        }

        @Override
        public String getServletPath() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public HttpSession getSession(boolean create) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public HttpSession getSession() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String changeSessionId() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isRequestedSessionIdValid() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isRequestedSessionIdFromCookie() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isRequestedSessionIdFromURL() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isRequestedSessionIdFromUrl() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean authenticate(HttpServletResponse response) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void login(String username, String password) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void logout() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Collection<Part> getParts() {
            return null;
        }

        @Override
        public Part getPart(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public <T extends HttpUpgradeHandler> T upgrade(Class<T> handlerClass) {
            throw new UnsupportedOperationException("Not implemented.");
        }
    }

    /**
     * Naive adapter of {@link io.micronaut.http.MutableHttpResponse} to {@link HttpServletResponse}
     * only for purpose of reusing existing functionality in {@link XRayHttpServerFilter}.
     */
    public static class HttpResponseAdapter implements HttpServletResponse {

        private final MutableHttpResponse<?> response;

        public HttpResponseAdapter(@NotNull MutableHttpResponse<?> response) {
            this.response = response;
        }

        /**
         *
         * @return The HTTP Response
         */
        public MutableHttpResponse<?> getResponse() {
            return response;
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getContentType() {
            if (response.getHeaders().getContentType().isPresent()) {
                return response.getHeaders().getContentType().get();
            }
            return null;
        }

        @Override
        public ServletOutputStream getOutputStream() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public PrintWriter getWriter() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setCharacterEncoding(String charset) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setContentLength(int len) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setContentLengthLong(long len) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setContentType(String type) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setBufferSize(int size) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getBufferSize() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void flushBuffer() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void resetBuffer() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean isCommitted() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void reset() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setLocale(Locale loc) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Locale getLocale() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void addCookie(Cookie cookie) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public boolean containsHeader(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String encodeURL(String url) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String encodeRedirectURL(String url) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String encodeUrl(String url) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String encodeRedirectUrl(String url) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void sendError(int sc, String msg) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void sendError(int sc) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void sendRedirect(String location) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setDateHeader(String name, long date) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void addDateHeader(String name, long date) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setHeader(String name, String value) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void addHeader(String name, String value) {
            response.getHeaders().add(name, value);
        }

        @Override
        public void setIntHeader(String name, int value) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void addIntHeader(String name, int value) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setStatus(int sc) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public void setStatus(int sc, String sm) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public int getStatus() {
            return response.getStatus().getCode();
        }

        @Override
        public String getHeader(String name) {
            return response.getHeaders().get(name);
        }

        @Override
        public Collection<String> getHeaders(String name) {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public Collection<String> getHeaderNames() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        public static MutableHttpResponse<?> createEmpty(ConversionService<?> conversionService) {
            return new MutableHttpResponse<Object>() {

                private final SimpleHttpHeaders simpleHttpHeaders = new SimpleHttpHeaders(conversionService);

                @Override
                public MutableHttpResponse<Object> cookie(io.micronaut.http.cookie.Cookie cookie) {
                    throw new UnsupportedOperationException("Not implemented.");
                }

                @Override
                public MutableHttpResponse<Object> body(@Nullable Object body) {
                    throw new UnsupportedOperationException("Not implemented.");
                }

                @Override
                public MutableHttpResponse<Object> status(HttpStatus status, CharSequence message) {
                    throw new UnsupportedOperationException("Not implemented.");
                }

                @Override
                public HttpStatus getStatus() {
                    throw new UnsupportedOperationException("Not implemented.");
                }

                @Override
                public MutableHttpHeaders getHeaders() {
                    return simpleHttpHeaders;
                }

                @NonNull
                @Override
                public MutableConvertibleValues<Object> getAttributes() {
                    throw new UnsupportedOperationException("Not implemented.");
                }

                @NonNull
                @Override
                public Optional<Object> getBody() {
                    return Optional.empty();
                }
            };
        }
    }
}
