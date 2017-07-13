package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ClientHandler implements Runnable {
	private Server server;
	private Logger log = LoggerFactory.getLogger(ClientHandler.class);
	private Socket socket;

	public ClientHandler(Socket socket, Server server) {
		super();
		this.socket = socket;
		this.server = server;
	}
	
	public Socket getSocket() {
		return socket;
	}
	
	// Sends message to client socket to display
	public void display(Message message) {
		try {
			if (!socket.isClosed()) {
				ObjectMapper mapper = new ObjectMapper();
				PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
				String output = mapper.writeValueAsString(message);
				writer.write(output);
				writer.flush();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void run() {
		try {

			ObjectMapper mapper = new ObjectMapper();
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);
				//String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
				long timestamp = new Date().getTime();
				message.setTimestamp(timestamp);

				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						message.setContents(String.format("%s: <%s> has connected.", message.getUTC(), message.getUsername()));
						server.broadcast(message);
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						message.setContents(String.format("%s: <%s> has disconnected.", message.getUTC(), message.getUsername()));
						server.broadcast(message);
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						message.setContents(String.format("%s: <%s> (echo): %s", message.getUTC(), message.getUsername(), message.getContents()));
						display(message);
						break;
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						message.setContents(String.format("%s: <%s> (broadcast): %s", message.getUTC(), message.getUsername(), message.getContents()));
						server.broadcast(message);
						break;
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
