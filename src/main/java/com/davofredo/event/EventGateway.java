package com.davofredo.event;

import java.util.List;

public interface EventGateway {
    void addEventListener(Object eventListener, Class<?>... listenerTypes);
    void removeEventListener(Object eventListener, Class<?>... listenerTypes);
    <T> List<T> getEventListeners(Class<T> cls);
}
