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

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;

/**
 * Base class to extend from in a DynamoDB Single table design.
 */
@Introspected
public class BaseItem implements CompositeKey, GlobalSecondaryIndex1, GlobalSecondaryIndex2, GlobalSecondaryIndex3, GlobalSecondaryIndex4, GlobalSecondaryIndex5 {
    @NonNull
    @NotBlank
    private final String pk;

    @NonNull
    @NotBlank
    private final String sk;

    @Nullable
    private final String gsi1Pk;

    @Nullable
    private final String gsi1Sk;

    @Nullable
    private final String gsi2Pk;

    @Nullable
    private final String gsi2Sk;

    @Nullable
    private final String gsi3Pk;

    @Nullable
    private final String gsi3Sk;

    @Nullable
    private final String gsi4Pk;

    @Nullable
    private final String gsi4Sk;

    @Nullable
    private final String gsi5Pk;

    @Nullable
    private final String gsi5Sk;

    /**
     *
     * @param pk Primary Key
     * @param sk Sort Key
     * @param gsi1pk Global Secondary Index 1 Primary Key
     * @param gsi1sk Global Secondary Index 1 Sort Key
     * @param gsi2pk Global Secondary Index 2 Primary Key
     * @param gsi2sk Global Secondary Index 2 Sort Key
     * @param gsi3pk Global Secondary Index 3 Primary Key
     * @param gsi3sk Global Secondary Index 3 Sort Key
     * @param gsi4pk Global Secondary Index 4 Primary Key
     * @param gsi4sk Global Secondary Index 4 Sort Key
     * @param gsi5pk Global Secondary Index 4 Primary Key
     * @param gsi5sk Global Secondary Index 5 Sort Key
     */
    @Creator
    public BaseItem(@NonNull String pk,
                    @NonNull String sk,
                    @Nullable String gsi1pk,
                    @Nullable String gsi1sk,
                    @Nullable String gsi2pk,
                    @Nullable String gsi2sk,
                    @Nullable String gsi3pk,
                    @Nullable String gsi3sk,
                    @Nullable String gsi4pk,
                    @Nullable String gsi4sk,
                    @Nullable String gsi5pk,
                    @Nullable String gsi5sk) {
        this.pk = pk;
        this.sk = sk;
        this.gsi1Pk = gsi1pk;
        this.gsi1Sk = gsi1sk;
        this.gsi2Pk = gsi2pk;
        this.gsi2Sk = gsi2sk;
        this.gsi3Pk = gsi3pk;
        this.gsi3Sk = gsi3sk;
        this.gsi4Pk = gsi4pk;
        this.gsi4Sk = gsi4sk;
        this.gsi5Pk = gsi5pk;
        this.gsi5Sk = gsi5sk;
    }

    public BaseItem(@NonNull String pk,
                    @NonNull String sk,
                    @Nullable String gsi1pk,
                    @Nullable String gsi1sk,
                    @Nullable String gsi2pk,
                    @Nullable String gsi2sk,
                    @Nullable String gsi3pk,
                    @Nullable String gsi3sk,
                    @Nullable String gsi4pk,
                    @Nullable String gsi4sk) {
        this(pk, sk, gsi1pk, gsi1sk, gsi2pk, gsi2sk, gsi3pk, gsi3sk, gsi4pk, gsi4sk, null, null);
    }

    public BaseItem(@NonNull String pk,
                    @NonNull String sk,
                    @Nullable String gsi1pk,
                    @Nullable String gsi1sk,
                    @Nullable String gsi2pk,
                    @Nullable String gsi2sk,
                    @Nullable String gsi3pk,
                    @Nullable String gsi3sk) {
        this(pk, sk, gsi1pk, gsi1sk, gsi2pk, gsi2sk, gsi3pk, gsi3sk, null, null);
    }

    public BaseItem(@NonNull String pk,
                    @NonNull String sk,
                    @Nullable String gsi1pk,
                    @Nullable String gsi1sk,
                    @Nullable String gsi2pk,
                    @Nullable String gsi2sk) {
        this(pk, sk, gsi1pk, gsi1sk, gsi2pk, gsi2sk, null, null);
    }

    public BaseItem(@NonNull String pk,
                    @NonNull String sk,
                    @Nullable String gsi1pk,
                    @Nullable String gsi1sk) {
        this(pk, sk, gsi1pk, gsi1sk, null, null);
    }

    /**
     *
     * @param pk Primary Key
     * @param sk Sort Key
     */
    public BaseItem(String pk,
                    String sk) {
        this(pk, sk, null, null);
    }

    @Override
    @NonNull
    public String getPk() {
        return pk;
    }

    @Override
    @NonNull
    public String getSk() {
        return sk;
    }

    @Override
    @Nullable
    public String getGsi1Pk() {
        return gsi1Pk;
    }

    @Override
    @Nullable
    public String getGsi1Sk() {
        return gsi1Sk;
    }

    @Override
    @Nullable
    public String getGsi2Pk() {
        return gsi2Pk;
    }

    @Override
    @Nullable
    public String getGsi2Sk() {
        return gsi2Sk;
    }

    @Override
    @Nullable
    public String getGsi3Pk() {
        return gsi3Pk;
    }

    @Override
    @Nullable
    public String getGsi3Sk() {
        return gsi3Sk;
    }

    @Override
    @Nullable
    public String getGsi4Pk() {
        return gsi4Pk;
    }

    @Override
    @Nullable
    public String getGsi4Sk() {
        return gsi4Sk;
    }

    @Override
    @Nullable
    public String getGsi5Pk() {
        return gsi5Pk;
    }

    @Override
    @Nullable
    public String getGsi5Sk() {
        return gsi5Sk;
    }
}
