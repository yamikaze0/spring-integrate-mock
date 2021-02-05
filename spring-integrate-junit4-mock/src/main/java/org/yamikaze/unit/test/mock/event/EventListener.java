package org.yamikaze.unit.test.mock.event;

/**
 * @author qinluo
 * @version 1.0.0
 * @date 2020-04-22 19:00
 */
public interface EventListener {

    /**
     * do something on event
     * @param event event.
     */
    void onEvent(Event event);
}
