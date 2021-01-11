package com.ethanshea.launchchord;

public interface LaunchEventListener {
    public void buttonPressed(Launchpad launchpad, int x, int y);
    public void buttonReleased(Launchpad launchpad, int x, int y);
    public void launchpadConnected(Launchpad launchpad);
    public void launchpadDisonnected(Launchpad launchpad);
}
