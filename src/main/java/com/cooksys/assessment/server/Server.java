package com.cooksys.assessment.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cooksys.assessment.model.Message;

public class Server implements Runnable {
	private Logger log = LoggerFactory.getLogger(Server.class);
	private List<ClientHandler> clients = new ArrayList<ClientHandler>();
	private Set<String> users = new HashSet<String>();
	
	private int port;
	private ExecutorService executor;
	
	public Server(int port, ExecutorService executor) {
		super();
		this.port = port;
		this.executor = executor;
	}

	public void run() {
		log.info("server started");
		ServerSocket ss;
		try {
			ss = new ServerSocket(this.port);
			while (true) {
				Socket socket = ss.accept();
				ClientHandler handler = new ClientHandler(socket, this);
				executor.execute(handler);
				clients.add(handler);
			}
		} catch (IOException e) {
			log.error("Something went wrong :/", e);
		}
	}
	
	public List<ClientHandler> getClients() {
		return clients;
	}
	
	public void broadcast(Message message) {
		for (ClientHandler client : clients) {
			client.display(message);
		}
	}
	
	public void addUser(String username) {
		users.add(username);
	}
	
	public Set<String> getUsers() {
		return users;
	}
	
	public ClientHandler getClient(String username) {
		for (ClientHandler client : clients) {
			if (username.equals(client.getUsername())) {
				return client;
			}
		} return null;
	}
	
}
