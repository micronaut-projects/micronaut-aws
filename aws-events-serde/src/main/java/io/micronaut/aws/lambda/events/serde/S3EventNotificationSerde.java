/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.aws.lambda.events.serde;

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification;
import io.micronaut.serde.annotation.SerdeImport;

/**
 * {@link SerdeImport} for {@link S3EventNotification}.
 * @author Sergio del Amo
 * @since 3.3.0
 */
@SerdeImport(value = S3EventNotification.class)
@SerdeImport(value = S3EventNotification.RequestParametersEntity.class)
@SerdeImport(value = S3EventNotification.ResponseElementsEntity.class)
@SerdeImport(value = S3EventNotification.S3ObjectEntity.class)
@SerdeImport(value = S3EventNotification.S3BucketEntity.class)
@SerdeImport(value = S3EventNotification.UserIdentityEntity.class)
@SerdeImport(value = S3EventNotification.S3EventNotificationRecord.class)
public class S3EventNotificationSerde {
}
