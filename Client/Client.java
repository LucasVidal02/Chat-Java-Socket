package Client;

import java.awt.EventQueue;

import Client.ScreenClient;

public class Client {
	
	public static void main(String[] args) {
		
		EventQueue.invokeLater(new Runnable() {
			
			public void run() {
				
				try {
					
					ScreenClient frame = new ScreenClient ();
				
				} catch (Exception e) {
					
					e.printStackTrace();
				
				}
			}
		});
	}
}
