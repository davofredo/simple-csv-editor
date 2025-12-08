package com.davofredo.event;

import java.util.List;
import java.util.Set;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collections;

public class EventGatewayImpl implements EventGateway {
    private final Map<Class<?>, Set<Object>> eventListeners;

    public EventGatewayImpl() {
        eventListeners = new HashMap<>();
    }

    private void addEventListener(Class<?> cls, Object listener) {
        if (listener == null) return;
        eventListeners.computeIfAbsent(cls, k -> new HashSet<>()).add(listener);
    }

    @Override
    public void addEventListener(Object eventListener, Class<?>... classes) {
        if (eventListener == null || classes == null) return;
        for (Class<?> cls : classes) {
            addEventListener(cls, eventListener);
        }
    }

    private void removeEventListener(Class<?> cls, Object listener) {
        if (listener == null) return;
        var currentList = eventListeners.get(cls);
        if (currentList != null) {
            currentList.remove(listener);
        }
    }

    @Override
    public void removeEventListener(Object eventListener, Class<?>... classes) {
        if (eventListener == null || classes == null) return;
        for (Class<?> cls : classes) {
            removeEventListener(cls, eventListener);
        }
    }

    @Override
    public <T> List<T> getEventListeners(Class<T> cls) {
        if (cls == null) return Collections.emptyList();
        var listenerList = eventListeners.get(cls);
        if (listenerList == null || listenerList.isEmpty()) return Collections.emptyList();
        return new ArrayList<>(listenerList.stream().map(cls::cast).toList());
    }
}
