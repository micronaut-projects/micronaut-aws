package io.micronaut.aws.xray.recorder;

import com.amazonaws.xray.contexts.ThreadLocalSegmentContext;
import com.amazonaws.xray.entities.Entity;
import io.micronaut.core.annotation.Nullable;
import reactor.util.context.Context;

import java.util.Objects;

public class ReactorSegmentContext extends ThreadLocalSegmentContext {

    private static final String KEY = "X-RAY-SEGMENT";

    private final Context context;
    public ReactorSegmentContext(Context context) {
        this.context = context;
    }

    @Nullable
    @Override
    public Entity getTraceEntity() {
        return context.get(KEY);
    }

    @Override
    public void setTraceEntity(@Nullable Entity entity) {
        if (entity != null && entity.getCreator() != null) {
            entity.getCreator().getSegmentListeners().stream().filter(Objects::nonNull).forEach(l -> {
                l.onSetEntity(context.get(KEY), entity);
            });
        }
        context.put(KEY, entity);
    }

    @Override
    public void clearTraceEntity() {
        Entity oldEntity = context.get(KEY);
        if (oldEntity != null && oldEntity.getCreator() != null) {
            oldEntity.getCreator().getSegmentListeners().stream().filter(Objects::nonNull).forEach(l -> {
                l.onClearEntity(oldEntity);
            });
        }
        context.delete(KEY);
    }
}
