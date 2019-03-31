package net.balkonsky.genesyschatapiv2sdk.client;

import net.balkonsky.genesyschatapiv2sdk.model.ChatState;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.ChatEvent;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

@Slf4j
public class EventManager {
    private List<EventListener> listeners = new ArrayList<>();

    public EventManager() {
    }

    public synchronized void subscribe(EventListener eventListener) {
        log.trace("add listener {}",eventListener);
        listeners.add(eventListener);
    }

    public synchronized void unsubscribe(EventListener eventListener) {
        log.trace("delete listener {}",eventListener);
        listeners.remove(eventListener);
    }

    public void notify(ChatEvent chatEvent) {
        log.debug("notify all listeners on chat event: {}",chatEvent);
        listeners.forEach(x -> x.onEvent(chatEvent));
    }

    public void notify(ChatState chatState) {
        log.debug("notify all listeners on chat state: {}",chatState);
        listeners.forEach(x -> x.onState(chatState));
    }

}
