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
package io.micronaut.aws.dynamodb;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

/**
 * Base class to extend from in a DynamoDB Single table design with a Global Secondary Index.
 */
@Introspected
public class SingleTableRowWithOneGlobalSecondaryIndex extends SingleTableRow {
    private final String gsi1Pk;

    private final String gsi1Sk;

    /**
     *
     * @param pk Partition Key
     * @param sk Sort Key
     * @param className Class Name
     * @param gsi1Pk Global Secondary Index 1 Partition Key
     * @param gsi1Sk Global Secondary Index 1 Sort Key
     */
    public SingleTableRowWithOneGlobalSecondaryIndex(String pk,
                                                     String sk,
                                                     String className,
                                                     String gsi1Pk,
                                                     String gsi1Sk) {
        super(pk, sk, className);
        this.gsi1Pk = gsi1Pk;
        this.gsi1Sk = gsi1Sk;
    }

    /**
     *
     * @return Global Secondary Index 1 Partition Key
     */
    @Nullable
    public String getGsi1Pk() {
        return gsi1Pk;
    }

    /**
     *
     * @return Global Secondary Index 1 Sort Key
     */
    @Nullable
    public String getGsi1Sk() {
        return gsi1Sk;
    }
}
