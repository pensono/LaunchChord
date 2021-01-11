package com.ethanshea.launchchord;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;

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
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class Main {
    private Launchpad launchpad;
    private HarmonicKeyboard keyboard;
    private JComboBox key;
    private JComboBox mode;
    private JSlider octave;
    private JSlider horizontal;
    private JSlider vertical;
    private JSlider horizontalShift;
    private JSlider verticalShift;

    public static void main(String[] args) {
	new Main().run();
    }

    public Main() {}

    public void run() {
	launchpad = new Launchpad();

	keyboard = new HarmonicKeyboard();
	launchpad.addEventListener(keyboard);

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

	// Listener to ley the harmonic keyboard know when to change it's ways
	KeyboardUpdator keyboardListener = new KeyboardUpdator();
	launchpad.addEventListener(keyboardListener);

	// Input Selector
	JComboBox inputSelector = new JComboBox(inputs.toArray());
	inputSelector.addActionListener(new InputChangeListener());
	JPanel inputGroup = new JPanel();
	inputGroup.add(new JLabel("Input:"), BorderLayout.WEST);
	inputGroup.add(inputSelector);

	// Display Selector
	JComboBox displaySelector = new JComboBox(outputs.toArray());
	displaySelector.addActionListener(new DisplayChangeListener());
	JPanel displayGroup = new JPanel();
	displayGroup.add(new JLabel("Display:"), BorderLayout.WEST);
	displayGroup.add(displaySelector);

	// Output Selector
	JComboBox outputSelector = new JComboBox(outputs.toArray());
	outputSelector.addActionListener(new OutputChangeListener());
	JPanel outputGroup = new JPanel();
	outputGroup.add(new JLabel("Output:"), BorderLayout.WEST);
	outputGroup.add(outputSelector);

	// IO Panel
	JPanel io = new JPanel(new GridLayout(0, 3));
	io.add(inputGroup);
	io.add(displayGroup);
	io.add(outputGroup);
	io.setBorder(BorderFactory.createTitledBorder("Input/Output"));
	frame.add(io, BorderLayout.NORTH);

	JPanel controls = new JPanel(new GridLayout(0, 1));// Vertical

	// ==Keyboard Layout==\\
	JPanel keyLayout = new JPanel(new GridLayout(0, 2));
	keyLayout.setBorder(BorderFactory.createTitledBorder("Keyboard Layout"));

	// Key
	keyLayout.add(new JLabel("Key:"));
	String[] keys = { "C", "Db", "D", "Eb", "E", "F", "F#", "G", "Ab", "A", "Bb", "B" };
	key = new JComboBox(keys);
	key.addActionListener(keyboardListener);
	keyLayout.add(key);

	// Mode
	keyLayout.add(new JLabel("Mode:"));
	mode = new JComboBox(ScaleMode.values());
	mode.addActionListener(keyboardListener);
	keyLayout.add(mode);

	// Octave
	keyLayout.add(new JLabel("Octave:"));
	octave = new JSlider(JSlider.HORIZONTAL, -2, 3, 0);
	octave.setMajorTickSpacing(1);
	octave.setSnapToTicks(true);
	octave.setPaintLabels(true);
	octave.addChangeListener(keyboardListener);
	keyLayout.add(octave);

	// Horizontal increment
	keyLayout.add(new JLabel("Horizontal increment (scale degrees):"));
	horizontal = new JSlider(JSlider.HORIZONTAL, 0, 8, 5);
	horizontal.setMajorTickSpacing(1);
	horizontal.setSnapToTicks(true);
	horizontal.setPaintLabels(true);
	horizontal.addChangeListener(keyboardListener);
	keyLayout.add(horizontal);

	// Vertical increment
	keyLayout.add(new JLabel("Vertical increment (scale degrees):"));
	vertical = new JSlider(JSlider.HORIZONTAL, 0, 8, 4);
	vertical.setMajorTickSpacing(1);
	vertical.setSnapToTicks(true);
	vertical.setPaintLabels(true);
	vertical.addChangeListener(keyboardListener);
	keyLayout.add(vertical);

	// Horizontal Shift
	keyLayout.add(new JLabel("Horizontal shift:"));
	horizontalShift = new JSlider(JSlider.HORIZONTAL, -4, 4, 0);
	horizontalShift.setMajorTickSpacing(1);
	horizontalShift.setSnapToTicks(true);
	horizontalShift.setPaintLabels(true);
	horizontalShift.addChangeListener(keyboardListener);
	keyLayout.add(horizontalShift);

	// Vertical Shift
	keyLayout.add(new JLabel("Vertical shift:"));
	verticalShift = new JSlider(JSlider.HORIZONTAL, -4, 4, 0);
	verticalShift.setMajorTickSpacing(1);
	verticalShift.setSnapToTicks(true);
	verticalShift.setPaintLabels(true);
	verticalShift.addChangeListener(keyboardListener);
	keyLayout.add(verticalShift);

	controls.add(keyLayout);
	frame.add(controls);

	frame.pack();
	frame.setResizable(false);
	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	frame.addWindowListener(new WindowListener() {
	    public void windowOpened(WindowEvent e) {}

	    public void windowClosing(WindowEvent e) {
		launchpad.close();
	    }

	    public void windowClosed(WindowEvent e) {}

	    public void windowIconified(WindowEvent e) {}

	    public void windowDeiconified(WindowEvent e) {}

	    public void windowActivated(WindowEvent e) {}

	    public void windowDeactivated(WindowEvent e) {}
	});
	frame.setVisible(true);
    }

    public class MidiListener implements Receiver {
	public void send(MidiMessage message, long timeStamp) {
	    byte[] msg = message.getMessage();
	    if ((message.getStatus() == ShortMessage.NOTE_OFF)
		    || ((message.getStatus() == ShortMessage.NOTE_ON) && (msg[2] == 0))) {// Note off

	    } else if (message.getStatus() == ShortMessage.NOTE_ON) {// Note on

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
		    MidiDevice input = MidiSystem.getMidiDevice((Info) ((JComboBox) e.getSource()).getSelectedItem());
		    launchpad.setMidiInDevice(input);
		    System.out.println("Input device selected.");
		} catch (MidiUnavailableException e1) {
		    JOptionPane.showMessageDialog(null, "MIDI Device already in use.", "MIDI Device Error",
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }

    public class OutputChangeListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() instanceof JComboBox) {
		try {
		    MidiDevice output = MidiSystem.getMidiDevice((Info) ((JComboBox) e.getSource()).getSelectedItem());
		    keyboard.setOutput(output);
		    System.out.println("Output device selected.");
		} catch (MidiUnavailableException e1) {
		    JOptionPane.showMessageDialog(null, "MIDI Device already in use.", "MIDI Device Error",
			    JOptionPane.ERROR_MESSAGE);
		}
	    }
	}
    }

    public class DisplayChangeListener implements ActionListener {
	public void actionPerformed(ActionEvent e) {
	    if (e.getSource() instanceof JComboBox) {
		try {
		    launchpad.setMidiOutDevice(MidiSystem.getMidiDevice((Info) ((JComboBox) e.getSource())
			    .getSelectedItem()));
		    System.out.println("Display device selected.");
		} catch (MidiUnavailableException e1) {
		    e1.printStackTrace();
		}
	    }
	}
    }

    public class KeyboardUpdator implements ActionListener, ChangeListener, LaunchEventListener {
	public void stateChanged(ChangeEvent e) {
	    trigger();
	}

	public void actionPerformed(ActionEvent e) {
	    trigger();
	}

	public void trigger() {
	    keyboard.calculateKeyboard(octave.getValue(), horizontal.getValue(), vertical.getValue(),
		    key.getSelectedIndex(), (ScaleMode) mode.getSelectedItem(), horizontalShift.getValue(),
		    verticalShift.getValue());
	}

	public void launchpadConnected(Launchpad launchpad) {
	    trigger();
	}

	public void buttonPressed(Launchpad launchpad, int x, int y) {}

	public void buttonReleased(Launchpad launchpad, int x, int y) {}

	public void launchpadDisonnected(Launchpad launchpad) {}
    };
}
