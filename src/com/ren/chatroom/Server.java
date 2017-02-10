package com.ren.chatroom;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class Server {

	public static void main(String[] args) throws UnknownHostException, IOException {
		new Server().start();
	}

	private List<MyChannel> mem = new ArrayList<MyChannel>();

	public void start() throws IOException {
		ServerSocket socket = new ServerSocket(9999);
		while (true) {
			Socket server = socket.accept();
			MyChannel channel = new MyChannel(server);
			mem.add(channel);
			new Thread(channel).start();
		}
	}

	private class MyChannel implements Runnable {

		private DataInputStream dis;
		private DataOutputStream dos;
		private boolean isRunning = true;
		private String name;

		public MyChannel(Socket server) {
			try {
				dis = new DataInputStream(server.getInputStream());
				dos = new DataOutputStream(server.getOutputStream());
				name = dis.readUTF();
				send(name + " 欢迎进入聊天室！");
				sendOthers(name + " 进入了聊天室！", true);
			} catch (IOException e) {
				isRunning = false;
				CloseUtil.closeAll(dis, dos);
				mem.remove(this);
			}
		}

		private String receive() {
			String msg = null;
			try {
				msg = dis.readUTF();
			} catch (IOException e) {
				isRunning = false;
				CloseUtil.closeAll(dis);
				mem.remove(this);
			}
			return msg;
		}

		private void send(String msg) {
			if (msg != null) {
				try {
					dos.writeUTF(msg);
					dos.flush();
				} catch (IOException e) {
					isRunning = false;
					CloseUtil.closeAll(dos);
					mem.remove(this);
				}
			}
		}

		private void sendOthers(String msg, boolean sys) {
			if (msg.startsWith("@") && (msg.indexOf(":") > 0)) {
				String name = msg.substring(1, msg.indexOf(":"));
				String content = msg.substring(msg.indexOf(":") + 1);
				for (MyChannel temp : mem) {
					if (temp.name.equals(name)) {
						temp.send(this.name + " 悄悄对您说：" + content);
					}
				}
			} else {
				for (MyChannel temp : mem) {
					if (temp == this) {
						continue;
					}
					if (sys) {
						temp.send("系统信息："+msg);
					} else {
						temp.send(name + " 对所有人说：" + msg);
					}
				}
			}
		}

		@Override
		public void run() {
			while (isRunning) {
				sendOthers(receive(), false);
			}
		}

	}

}
