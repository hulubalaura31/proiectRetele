package client;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Text;

import common.Customer;
import common.Settings;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;

public class Program extends Shell {
	private Client client;
	private Customer customer;
	private String listOfRooms = "";
	private Text textAuth;
	private Text textCommand;
	private Text textMessages;
	private String chosenRoom = "";

	/**
	 * Launch the application.
	 * 
	 * @param args
	 */
	public static void main(String args[]) {
		try {
			Display display = Display.getDefault();
			Program shell = new Program(display);
			shell.open();
			shell.layout();
			while (!shell.isDisposed()) {
				if (!display.readAndDispatch()) {
					display.sleep();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the shell.
	 * 
	 * @param display
	 * @throws IOException
	 * @throws UnknownHostException
	 */
	public Program(Display display) throws UnknownHostException, IOException {
		super(display, SWT.SHELL_TRIM);
		addShellListener(new ShellAdapter() {
			@Override
			public void shellClosed(ShellEvent e) {
				try {
					client.close();
				} catch (Exception exception) {

				}
			}
		});
		setLayout(new GridLayout(1, false));

		List listRooms = new List(this, SWT.BORDER);
		GridData gd_listRooms = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_listRooms.heightHint = 184;
		gd_listRooms.widthHint = 553;
		listRooms.setLayoutData(gd_listRooms);

		Label lblNewLabel = new Label(this, SWT.NONE);
		lblNewLabel.setText("Please login or register first");

		textAuth = new Text(this, SWT.BORDER);
		textAuth.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				try {
					if (e.keyCode == SWT.CR && textAuth.getText().trim().length() > 0) {
						String textToSend = "auth ";
						textToSend = textToSend.concat(textAuth.getText());
						client.send(textToSend);
						textAuth.setEnabled(false);
					}
				} catch (Exception eception) {
				}
			}

		});
		GridData gd_textAuth = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
		gd_textAuth.heightHint = 26;
		textAuth.setLayoutData(gd_textAuth);

		Label lblNewLabel_1 = new Label(this, SWT.NONE);
		lblNewLabel_1.setText("Desired Room:");

		textCommand = new Text(this, SWT.BORDER);
		textCommand.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		Button btnTowels = new Button(this, SWT.NONE);
		btnTowels.setEnabled(false);

		Button btnBlock = new Button(this, SWT.NONE);
		btnBlock.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				if (textCommand.getText().trim().length() > 0) {
					try {
						String messageToSend = "put Blocked ";

						messageToSend = messageToSend.concat(textCommand.getText());
						messageToSend = messageToSend.concat(" ");
						messageToSend = messageToSend.concat(textAuth.getText());
						client.send(messageToSend);
						chosenRoom = "";
						chosenRoom = chosenRoom.concat(textCommand.getText());
						textCommand.setText("");
					} catch (IOException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				} else {
					textMessages.setText("Please provide a room id");
				}
			}
		});
		btnBlock.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnBlock.setText("Block");
		
				Button btnReserve = new Button(this, SWT.NONE);
				btnReserve.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseUp(MouseEvent e) {
						try {
							String messageToSend = "put Reserved ";
							if (chosenRoom.equals("")) {
								if (textCommand.getText().trim().length() > 0) {
									chosenRoom = chosenRoom.concat(textCommand.getText().trim());
									messageToSend = messageToSend.concat(textCommand.getText());
									messageToSend = messageToSend.concat(" ");
									messageToSend = messageToSend.concat(textAuth.getText().trim());
									String cancelReservation = "";
									client.send(messageToSend);
								}

							} else {
								if (textCommand.getText().trim().equals("")) {
									textMessages.setText("Please chooose first a room that is available.");
								} else {
									messageToSend = messageToSend.concat(chosenRoom);
									messageToSend = messageToSend.concat(" ");
									messageToSend = messageToSend.concat(textAuth.getText().trim());
									client.send(messageToSend);
								}
							}
							btnTowels.setEnabled(true);
							chosenRoom ="";
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				btnReserve.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				btnReserve.setText("Reserve");
		
				Button btnCancel = new Button(this, SWT.NONE);
				btnCancel.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseUp(MouseEvent e) {
						try {
							String messageToSend = "put Cancel ";
							String a = chosenRoom;
							messageToSend = messageToSend.concat(chosenRoom);
							messageToSend = messageToSend.concat(" ");
							messageToSend = messageToSend.concat(textAuth.getText().trim());
							client.send(messageToSend);
							btnTowels.setEnabled(false);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				});
				btnCancel.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
				btnCancel.setText("Cancel");

		

		btnTowels.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseUp(MouseEvent e) {
				try {
					String messageToSend = "put towels ";
					messageToSend = messageToSend.concat(chosenRoom);
					messageToSend = messageToSend.concat(" ");
					messageToSend = messageToSend.concat(textAuth.getText().trim());
					messageToSend = messageToSend.concat(" ");
					messageToSend = messageToSend.concat("true");
					client.send(messageToSend);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
		});
		btnTowels.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
		btnTowels.setText("I want towels");
		new Label(this, SWT.NONE);
		new Label(this, SWT.NONE);
		
				textMessages = new Text(this, SWT.BORDER);
				GridData gd_textMessages = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
				gd_textMessages.heightHint = 62;
				textMessages.setLayoutData(gd_textMessages);
		createContents();
		client = new Client(Settings.HOST, Settings.PORT, message -> {
			Display.getDefault().asyncExec(() -> {
				String test = message;

				if (message.split("-")[0].equals("list")) {
					String[] splittedMessage = message.split("-")[1].split("\n");
					listRooms.removeAll();
					for (int i = 0; i < splittedMessage.length; i++) {
						listRooms.add(splittedMessage[i]);
					}

					listRooms.redraw();
				} else {
					textMessages.setText(message.split("-")[1]);
				}
			});
		});
	}

	/**
	 * Create contents of the shell.
	 */
	protected void createContents() {
		setText("Talk");
		setSize(584, 570);

	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

}
