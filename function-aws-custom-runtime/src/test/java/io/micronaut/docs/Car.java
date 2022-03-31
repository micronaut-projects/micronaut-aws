package io.micronaut.docs;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class Car {

    private int cylinders;

    public Car() {
    }

    public int getCylinders() {
        return cylinders;
    }

    public void setCylinders(int cylinders) {
        this.cylinders = cylinders;
    }
}
