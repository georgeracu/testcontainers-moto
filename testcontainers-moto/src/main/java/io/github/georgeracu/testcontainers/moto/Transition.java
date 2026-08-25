package io.github.georgeracu.testcontainers.moto;

import java.time.Duration;

/**
 * Represents a state progression configuration for Moto's state-manager API.
 */
public final class Transition {

    private final String json;

    private Transition(String json) {
        this.json = json;
    }

    /**
     * Progresses the state immediately.
     */
    public static Transition immediate() {
        return new Transition("{\"progression\":\"immediate\"}");
    }

    /**
     * Progresses the state after a specified time.
     *
     * @param duration the time to wait before the state progresses
     */
    public static Transition time(Duration duration) {
        return new Transition("{\"progression\":\"time\",\"seconds\":" + duration.getSeconds() + "}");
    }

    /**
     * Progresses the state after a specified number of describe calls.
     *
     * @param times the number of describe calls before the state progresses
     */
    public static Transition manual(int times) {
        return new Transition("{\"progression\":\"manual\",\"times\":" + times + "}");
    }

    String toJson() {
        return json;
    }
}
