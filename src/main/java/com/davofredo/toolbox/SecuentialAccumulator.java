package com.davofredo.toolbox;

public class SecuentialAccumulator {
    private long next;

    public SecuentialAccumulator() {
        this(1);
    }

    public SecuentialAccumulator(long start) {
        next = start;
    }

    public long getNext() {
        return next++;
    }
}
