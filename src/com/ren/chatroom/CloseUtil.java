package com.ren.chatroom;

import java.io.Closeable;
import java.io.IOException;

public class CloseUtil {
	
	public static void closeAll(Closeable... io){
		for (Closeable temp : io) {
			if(null!=temp){
				try {
					temp.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

}
