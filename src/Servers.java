import java.awt.*;
import java.awt.event.*;

import javax.swing.JFrame;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import javax.swing.*;


public class Servers extends JFrame implements ActionListener{

	private static final long serialVersionUID = 1L;
	JPanel panel1 = new JPanel();
	JPanel panel2 = new JPanel();
	JPanel panel3 = new JPanel();
	JButton btnSend = new JButton("Send");
	JButton btnExit = new JButton("Exit");
	TextArea taSend = new TextArea("", 4, 48, TextArea.SCROLLBARS_NONE);
	TextArea taReceive = new TextArea("", 15, 45, TextArea.SCROLLBARS_VERTICAL_ONLY);
	ServerSocket ss = null;
	Socket socket = new Socket();
	BufferedReader br;
	PrintStream ps;
	static int numberInstance = 0;
	
	public static void main(String args[])
	{      
		new Servers();   
	}
	
	public Servers()
	{
		
		btnSend.addActionListener(this);
		btnExit.addActionListener(this);
		btnSend.setMnemonic('1');
		btnExit.setMnemonic('2');
		btnSend.setFocusable(false);
		btnExit.setFocusable(false);
		setTitle("Server");
		setLayout(new FlowLayout());
		setSize(450, 420);
		setResizable(false);
		panel1.add(taReceive, BorderLayout.EAST);
		panel2.add(taSend, BorderLayout.EAST);
		panel3.add(btnSend, BorderLayout.WEST);
		panel3.add(btnExit, BorderLayout.EAST);
		// taReceive.setSize(590, 320);
		// taSend.setSize(590, 150);
	    // taReceive.setEnabled(false);
		taReceive.setFocusable(false);
		taSend.setEnabled(true);
		add(panel1, BorderLayout.NORTH);
		add(panel2, BorderLayout.SOUTH);
		add(panel3); 
		
		//stop listening window events
		this.addWindowListener(new WindowAdapter() {       
			public void windowClosing(WindowEvent e)
			{          
				System.exit(0);       
			}     
		});
		
		setBackground(Color.GRAY);
		setLocation(300, 120);
		setVisible(true);
		
		while (true)     
		{         
			try       
			{          
				if ((ss = new ServerSocket(9998)) != null)         
				{            
					taReceive.append("System: Listening...\n"); 
					System.out.println("server socket open.");
					break;         
				}        
			} 
			catch (IOException e)       
			{          
				e.printStackTrace();       
			}     
		}
		
		while (true)     
		{       
			try       
			{          
				if ((socket = ss.accept()) != null)         
				{            
					taReceive.append("System: Client connected!\n");  
					
					Thread t = new Thread(new ClientHandler(socket)); 
					t.start();
					taReceive.append("System: New thread created!\n");
					
					/*//create input stream
					InputStream ins = socket.getInputStream();           
					InputStreamReader isr = new InputStreamReader(ins, "utf-8");           
          
					br = new BufferedReader(isr);              
					//create output stream          
					OutputStream os = socket.getOutputStream();           
					ps = new PrintStream(os);             
					break;  */       
				}          
			} 
			catch (IOException e)       
			{          
				e.printStackTrace();       
			}     
		}

	}
	
	public class ClientHandler extends JFrame implements Runnable, ActionListener
	{
		private BufferedReader bufferedReader;
		private Socket socket;
		private PrintStream myPrintStream;
		boolean isClientDisconnected = false;
		boolean isServerDisconnected = false;
		
		JPanel editPanel = new JPanel();
		JPanel actionPanel = new JPanel();
		JButton sendButton = new JButton("Send");
		JButton exitButton = new JButton("Exit");
		TextArea editTa = new TextArea("", 4, 48, TextArea.SCROLLBARS_NONE);
		
		public ClientHandler(Socket clientSocket)
		{
			sendButton.addActionListener(this);
			exitButton.addActionListener(this);
			setTitle("Server" + numberInstance);
			setLayout(new FlowLayout());
			setSize(450, 420);
			setResizable(true);
			editPanel.add(editTa);
			actionPanel.add(sendButton);
			actionPanel.add(exitButton);
			add(editPanel);
			add(actionPanel);
			//stop listening window events
			this.addWindowListener(new WindowAdapter() {       
				public void windowClosing(WindowEvent e)
				{          
					try 
					{
						socket.close();
						isServerDisconnected = true;
					} 
					catch (IOException e1) 
					{
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					System.out.println("Server stopped.");       
				}     
			});
			setVisible(true);
			
			try
			{
				socket = clientSocket;
				InputStreamReader isReader = new InputStreamReader(socket.getInputStream());
				bufferedReader = new BufferedReader(isReader);
				//create output stream          
				OutputStream os = socket.getOutputStream();           
				myPrintStream = new PrintStream(os); 
			}
			catch(IOException ex)
			{
				ex.printStackTrace();
			}
		}
		
		public void run()
		{
			String receive = null;     
			try      
			{         
				while (!isClientDisconnected && !isServerDisconnected)       
				{         
					try         
					{            
						receive = bufferedReader.readLine();         
					} 
					catch (IOException ex)         
					{
						isClientDisconnected = true;
						ex.printStackTrace();         
					}                   
					if (!(receive.equals(""))) 
						taReceive.append(">>>> " + receive + "\n");         
				}      
			} 
			catch (Exception ie)      
			{    
				System.out.println("Client disconnected");
				taReceive.append("System: Client disconnected. Thread finished.\n");
				dispose();
				return; //finish thread;
				     
			}
		}
		
		@SuppressWarnings("deprecation")    
		private void sendMessage(String message)   
		{      
			Date now = new Date();      
			String time = timeFormat(now.getHours()) + ":" + timeFormat(now.getMinutes()) + ":" + timeFormat(now.getSeconds());     
			if (!(message.isEmpty()))     
			{
				//message = time + " : " + message + "\n";       
				taReceive.append("<<<< " + message + "\n");        
				// message = new String(message.getBytes("gbk"),"gb2312");            
				if (myPrintStream.checkError()) 
					taReceive.append("System: Client not connected.\n");       
				else       
				{          
					myPrintStream.println(message);         
					myPrintStream.flush();          
					taSend.setText("");       
				}      
			}
			else 
				taReceive.append("System: Cannot send empty message.\n");   
		}
		
		@Override    
		public void actionPerformed(ActionEvent e)   
		{      
			if (e.getActionCommand() == "Exit")     
			{
				try 
				{
					socket.close();
					isServerDisconnected = true;
				} 
				catch (IOException e1) 
				{
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				System.out.println("Server stopped.");        
				dispose();        
				//System.exit(0);      
			} 
			else 
				this.sendMessage(editTa.getText().toString());    
		}
	}
	
	
	@Override    
	public void actionPerformed(ActionEvent e)   
	{      
		System.out.println("Server stopped.");        
		dispose();        
		System.exit(0);          
	}
	
	//time formatting 
	public String timeFormat(int time)   
	{      
		if (time < 10) return "0" + time;     
		else return "" + time;   
	} 
	
	

	
}
