package com.ethanshea.launchchord;

import java.awt.Color;

public class LEDColor {
	int red;
	int green;
	boolean flashing;
	
	public LEDColor(int red, int green){
	    this.red = red%4;
	    this.green = green%4;
	}
	
	public int getBitfeild(){
	    return green*0x10+red+12; //12=normal operation flag
	}
	
	public final static LEDColor BLANK= new LEDColor(0,0);
	public final static LEDColor RED= new LEDColor(3,0);
	public final static LEDColor GREEN= new LEDColor(0,3);
	public final static LEDColor ORANGE= new LEDColor(3,1);
}