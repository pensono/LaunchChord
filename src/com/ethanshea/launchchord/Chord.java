package com.ethanshea.launchchord;

import java.util.ArrayList;

public class Chord {
    private ArrayList<Integer> notes;

    public Chord() {
	notes = new ArrayList<Integer>();
    }

    public Chord(int... note) {
	this();
	for (int n : note) {
	    notes.add(n);
	}
    }
    
    public int getLowest(){
	int lowest= 256;
	for (int i:notes){
	    if (i<lowest)
		lowest = i;
	}
	return lowest;
    }

    public void addNote(int note) {
	if ((note > 0) && (note < 128))
	    notes.add(note);
    }

    public String toString() {
	StringBuilder sb = new StringBuilder("{");
	for (int note : notes) {
	    sb.append(MusicUtils.getNoteName(note));
	}
	return sb.toString();
    }

    public ArrayList<Integer> getNotes() {
	return notes;
    }
}
