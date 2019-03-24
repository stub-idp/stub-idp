package stubidp.utils.rest.eventsink;

import stubidp.eventemitter.EventDetailsKey;

public class EventDetails {

    private final EventDetailsKey key;
    private final String value;

    public EventDetails(EventDetailsKey key, String value) {
        this.key = key;
        this.value = value;
    }

    public EventDetailsKey getKey() {
        return key;
    }

    public String getValue() {
        return value;
    }
}
