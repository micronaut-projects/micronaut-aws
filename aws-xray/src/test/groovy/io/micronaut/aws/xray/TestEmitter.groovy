package io.micronaut.aws.xray

import com.amazonaws.xray.config.DaemonConfiguration
import com.amazonaws.xray.emitters.Emitter
import com.amazonaws.xray.entities.Segment
import com.amazonaws.xray.entities.Subsegment
import groovy.transform.CompileStatic

@CompileStatic
class TestEmitter extends Emitter {

    List<Segment> segments = []
    List<Subsegment> subsegments = []

    @Override
    boolean sendSegment(Segment segment) {
        synchronized (this) {
            segments.add(segment)
        }
        true
    }

    @Override
    boolean sendSubsegment(Subsegment subsegment) {
        synchronized (this) {
            subsegments.add(subsegment)
        }
        true
    }

    void reset() {
        synchronized (this) {
            segments.clear()
            subsegments.clear()
        }
    }
}
