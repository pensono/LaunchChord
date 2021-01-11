package com.ethanshea.launchchord;

public enum ScaleMode {
    IONIAN("Ionian (Major)", 0, 2, 4, 5, 7, 9, 11), DORIAN("Dorian", 0, 2, 3, 5, 7, 9, 10),
    PHRYGIAN("Phrygian", 0, 1, 3, 5, 7, 8, 10), LYDIAN("Lydian", 0, 2, 4, 6, 7, 9, 11),
    MIXOLYDIAN("Mixolydian", 0, 2, 4, 5, 7, 9, 10), AEOLIAN("Aeolian (Natural Minor)", 0, 2, 3, 5, 7, 8, 10),
    LOCRIAN("Locrian", 0, 1, 3, 5, 6, 8, 10), HARMONIC_MINOR("Harmonic Minor", 0, 2, 3, 5, 7, 8, 11),
    WHOLE_TONE("Whole Tone", 0, 2, 4, 6, 8, 10);

    int[] notes;
    String name;

    private ScaleMode(String name, int... notes) {
	this.notes = notes;
	this.name = name;
    }

    public int getNoteScaleDegree(int note) {
	int noteMod = note % 12;
	for (int i = 0; i < notes.length; i++) {
	    if (noteMod == notes[i])
		return i;
	}
	return -1;
    }

    public int getShift(int degrees) {
	// Get the combined number of octaves and semitones from a root
	int degree = degrees % notes.length;
	if (degree < 0)
	    degree += notes.length;
	if (degrees < 0)
	    degrees -= notes.length;// Make negitives work with rounding towards 0
	return 12 * (degrees / notes.length) + notes[degree];
    }

    public int getNoteCount() {
	return notes.length;
    }

    public String toString() {
	return name;
    }
}
