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
package io.micronaut.tracing.aws;


import io.micronaut.tracing.aws.annotation.AwsXraySegment;
import io.micronaut.tracing.aws.annotation.AwsXraySubsegment;

import javax.inject.Singleton;

@Singleton
public class SegmentBean {

    @AwsXraySegment
    public String methodName() {
        return "method name as segment name";
    }

    @AwsXraySubsegment
    public String subsegment() {
        return "method name as subsegment name";
    }

    @AwsXraySegment(name = "bar")
    public String customSegment() {
        return "method name as segment name";
    }

    @AwsXraySegment(name = "bar", namespace = "namespace")
    public String customSegmentWithNamespace() {
        return "method name as segment name";
    }

    @AwsXraySubsegment(name = "foo")
    public String customSubsegment() {
        return "method name as subsegment name";
    }
}
