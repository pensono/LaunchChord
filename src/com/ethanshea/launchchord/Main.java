package com.ethanshea.launchchord;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.HashMap;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiDevice.Info;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSlider;

public class Main {
    private MidiDevice input;
    private MidiDevice output;
    private Scale scale;
    
    private JSlider bassNotes;
    private JSlider bassOctave;
    private JSlider voices;
    
    private HashMap<Integer, Chord> playedChords;

    public static void main(String[] args) {
	new Main().run();
    }
    
    public Main(){
	scale = new Scale();
	playedChords = new HashMap<Integer, Chord>();
    }

    public void run() {
	JFrame frame = new JFrame("LaunchChord");
	
	Info[] devices = MidiSystem.getMidiDeviceInfo();
	ArrayList<Info> inputs = new ArrayList<Info>();
	ArrayList<Info> outputs = new ArrayList<Info>();

	for (Info i : devices) {
	    try {
		MidiDevice device = MidiSystem.getMidiDevice(i);
		if (device.getMaxTransmitters() != 0)
		    inputs.add(i);
		if (device.getMaxReceivers() != 0)
		    outputs.add(i);
	    } catch (MidiUnavailableException e) {
		// Not applicable
	    }
	}

	//Input Selector
	JComboBox inputSelector = new JComboBox(inputs.toArray());
	inputSelector.addActionListener(new InputChangeListener());
	JPanel inputGroup = new JPanel();
	inputGroup.add(new JLabel("Input:"),BorderLayout.WEST);
	inputGroup.add(inputSelector);
	
	//Output Selector
	JComboBox outputSelector = new JComboBox(outputs.toArray());
	outputSelector.addActionListener(new OutputChangeListener());
	JPanel outputGroup = new JPanel();
	outputGroup.add(new JLabel("Output:"),BorderLayout.WEST);
	outputGroup.add(outputSelector);
	
	//IO Panel
	JPanel io = new JPanel(new GridLayout(0,2));
	io.add(inputGroup);
	io.add(outputGroup);
	io.setBorder(BorderFactory.createTitledBorder("Input/Output"));
	frame.add(io,BorderLayout.NORTH);

	// Key Selector
	JComboBox keySelector = new JComboBox(MusicUtils.noteNames);
	keySelector.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		scale.setKey(((JComboBox) e.getSource()).getSelectedIndex());
	    }
	});
	JPanel keySelectorGroup = new JPanel();
	keySelectorGroup.setLayout(new GridLayout(0,2));
	keySelectorGroup.add(new JLabel("Key: "), BorderLayout.WEST);
	keySelectorGroup.add(keySelector);
	
	// Mode Selector
	JComboBox modeSelector = new JComboBox(ScaleMode.getAllScales());
	modeSelector.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		scale.setMode((ScaleMode) ((JComboBox) e.getSource()).getSelectedItem());
	    }
	});
	JPanel modeSelectorGroup = new JPanel();
	modeSelectorGroup.setLayout(new GridLayout(0,2));
	modeSelectorGroup.add(new JLabel("Mode: "), BorderLayout.WEST);
	modeSelectorGroup.add(modeSelector);
	
	//Scale Selector
	JPanel scaleSelectorSubgroup = new JPanel();
	scaleSelectorSubgroup.setLayout(new GridLayout(2,0));
	scaleSelectorSubgroup.setBorder(BorderFactory.createTitledBorder("Scale"));
	scaleSelectorSubgroup.add(keySelectorGroup);
	scaleSelectorSubgroup.add(modeSelectorGroup);
	
	JPanel scaleSelectorGroup = new JPanel();
	scaleSelectorGroup.add(scaleSelectorSubgroup,BorderLayout.NORTH);
	scaleSelectorGroup.add(new JPanel());
	frame.add(scaleSelectorGroup,BorderLayout.EAST);
	
	//===Voicing controls===\\
	JPanel bass = new JPanel();
	bass.setBorder(BorderFactory.createTitledBorder("Bass"));
	bass.setLayout(new GridLayout(0,2));	

	//Bass notes
	bass.add(new JLabel("Bass Notes:"));
	bassNotes = new JSlider(JSlider.HORIZONTAL,0,3,2);
	bassNotes.setMajorTickSpacing(1);
	bassNotes.setSnapToTicks(true);
	bassNotes.setPaintLabels(true);
	bass.add(bassNotes);

	//Bass Octave
	bass.add(new JLabel("Bass Octave:"));
	bassOctave = new JSlider(JSlider.HORIZONTAL,-3,1,-1);
	bassOctave.setMajorTickSpacing(1);
	bassOctave.setSnapToTicks(true);
	bassOctave.setPaintLabels(true);
	bass.add(bassOctave);
	
	//Voicing Panel
	JPanel voicing = new JPanel();
	voicing.setBorder(BorderFactory.createTitledBorder("Voicing"));
	voicing.setLayout(new GridLayout(0,2));	
	
	//Note Number
	voicing.add(new JLabel("Voices:"));
	voices = new JSlider(JSlider.HORIZONTAL,0,10,4);
	voices.setMajorTickSpacing(1);
	voices.setSnapToTicks(true);
	voices.setPaintLabels(true);
	voicing.add(voices);
	
	JPanel mainControls = new JPanel();
	mainControls.setLayout(new GridLayout(2,0));
	mainControls.add(bass,BorderLayout.NORTH);
	mainControls.add(voicing);
	frame.add(mainControls);
	
	frame.pack();
	//frame.setResizable(false);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.addWindowListener(new WindowListener() {
	    public void windowOpened(WindowEvent e) {}

	    public void windowClosing(WindowEvent e) {
		if (input != null)
		    input.close();
		if (output != null){
		    //TODO send out all notes off message
		    output.close();
		}
	    }

	    public void windowClosed(WindowEvent e) {}

	    public void windowIconified(WindowEvent e) {}

	    public void windowDeiconified(WindowEvent e) {}

	    public void windowActivated(WindowEvent e) {}

	    public void windowDeactivated(WindowEvent e) {}
	});
	frame.setVisible(true);
    }
    
    public Chord voiceChord(Chord base, int bassNotes, int bassOctave, int voices){
	int root = base.getLowest();
	Chord chord = new Chord();
	for (int i=0;i>-bassNotes;i--){
	    chord.addNote(root+((bassOctave+i)*12));
	}
	
	return chord;
    }

    public class MidiListener implements Receiver {
	public void send(MidiMessage message, long timeStamp) {
	    byte[] msg = message.getMessage();
	    if ((message.getStatus() == ShortMessage.NOTE_OFF) || ((message.getStatus() == ShortMessage.NOTE_ON) && (msg[2] == 0))) {// Note off
		System.out.println("Note off " + MusicUtils.getNoteName(msg[1]) + "-" + msg[2]);
		if (playedChords.containsKey(new Integer(msg[1]))){
		    try {
			MusicUtils.sendChordOff(playedChords.get(new Integer(msg[1])),msg[2],output.getReceiver());
		    } catch (MidiUnavailableException e) {
			e.printStackTrace();
		    }
		    playedChords.remove(new Integer(msg[1]));
		}
	    } else if (message.getStatus() ==ShortMessage.NOTE_ON) {// Note on
		System.out.println("Note on " + MusicUtils.getNoteName(msg[1]) + "-" + msg[2]);
		Chord chord = voiceChord(scale.getDiatonicChord(msg[1]), bassNotes.getValue(), bassOctave.getValue(), voices.getValue());
		playedChords.put(new Integer(msg[1]), chord);
		try {
		    MusicUtils.sendChordOn(chord,msg[2],output.getReceiver());
		} catch (MidiUnavailableException e) {
		    e.printStackTrace();
		}
	    } else if (message.getStatus() == ShortMessage.CONTROL_CHANGE) {// MIDI CC
		System.out.println("CC " + msg[1] + ": " + msg[2]);
	    } else if (message.getStatus() == ShortMessage.PITCH_BEND) {// Pitch bend
		System.out.println("Pitch Bend " + msg[2] + " " + msg[1]);
	    } else if (message.getStatus() == ShortMessage.CHANNEL_PRESSURE) {// Pitch bend
		System.out.println("Aftertouch " + msg[1]);
	    } else {
		System.out.println("Unknown Midi Message. #" + message.getStatus() + " data:" + (msg.length - 1));
	    }
	}

	public void close() {}
    }

    public class InputChangeListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() instanceof JComboBox) {
		try {
		    if (input != null) {
			input.close();
		    }
		    input = MidiSystem.getMidiDevice((Info) ((JComboBox) e.getSource()).getSelectedItem());
		    input.open();
		    input.getTransmitter().setReceiver(new MidiListener());
		    System.out.println("Input device selected.");
		} catch (MidiUnavailableException e1) {
		    JOptionPane.showMessageDialog(null, "MIDI Device already in use.", "MIDI Device Error", JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }
    
    public class OutputChangeListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() instanceof JComboBox) {
		try {
		    if (output != null) {
			output.close();
		    }
		    output = MidiSystem.getMidiDevice((Info) ((JComboBox) e.getSource()).getSelectedItem());
		    output.open();
		    System.out.println("Output device selected.");
		} catch (MidiUnavailableException e1) {
		    JOptionPane.showMessageDialog(null, "MIDI Device already in use.", "MIDI Device Error", JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }
}
