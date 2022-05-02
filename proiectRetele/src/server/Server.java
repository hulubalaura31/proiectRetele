package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import common.Customer;
import common.Room;
import common.Transport;

import java.net.Socket;

public class Server implements AutoCloseable {
	private ServerSocket serverSocket;
	private ExecutorService executorService;
	private List<Customer> customers = new ArrayList<Customer>();
	private Map<Integer, Room> storage = Collections.synchronizedMap(new HashMap<>());

	@Override
	public void close() throws Exception {
		stop();

	}

	public void start(int port) throws IOException {
		stop();
		for (int i = 0; i < 5; i++) {
			storage.put(i, new Room(i));
		}
		String[] names = { "Ana", "Maria", "Stefan" };
		for (int i = 0; i < names.length; i++) {
			customers.add(new Customer(names[i]));
		}
		serverSocket = new ServerSocket(port);
		executorService = Executors.newFixedThreadPool(50 * Runtime.getRuntime().availableProcessors());
		final List<Socket> clients = Collections.synchronizedList(new ArrayList<Socket>());
		executorService.execute(() -> {
			while (serverSocket != null && !serverSocket.isClosed()) {
				try {
					final Socket socket = serverSocket.accept();
					executorService.submit(() -> {
						try {
							clients.add(socket);
							ClientState state = new ClientState();
							while (socket != null && !socket.isClosed()) {
								String command = Transport.receive(socket);
								String response = processCommand(command, state, socket);
								String[] splittedCommand = command.strip().split("\\s");
								System.out.println(command + " -> " + response);
								if (state.isAuthenticated) {
									clients.forEach(client -> {
										try {
											Transport.send(response, client);
										} catch (Exception e) {

										}
									});
								}
								if (splittedCommand[1].equals("Reserved") || splittedCommand[1].equals("Blocked")) {
									int seconds;
									if (splittedCommand[1].equals("Reserved")) {
										seconds = 50;
									} else {
										seconds = 30;
									}
									final String newCommand = splittedCommand[0].concat(" Cancel ")
											.concat(splittedCommand[2]).concat(" ").concat(splittedCommand[3]);
									ScheduledExecutorService scheduledExecutor = Executors.newScheduledThreadPool(1);
									final ClientState clientState = state;
									final Socket copiedSocket = socket;
									ScheduledFuture scheduledFuture = scheduledExecutor.schedule(new Callable() {
										public Object call() throws Exception {
											System.out.println("Time ended!");
											String newResponse = processCommand(newCommand, clientState, copiedSocket);
											String a = newResponse;
											if (clientState.isAuthenticated) {
												clients.forEach(client -> {
													try {
														Transport.send(newResponse, client);
													} catch (Exception e) {

													}
												});
											}
											return "Called!";
										}
									}, seconds, TimeUnit.SECONDS);
									System.out.println("result = " + scheduledFuture.get());

									scheduledExecutor.shutdown();
								}

							}

						} catch (Exception e) {

						} finally {
							clients.remove(socket);
						}
					});
				} catch (Exception e) {

				}
			}
		});

	}

	private String processCommand(String command, ClientState state, Socket socket) throws IOException {
		String[] items = command.strip().split("\\s");
		if (state.isAuthenticated) {
			switch (items[0]) {
			case "put":
				if (items.length == 4) {
					if (storage.containsKey(Integer.parseInt(items[2])) && !items[1].equals("towels")) {
						Room existing = storage.get(Integer.parseInt(items[2]));
						if (existing.getStatus().equals("Available")) {
							if (!items[1].equals("Cancel")) {
								existing.setStatus(items[1]);
								existing.setReservedOnName(items[3]);
								storage.put(existing.getIdCamera(), existing);
								String updatedList = "list-";
								for (Map.Entry<Integer, Room> entry : storage.entrySet()) {
									updatedList = updatedList.concat(entry.toString());
									updatedList = updatedList.concat("\n");
								}
								return updatedList;
							}
						} else {
							if ((items[1].equals("Reserved") || items[1].equals("Blocked"))
									&& !existing.getReservedOnName().equals(items[3])) {
								return "message-Room is already locked or reserved by someone else.\n Please choose a different one";
							} else if (items[1].equals("Reserved") && existing.getReservedOnName().equals(items[3])) {
								return "message-Room is already reserved by you.\n It's not possible to block a room that is in status reserved.";
							} else if (items[1].equals("Reserved") && existing.getReservedOnName().equals(items[3])
									&& existing.getStatus().equals("Blocked")) {
								existing.setStatus(items[1]);
								existing.setReservedOnName(items[3]);
								storage.put(existing.getIdCamera(), existing);
								String updatedList = "list-";
								for (Map.Entry<Integer, Room> entry : storage.entrySet()) {
									updatedList = updatedList.concat(entry.toString());
									updatedList = updatedList.concat("\n");
								}
								return updatedList;
							} else if (items[1].equals("Cancel")) {
								String name = existing.getReservedOnName();
								if (items[3].equals(name)) {
									existing.setStatus("Available");
									existing.setReservedOnName("");
									existing.setWithTowels(false);
									storage.put(existing.getIdCamera(), existing);
									String updatedList = "list-";
									for (Map.Entry<Integer, Room> entry : storage.entrySet()) {
										updatedList = updatedList.concat(entry.toString());
										updatedList = updatedList.concat("\n");
									}
									return updatedList;
								} else {
									return "message-The chosen room was reserved or blocked by someone else.\n You do not have rights to cancel the reservation.";
								}
							} else if (existing.getStatus().equals("Blocked")) {
								if (items[1].equals("Reserved")) {
									existing.setStatus(items[1]);
									existing.setReservedOnName(items[3]);
									storage.put(existing.getIdCamera(), existing);
									String updatedList = "list-";
									for (Map.Entry<Integer, Room> entry : storage.entrySet()) {
										updatedList = updatedList.concat(entry.toString());
										updatedList = updatedList.concat("\n");
									}
									return updatedList;
								}

							}
						}
					} else {
						return "message-Room doesn't exist";
					}
				} else if (storage.containsKey(Integer.parseInt(items[2])) && items[1].equals("towels")
						&& items.length == 5) {
					Room existing = storage.get(Integer.parseInt(items[2]));
					existing.setWithTowels(Boolean.parseBoolean(items[4]));
					storage.put(existing.getIdCamera(), existing);
					String updatedList = "list-";
					for (Map.Entry<Integer, Room> entry : storage.entrySet()) {
						updatedList = updatedList.concat(entry.toString());
						updatedList = updatedList.concat("\n");
					}
					return updatedList;
				} else {
					return "message-Please provide room number and a name";
				}
			default:
				return "message-unknown command";
			}
		} else {
			if (items[0].equals("auth")) {
				if (items.length == 2) {
					if (customers.stream().filter(x -> x.getName().equals(items[1])).collect(Collectors.toList())
							.size() > 0) {
						state.isAuthenticated = true;
						sendRoomList(socket, storage);
						return "message-Welcome back";
					} else {
						state.isAuthenticated = true;
						customers.add(new Customer(items[1]));
						sendRoomList(socket, storage);
						return "message-Welcome";
					}
				} else {
					return "message-Please login or register";
				}
			} else {
				return "message-System not available. Please try again later";
			}
		}
	}

	private void sendRoomList(Socket socket, Map<Integer, Room> hotel) throws IOException {
		String updatedList = "list-";
		for (Map.Entry<Integer, Room> entry : storage.entrySet()) {
			updatedList = updatedList.concat(entry.toString());
			updatedList = updatedList.concat("\n");
		}
		Transport.send(updatedList, socket);
	}

	private void stop() throws IOException {
		if (executorService != null) {
			executorService.shutdown();
			executorService = null;
		}
		if (serverSocket != null) {
			serverSocket.close();
			serverSocket = null;
		}

	}

}
