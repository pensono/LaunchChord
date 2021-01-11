package com.ethanshea.launchchord;

public enum ScaleMode {
    MAJOR("Major", 0, 2, 4, 5, 7, 9, 11), MINOR("Minor", 0, 2, 3, 5, 7, 8, 10), IONIAN("Ionian", 0, 2, 4, 5, 7, 9, 11),
    DORIAN("Dorian", 0, 2, 3, 5, 7, 9, 10), AEOLIAN("Aeolian", 0, 2, 3, 5, 7, 8, 10);

    private String name;
    private int[] pitches;

    private ScaleMode(String name, int... pitches) {
	this.name = name;
	this.pitches = pitches;
    }

    /**
     * Returns all scales, including aliases
     * 
     * @return
     */
    public static ScaleMode[] getAllScales() {
	return values();
    }

    public int getPitch(int degree) {
	return pitches[degree % pitches.length];
    }

    public int getNumPitches() {
	return pitches.length;
    }

    public int[] getPitches() {
	return pitches;
    }

    public String getName() {
	return name;
    }

    public String toString() {
	return name;
    }
}
