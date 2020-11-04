/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.discovery.aws.parameterstore;

/**
 * An object encapsulating all necessary parameters to perform a request for
 * configuration values to the Parameter Store, as well as the name of the
 * resulting property source and associated priority.
 *
 * @author ttzn
 * @since 2.1.2
 */
public class ParameterQuery {
    private final String path;
    private final String propertySourceName;
    private final int priority;
    private final boolean name;

    public ParameterQuery(String path, String propertySourceName, int priority) {
        this(path, propertySourceName, priority, false);
    }

    public ParameterQuery(String path, String propertySourceName, int priority, boolean name) {
        this.path = path;
        this.propertySourceName = propertySourceName;
        this.name = name;
        this.priority = priority;
    }

    /**
     * @return the path to be used when querying the Parameter Store; if {@link #isName()} is true,
     * it will be used as <code>names</code> in a GetParameters request,
     * call, otherwise it is the <code>path</code> parameter of a GetParametersByPath.
     */
    public String getPath() {
        return path;
    }

    /**
     * @return the name of the property source that will hold the retrieved configuration values.
     */
    public String getPropertySourceName() {
        return propertySourceName;
    }

    /**
     * @return whether the current query should be performed using the GetParameters API instead of
     * GetParametersByPath. This is almost never not what you want.
     */
    public boolean isName() {
        return name;
    }

    /**
     * @return the priority of the property source that will hold the retrieved configuration values.
     */
    public int getPriority() {
        return priority;
    }
}
