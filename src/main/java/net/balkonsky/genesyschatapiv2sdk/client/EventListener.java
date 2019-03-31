package net.balkonsky.genesyschatapiv2sdk.client;

import net.balkonsky.genesyschatapiv2sdk.model.ChatState;
import net.balkonsky.genesyschatapiv2sdk.model.chatevents.ChatEvent;

public interface EventListener {
    void onEvent(ChatEvent chatEvent);
    void onState(ChatState chatState);
}
