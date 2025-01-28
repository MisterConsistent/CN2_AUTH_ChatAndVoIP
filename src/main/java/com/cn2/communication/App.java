package com.cn2.communication;

import java.io.*;
import java.net.*;

import javax.swing.JFrame;
import javax.swing.JTextField;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.TargetDataLine;
import javax.swing.JButton;
import javax.swing.JTextArea;
import javax.swing.JScrollPane;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.*;
import java.awt.event.*;
import java.awt.Color;
import java.lang.Thread;

public class App extends Frame implements WindowListener, ActionListener {

	/*
	 * Definition of the app's fields
	 */
	static TextField inputTextField;		
	static JTextArea textArea;				 
	static JFrame frame;					
	static JButton sendButton;				
	static JTextField meesageTextField;		  
	public static Color gray;				
	final static String newline="\n";		
	static JButton callButton;				
	
	// TODO: Please define and initialize your variables here...
	// TODO: Please define and initialize your variables here...

	
	private DatagramSocket socket; 
	private InetAddress remoteAddress; 
	private int remotePort = 12345; 

	
	private TargetDataLine microphone; 
	private SourceDataLine speaker; 
	private AudioFormat audioFormat; 

	
	private Thread messageListeningThread; 
	private Thread voiceListeningThread; 

	
	private static final int SAMPLE_RATE = 8000; 
	private static final int SAMPLE_SIZE = 8; 
	private static final int CHANNELS = 1; 

	private void initializeAudio() {
	    try {	
	        // Ορισμός μορφής ήχου
	        audioFormat = new AudioFormat(SAMPLE_RATE, SAMPLE_SIZE, CHANNELS, true, false);

	        // Ρύθμιση μικροφώνου
	        DataLine.Info micInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
	        microphone = (TargetDataLine) AudioSystem.getLine(micInfo);
	        microphone.open(audioFormat);
	        microphone.start();

	        // Ρύθμιση ηχείου
	        DataLine.Info speakerInfo = new DataLine.Info(SourceDataLine.class, audioFormat);
	        speaker = (SourceDataLine) AudioSystem.getLine(speakerInfo);
	        speaker.open(audioFormat);
	        speaker.start();

	        textArea.append("Audio initialized successfully.\n");

	    } catch (LineUnavailableException ex) {
	        textArea.append("Error initializing audio: " + ex.getMessage() + newline);
	    }
	}
	private void sendVoice() {
	    byte[] buffer = new byte[1024];
	    try {
	        while (true) {
	            int bytesRead = microphone.read(buffer, 0, buffer.length);
	            DatagramPacket packet = new DatagramPacket(buffer, bytesRead, remoteAddress, remotePort);
	            socket.send(packet);
	        }
	    } catch (IOException ex) {
	        textArea.append("Error sending voice data: " + ex.getMessage() + newline);
	    }
	}
	private void listenForVoice() {
	    byte[] buffer = new byte[1024];
	    try {
	        while (true) {
	            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	            socket.receive(packet);
	            speaker.write(packet.getData(), 0, packet.getLength());
	        }
	    } catch (IOException ex) {
	        textArea.append("Error receiving voice data: " + ex.getMessage() + newline);
	    }
	}

	/**
	 * Construct the app's frame and initialize important parameters
	 */
	public App(String title) {
		
		/*
		 * 1. Defining the components of the GUI
		 */
		
		// Setting up the characteristics of the frame
		super(title);									
		gray = new Color(254, 254, 254);		
		setBackground(gray);
		setLayout(new FlowLayout());			
		addWindowListener(this);	
		
		// Setting up the TextField and the TextArea
		inputTextField = new TextField();
		inputTextField.setColumns(20);
		
		// Setting up the TextArea.
		textArea = new JTextArea(10,40);			
		textArea.setLineWrap(true);				
		textArea.setEditable(false);			
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
		
		//Setting up the buttons
		sendButton = new JButton("Send");			
		callButton = new JButton("Call");			
						
		/*
		 * 2. Adding the components to the GUI
		 */
		add(scrollPane);								
		add(inputTextField);
		add(sendButton);
		add(callButton);
		
		/*
		 * 3. Linking the buttons to the ActionListener
		 */
		sendButton.addActionListener(this);			
		callButton.addActionListener(this);	

		
	}
	
	/**
	 * The main method of the application. It continuously listens for
	 * new messages.
	 */
	public static void main(String[] args){
	
		/*
		 * 1. Create the app's window
		 */
		App app = new App("CN2 - AUTH");  // TODO: You can add the title that will displayed on the Window of the App here																		  
		app.setSize(500,250);				  
		app.setVisible(true);
		try {
	        app.socket = new DatagramSocket(12346); 
	        app.remoteAddress = InetAddress.getByName("127.0.0.1"); 
	        app.remotePort = 12345; 
	        app.textArea.append("Application started. Listening on port 12345." + newline);
	    } catch (SocketException | UnknownHostException ex) {
	        app.textArea.append("Error initializing socket: " + ex.getMessage() + newline);
	        return;
	    }

		/*
		 * 2. 
		 */
		do {
	        try {
	            // 3.1 Λήψη εισερχόμενων πακέτων
	            byte[] buffer = new byte[1024];
	            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
	            app.socket.receive(packet);

	            // 3.2 Επεξεργασία των δεδομένων
	            String message = new String(packet.getData(), 0, packet.getLength());
	            app.textArea.append("Remote: " + message + newline);

	        } catch (IOException ex) {
	            app.textArea.append("Error receiving data: " + ex.getMessage() + newline);
	        }

	        // 3.3 Μικρή καθυστέρηση για να μην επιβαρύνεται το σύστημα
	        try {
	            Thread.sleep(100); // 100 ms delay
	        } catch (InterruptedException e) {
	            Thread.currentThread().interrupt();
	        }
	    } while (true);
	}
	
	/**
	 * The method that corresponds to the Action Listener. Whenever an action is performed
	 * (i.e., one of the buttons is clicked) this method is executed. 
	 */
	@Override
	public void actionPerformed(ActionEvent e) {
		
	

		/*
		 * Check which button was clicked.
		 */
		if (e.getSource() == sendButton){
			
			 String message = inputTextField.getText();
		        if (message.isEmpty() || socket == null) {
		            textArea.append("Cannot send message. Socket is not initialized.\n");
		            return;
		        }

		        try {
		            byte[] buffer = message.getBytes();
		            DatagramPacket packet = new DatagramPacket(buffer, buffer.length, remoteAddress, remotePort);
		            socket.send(packet);

		            textArea.append("Local: " + message + newline);
		            inputTextField.setText(""); 
		        } catch (IOException ex) {
		            textArea.append("Error sending message: " + ex.getMessage() + newline);
		        }
		
			
		}else if(e.getSource() == callButton){
			
			if (microphone == null || speaker == null) {
	            initializeAudio();
	        }

	        // Εκκίνηση νημάτων για αποστολή και λήψη φωνής
	        voiceListeningThread = new Thread(this::listenForVoice);
	        voiceListeningThread.start();

	        Thread voiceSendingThread = new Thread(this::sendVoice);
	        voiceSendingThread.start();

	        textArea.append("VoIP communication started.\n");
			
			
		}
			

	}

	/**
	 * These methods have to do with the GUI. You can use them if you wish to define
	 * what the program should do in specific scenarios (e.g., when closing the 
	 * window).
	 */
	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowClosing(WindowEvent e) {
		// TODO Auto-generated method stub
		dispose();
        	System.exit(0);
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub	
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub	
	}
}
