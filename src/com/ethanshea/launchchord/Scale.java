package com.ethanshea.launchchord;

public class Scale {
    private ScaleMode mode = ScaleMode.IONIAN;
    private int key=0;
    
    public boolean isNoteScaleDegree(int note, int degree){
	return getDegree(note) == degree;
    }
    
    public int getDegree(int note){
	return mode.getNoteScaleDegree(note-key);
    }
    
    public int getShift(int degrees){
	return mode.getShift(degrees);
    }

    public int getNoteCount() {
	return mode.getNoteCount();
    }

    public void setKey(int key) {
	this.key = key%12;
    }

    public void setMode(ScaleMode mode) {
	this.mode = mode;
    }
    
    public int truncateDegree(int degree){
	int val = degree % mode.getNoteCount();
	if (val < 0)
	    val += mode.getNoteCount();
	return val;
    }
}
