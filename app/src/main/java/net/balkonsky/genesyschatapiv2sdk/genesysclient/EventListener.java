package net.balkonsky.genesyschatapiv2sdk.genesysclient;

import net.balkonsky.genesyschatapiv2sdk.model.ChatState;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.ChatEvent;

public interface EventListener {
    void onEvent(ChatEvent chatEvent);
    void onState(ChatState chatState);
}
