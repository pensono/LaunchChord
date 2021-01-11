package com.ethanshea.launchchord;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.ComboBoxModel;

public class MusicUtils {
    public static final String[] noteNames = { "C", "Db", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B" };
    public static String[] modeNames = {"Ionian","Dorian","Phrygian","Lydian","Mixolydian","Aeolian","Locrian"};

    public static String getNoteName(int note) {
	return noteNames[note % 12] + (note / 12);
    }

    public static String getNoteName(int note, boolean omitOctave) {
	return noteNames[note % 12] + (omitOctave ? "" : (note / 12));
    }

    public static void sendChordOn(Chord chord, int velocity, Receiver to) {
	for (int note : chord.getNotes()) {
	    sendNoteOn(note, velocity, to);
	}
    }

    public static void sendNoteOn(int note, int velocity, Receiver to) {
	ShortMessage msg = new ShortMessage();
	try {
	    msg.setMessage(ShortMessage.NOTE_ON, note, velocity);
	} catch (InvalidMidiDataException e) {
	    e.printStackTrace();
	}
	to.send(msg, -1);
    }
    
    public static void sendChordOff(Chord chord, int velocity, Receiver to) {
	for (int note : chord.getNotes()) {
	    sendNoteOff(note, velocity, to);
	}
    }

    public static void sendNoteOff(int note, int velocity, Receiver to) {
	ShortMessage msg = new ShortMessage();
	try {
	    msg.setMessage(ShortMessage.NOTE_OFF, note, velocity);
	} catch (InvalidMidiDataException e) {
	    e.printStackTrace();
	}
	to.send(msg, -1);
    }
}
