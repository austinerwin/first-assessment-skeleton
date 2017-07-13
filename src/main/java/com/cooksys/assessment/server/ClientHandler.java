package com.cooksys.assessment.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

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
			//PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));

			while (!socket.isClosed()) {
				String raw = reader.readLine();
				Message message = mapper.readValue(raw, Message.class);

				switch (message.getCommand()) {
					case "connect":
						log.info("user <{}> connected", message.getUsername());
						message.setContents(String.format("User <%s> has connected.", message.getUsername()));
						server.broadcast(message);
						break;
					case "disconnect":
						log.info("user <{}> disconnected", message.getUsername());
						this.socket.close();
						break;
					case "echo":
						log.info("user <{}> echoed message <{}>", message.getUsername(), message.getContents());
						message.setContents(String.format("<%s> (echo): %s", message.getUsername(), message.getContents()));
						display(message);
						break;
					case "broadcast":
						log.info("user <{}> broadcasted message <{}>", message.getUsername(), message.getContents());
						message.setContents(String.format("<%s> (broadcast): <%s>", message.getUsername(), message.getContents()));
						server.broadcast(message);
				}
			}

		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}

}
