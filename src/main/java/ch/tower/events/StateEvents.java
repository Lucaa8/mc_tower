package ch.tower.events;

import org.bukkit.event.Listener;

public interface StateEvents extends Listener
{
    public void onStateBegin();
    public void onStateLeave();
}
