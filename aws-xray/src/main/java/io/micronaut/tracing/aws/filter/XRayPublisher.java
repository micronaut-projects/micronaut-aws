/*
 * Copyright 2021 original authors
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
package io.micronaut.tracing.aws.filter;

import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.javax.servlet.AWSXRayServletFilter;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

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
 * @author Pavol Gressa
 * @since 2.5
 */
@SuppressWarnings("PublisherImplementation")
public class XRayPublisher<T> implements Publishers.MicronautPublisher<T> {

    private final Publisher<T> publisher;
    private final HttpRequest<?> request;
    private final AWSXRayServletFilter delegate;

    public XRayPublisher(Publisher<T> publisher, HttpRequest<?> request, AWSXRayServletFilter awsxRayServletFilter) {
        this.publisher = publisher;
        this.delegate = awsxRayServletFilter;
        this.request = request;
    }

    private Optional<TraceHeader> getTraceHeader(HttpRequest<?> request) {
        String traceHeaderString = request.getHeaders().get(TraceHeader.HEADER_KEY);
        if (null != traceHeaderString) {
            return Optional.of(TraceHeader.fromString(traceHeaderString));
        }
        return Optional.empty();
    }

    @Override
    public void subscribe(Subscriber<? super T> actual) {
        //noinspection SubscriberImplementation
        publisher.subscribe(new Subscriber<T>() {

            Segment segment;

            @Override
            public void onSubscribe(Subscription s) {
                segment = delegate.preFilter(new HttpRequestAdapter(request), null);
                actual.onSubscribe(s);
            }

            @Override
            public void onNext(T object) {
                if (object instanceof MutableHttpResponse) {
                    MutableHttpResponse<?> mutableHttpResponse = (MutableHttpResponse<?>) object;

                    Optional<TraceHeader> incomingHeader = getTraceHeader(request);
                    final TraceHeader responseHeader;
                    if (incomingHeader.isPresent()) {
                        // create a new header, and use the incoming header so we know what to do in regards to sending back the sampling
                        // decision.
                        responseHeader = new TraceHeader(segment.getTraceId());
                        if (TraceHeader.SampleDecision.REQUESTED == incomingHeader.get().getSampled()) {
                            responseHeader.setSampled(segment.isSampled() ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED);
                        }
                    } else {
                        // Create a new header, we're the tracing root. We wont return the sampling decision.
                        responseHeader = new TraceHeader(segment.getTraceId());
                    }
                    mutableHttpResponse.header(TraceHeader.HEADER_KEY, responseHeader.toString());

                    delegate.postFilter(new HttpRequestAdapter(request), new HttpResponseAdapter(mutableHttpResponse));
                }
                actual.onNext(object);
            }

            @Override
            public void onError(Throwable t) {
                if (segment != null) {
                    segment.addException(t);
                }
                actual.onError(t);
            }

            @Override
            public void onComplete() {
                actual.onComplete();
            }
        });
    }

    static class HttpRequestAdapter implements HttpServletRequest
    {
        private final HttpRequest<?> request;

        public HttpRequestAdapter(HttpRequest<?> request) {
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
            if(request.getContentType().isPresent()){
                return request.getContentType().get().getName();
            }else {
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
            throw new UnsupportedOperationException("Not implemented.");
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
            throw new UnsupportedOperationException("Not implemented.");
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
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public StringBuffer getRequestURL() {
            throw new UnsupportedOperationException("Not implemented.");
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

    static class HttpResponseAdapter implements HttpServletResponse{
        private final MutableHttpResponse<?> response;

        public HttpResponseAdapter(MutableHttpResponse<?> response) {
            this.response = response;
        }

        @Override
        public String getCharacterEncoding() {
            throw new UnsupportedOperationException("Not implemented.");
        }

        @Override
        public String getContentType() {
            if(response.getHeaders().getContentType().isPresent()){
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
            if(response != null){
                response.getHeaders().add(name, value);
            }else{

            }
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
    }
}