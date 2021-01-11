package com.ethanshea.launchchord;

import java.util.ArrayList;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

public class Launchpad implements Receiver {
    private ArrayList<LaunchEventListener> listeners;
    private MidiDevice midiIn;
    private MidiDevice midiOut;

    private boolean[][] buttons;// Array containing the pushed state of the launchpad buttons

    public boolean isButtonPressed(int x, int y) {
	return buttons[x][y + 1];
    }

    public void changeColor(LEDColor color, int x, int y) {
	ShortMessage msg = new ShortMessage();
	try {
	    if (y >= 0) {
		msg.setMessage(0x90, x + y * 16, color.getBitfeild());
	    } else { // For the automap buttons
		msg.setMessage(0xB0, 0x68 + x, color.getBitfeild());
	    }
	    midiOut.getReceiver().send(msg, -1);
	} catch (InvalidMidiDataException e) {
	    e.printStackTrace();
	    System.err.println("Invalid MIDI Data");
	} catch (MidiUnavailableException e) {
	    e.printStackTrace();
	    System.err.println("Unable to send a color update");
	}
    }

    public Launchpad() {
	buttons = new boolean[9][9];
	listeners = new ArrayList<LaunchEventListener>();
    }

    public void setMidiInDevice(MidiDevice d) {
	if ((midiIn != null) && (midiIn.isOpen())) {
	    midiIn.close();
	}
	midiIn = d;
	try {
	    midiIn.open();
	    midiIn.getTransmitter().setReceiver(this);
	    fireLaunchpadConnected();
	} catch (MidiUnavailableException e) {
	    e.printStackTrace();
	    System.err.println("Unable to set the MIDI reciever, or open the Midi Device.");
	}
    }

    public void setMidiOutDevice(MidiDevice d) {
	if ((midiOut != null) && (midiOut.isOpen())) {
	    midiOut.close();
	}
	midiOut = d;
	try {
	    midiOut.open();

	    // FLash and reset the launchpad
	    ShortMessage msg = new ShortMessage();
	    msg.setMessage(0xB0, 0, 127); // All LEDs on full
	    midiOut.getReceiver().send(msg, -1);
	    msg.setMessage(0xB0, 0, 0); // Reset
	    midiOut.getReceiver().send(msg, -1);
	    fireLaunchpadConnected();
	} catch (MidiUnavailableException e) {
	    e.printStackTrace();
	    System.err.println("Unable to set the MIDI reciever, or open the Midi Device.");
	} catch (InvalidMidiDataException e) {
	    e.printStackTrace();
	}
    }

    public void fireEvent(ShortMessage msg) {
	int x = 0, y = 0;
	if (msg.getCommand() == 0x90) {
	    x = msg.getData1() % 16;
	    y = msg.getData1() / 16;
	} else if (msg.getCommand() == 0xB0) {
	    x = msg.getData1() - 0x68;
	    y = -1;
	}
	buttons[x][y + 1] = (msg.getData2() == 127);
	for (LaunchEventListener l : listeners) {
	    if (msg.getData2() == 127) {
		l.buttonPressed(this, x, y);
	    } else {
		l.buttonReleased(this, x, y);
	    }
	}
    }

    public void fireLaunchpadConnected() {
	if ((midiIn != null) && (midiOut != null)) {
	    for (LaunchEventListener l : listeners) {
		l.launchpadConnected(this);
	    }
	}
    }

    public void fireLaunchpadDisconnected() {
	for (LaunchEventListener l : listeners) {
	    l.launchpadDisonnected(this);
	}
    }

    public void addEventListener(LaunchEventListener listener) {
	listeners.add(listener);
    }

    public void removeEventListener(LaunchEventListener listener) {
	listeners.remove(listener);
    }

    public void send(MidiMessage message, long timeStamp) {
	if (message instanceof ShortMessage) {
	    fireEvent((ShortMessage) message);
	} else {
	    System.out.println(message);
	}
    }

    public void close() {
	fireLaunchpadDisconnected();
	if (midiIn != null)
	    midiIn.close();
	if (midiOut != null)
	    midiOut.close();
    }

    /**
     * Send a reset message to the launchpad. Does not reset the object, but the phyiscal device.
     */
    public void reset() {
	if (midiOut == null)
	    return;
	ShortMessage msg = new ShortMessage();
	try {
	    msg.setMessage(0xB0, 0, 0); // Reset
	    midiOut.getReceiver().send(msg, -1);
	} catch (InvalidMidiDataException e) {
	    e.printStackTrace();
	} catch (MidiUnavailableException e) {
	    e.printStackTrace();
	}
    }

    public boolean ready() {
	return midiIn.isOpen() && midiOut.isOpen();
    }
}
