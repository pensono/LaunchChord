package com.ethanshea.launchchord;

public class Scale {
    private ScaleMode mode = ScaleMode.MAJOR;
    private int key;

    public Chord getDiatonicChord(int root) {
	int degree = getDegree(root);
	if (degree == -1)
	    return new Chord(); // Not in the key
	return new Chord(root, getRelativeNote(root, 2, degree), getRelativeNote(root, 4, degree));
    }

    /**
     * Get the next note relative to a given note
     * 
     * @param note The starting note to count from
     * @param positions Number of positions away from the starting note. Negitives will go down in pitch.
     * @return
     */
    public int getRelativeNote(int note, int positions, int degree) {
	return mode.getPitch(degree + positions)+ (((degree + positions) / mode.getNumPitches()) * 12)
		+ ((note - key) / 12) * 12 + key;
    }

    public int getRelativeNote(int note, int positions) {
	int degree = getDegree(note);
	if (degree == -1)
	    return -1;
	return getRelativeNote(note, positions, degree);
    }

    /**
     * Get the scale degree of the current note.
     * @param note
     * @return
     */
    public int getDegree(int note) {
	int degree = -1;
	int octaveLimited = (note - key) % 12;
	for (int i = 0; i < mode.getNumPitches(); i++) {
	    if (mode.getPitch(i) == octaveLimited) {
		degree = i;
		break;
	    }
	}
	return degree;
    }

    public int getKey() {
	return key;
    }

    public void setKey(int key) {
	this.key = key % 12;
    }

    public ScaleMode getMode() {
	return mode;
    }

    public void setMode(ScaleMode mode) {
	this.mode = mode;
    }
}
