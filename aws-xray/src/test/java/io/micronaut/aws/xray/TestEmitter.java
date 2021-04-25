package io.micronaut.aws.xray;

import com.amazonaws.xray.emitters.Emitter;
import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.Subsegment;

import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * Test emitter to test emitted segments.
 */
@Singleton
public class TestEmitter extends Emitter {

    private List<Segment> segments = new ArrayList<>();
    private List<Subsegment> subsegments = new ArrayList<>();

    @Override
    public boolean sendSegment(Segment segment) {
        segments.add(segment);
        return true;
    }

    @Override
    public boolean sendSubsegment(Subsegment subsegment) {
        subsegments.add(subsegment);
        return true;
    }

    public List<Segment> getSegments() {
        return segments;
    }

    public List<Subsegment> getSubsegments() {
        return subsegments;
    }

    public void reset() {
        segments.clear();
        subsegments.clear();
    }
}
