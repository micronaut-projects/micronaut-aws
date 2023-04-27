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
public class SingleTableRowWithTwoGlobalSecondaryIndex extends SingleTableRowWithOneGlobalSecondaryIndex {
    private final String gsi2Pk;

    private final String gsi2Sk;

    /**
     *
     * @param pk Partition Key
     * @param sk Sort Key
     * @param className Class Name
     * @param gsi1Pk Global Secondary Index 1 Partition Key
     * @param gsi1Sk Global Secondary Index 1 Sort Key
     * @param gsi2Pk Global Secondary Index 2 Partition Key
     * @param gsi2Sk Global Secondary Index 2 Sort Key
     */
    public SingleTableRowWithTwoGlobalSecondaryIndex(String pk,
                                                     String sk,
                                                     String className,
                                                     String gsi1Pk,
                                                     String gsi1Sk,
                                                     String gsi2Pk,
                                                     String gsi2Sk) {
        super(pk, sk, className, gsi1Pk, gsi1Sk);
        this.gsi2Pk = gsi2Pk;
        this.gsi2Sk = gsi2Sk;
    }

    /**
     *
     * @return Global Secondary Index 2 Partition Key
     */
    @Nullable
    public String getGsi2Pk() {
        return gsi2Pk;
    }

    /**
     *
     * @return Global Secondary Index 2 Sort Key
     */
    @Nullable
    public String getGsi2Sk() {
        return gsi2Sk;
    }
}
