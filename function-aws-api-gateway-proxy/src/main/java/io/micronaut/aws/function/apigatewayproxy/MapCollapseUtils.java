package io.micronaut.aws.function.apigatewayproxy;

import io.micronaut.core.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapCollapseUtils {

    private MapCollapseUtils() {

    }

    public static Map<String, List<String>> collapse(Map<String, List<String>> multi, Map<String, String> single) {
        if (multi == null && single == null) {
            return Collections.emptyMap();
        } else {
            Map<String, List<String>> values = new HashMap<>();
            if (multi != null) {
                for (String name : multi.keySet()) {
                    values.computeIfAbsent(name, s -> new ArrayList<>());
                    values.get(name).addAll(multi.get(name));
                }
            }
            if (CollectionUtils.isNotEmpty(single)) {
                for (String name : single.keySet()) {
                    values.computeIfAbsent(name, s -> new ArrayList<>());
                    values.get(name).add(single.get(name));
                }
            }
            return values;
        }
    }
}
