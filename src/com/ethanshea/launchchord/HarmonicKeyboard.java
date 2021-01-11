package com.ethanshea.launchchord;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.ShortMessage;

public class HarmonicKeyboard implements LaunchEventListener {
    private int[][] notes;
    private MidiDevice output;
    private Launchpad launchpad;
    private Scale scale;

    private int octaveTranspose = 0;
    private boolean chord;

    private Point played;
    private Thread player;

    public HarmonicKeyboard() {
	notes = new int[8][8];
	scale = new Scale();
    }

    public void buttonPressed(Launchpad launchpad, int x, int y) {
	if (y == -1) {
	    if (x == 0) {// Octave up
		octaveTranspose += 1;
		if (octaveTranspose > 3)
		    octaveTranspose = 3;
	    } else if (x == 1) {// Octave down
		octaveTranspose -= 1;
		if (octaveTranspose < -3)
		    octaveTranspose = -3;
	    } else if (x == 7) {// Chord mode toggle
		chord = !chord;
		if (chord) {
		    launchpad.changeColor(LEDColor.RED, 7, -1);
		} else {
		    launchpad.changeColor(LEDColor.BLANK, 7, -1);
		}
	    }

	    if (octaveTranspose > 0) {
		launchpad.changeColor(new LEDColor(0, octaveTranspose), 0, -1);
		launchpad.changeColor(LEDColor.BLANK, 1, -1);
	    } else if (octaveTranspose < 0) {
		launchpad.changeColor(LEDColor.BLANK, 0, -1);
		launchpad.changeColor(new LEDColor(0, -octaveTranspose), 1, -1);
	    } else {
		launchpad.changeColor(LEDColor.BLANK, 0, -1);
		launchpad.changeColor(LEDColor.BLANK, 1, -1);
	    }
	}
	if (output != null) {
	    if ((x < 8) && (y >= 0)) {
		if (chord) {
		    // if (played==null){
		    // //Nothing played yet, so wait a while, then play.
		    // played = new Point(x,y);
		    // player = new Thread(new Runnable(){
		    // public void run() {
		    // try {
		    // Thread.sleep(20);
		    // } catch (InterruptedException e) {
		    // //Another note was played
		    // return;
		    // }
		    // //Play the diatonic chord.
		    // int pitch = notes[played.x][played.y] + octaveTranspose * 12;
		    // sendNote(ShortMessage.NOTE_ON,pitch);
		    // sendNote(ShortMessage.NOTE_ON,pitch+4);
		    // sendNote(ShortMessage.NOTE_ON,pitch+7);
		    // played = null;
		    // }
		    // });
		    // player.start();
		    // }else{
		    // //Stop the old thread and play our notes.
		    // player.interrupt();
		    // int pitch = notes[played.x][played.y] + octaveTranspose * 12;
		    // sendNote(ShortMessage.NOTE_ON,pitch);
		    // sendNote(ShortMessage.NOTE_ON,pitch+4);
		    // sendNote(ShortMessage.NOTE_ON,pitch+7);
		    // sendNote(ShortMessage.NOTE_ON,pitch+11);
		    // }
		} else {
		    int pitch = notes[x][y] + octaveTranspose * 12;
		    sendNote(ShortMessage.NOTE_ON, pitch);
		    int degree = scale.getDegree(pitch);
		    if (degree == 0) {// Root
			launchpad.changeColor(new LEDColor(3, 0), x, y);
		    } else if (degree == 4) {// Fith
			launchpad.changeColor(new LEDColor(0, 3), x, y);
		    } else {
			launchpad.changeColor(new LEDColor(3, 3), x, y);
		    }
		}
	    }
	}
    }

    public void buttonReleased(Launchpad launchpad, int x, int y) {
	if (output != null) {
	    if ((x < 8) && (y >= 0)) {
		if (chord) {
		    int pitch = notes[x][y] + octaveTranspose * 12;
		    sendNote(ShortMessage.NOTE_OFF, pitch);
		    sendNote(ShortMessage.NOTE_OFF, pitch + 4);
		    sendNote(ShortMessage.NOTE_OFF, pitch + 7);
		    sendNote(ShortMessage.NOTE_OFF, pitch + 11);
		} else {
		    int pitch = notes[x][y] + octaveTranspose * 12;
		    sendNote(ShortMessage.NOTE_OFF, pitch);
		    int degree = scale.getDegree(pitch);
		    if (degree == 0) {// Root
			launchpad.changeColor(new LEDColor(2, 0), x, y);
		    } else if (degree == 4) {// Fith
			launchpad.changeColor(new LEDColor(0, 2), x, y);
		    } else {
			launchpad.changeColor(new LEDColor(2, 2), x, y);
		    }
		}
	    }
	}
    }

    public void launchpadConnected(Launchpad launchpad) {
	// Populate the kayboard with notes
	// 102=top right corner
	this.launchpad = launchpad;
    }

    public void calculateKeyboard(int octave, int right, int up, int key, ScaleMode mode, int horizShift, int vertShift) {
	scale.setKey(key);
	scale.setMode(mode);
	int base = key + octave * 12;// Set the "base note" the root of all the roots
	int degree;
	int topLeft = up * (6 - vertShift) - (right * horizShift);// Degree of topleft pad
	if ((launchpad == null) || (!launchpad.ready()))
	    return;
	for (int x = 0; x < 8; x++) {
	    for (int y = 0; y < 8; y++) {
		// Calculate this all out in degrees, then convert to midi note space at the very end.
		// 0,6 should always = 0.
		degree = topLeft + (x * right) - (y * up);
		notes[x][y] = scale.getShift(degree) + base;
		if (scale.truncateDegree(degree) == 0) {// Root
		    launchpad.changeColor(new LEDColor(2, 0), x, y);
		} else if (scale.truncateDegree(degree) == 4) {// Fifth
		    launchpad.changeColor(new LEDColor(0, 2), x, y);
		} else {
		    launchpad.changeColor(new LEDColor(2, 2), x, y);
		}
	    }
	}
    }

    public void launchpadDisonnected(Launchpad launchpad) {
	launchpad.reset();
    }

    public void close() {
	if (output != null)
	    output.close();
    }

    public void setOutput(MidiDevice out) {
	if (output != null)
	    output.close();
	output = out;
	try {
	    output.open();
	} catch (MidiUnavailableException e) {
	    e.printStackTrace();
	}
    }

    public boolean sendNote(int command, int pitch) {
	if (pitch < 0 || pitch > 127)
	    return false;

	try {
	    ShortMessage msg = new ShortMessage();
	    msg.setMessage(command, pitch, 100);
	    output.getReceiver().send(msg, -1);
	} catch (InvalidMidiDataException e) {
	    e.printStackTrace();
	} catch (MidiUnavailableException e) {
	    e.printStackTrace();
	}
	return true;
    }
}
