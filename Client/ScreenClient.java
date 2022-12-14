package Client;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import java.awt.Font;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;

import Message.Message;
import ServerChat.Services;

import java.awt.event.ActionListener;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.nio.file.Files;
import java.awt.event.ActionEvent;

public class ScreenClient {

	private Socket socket;
	private Services services;
	private Message message;

	private JFrame frame;
	private JTextField textFieldIpServidor;
	private JTextField textFieldPortaServidor;
	private JTextField textFieldNomeUsuario;
	private JButton btnConectarAoChat;
	private JButton btnDesconectarDoChat;
	private JButton btnEnviarMensagem;
	private JButton btnLimparMensagem;
	private JButton btnConectarAoServidor;
	private JButton btnDesconectarDoServidor;
	private JTextArea textAreaChat;
	private JTextArea textAreaEnvio;
	private JList<String> listUsuariosOnline;
	private JButton btnEnviarArquivo;


	private class ListenerSocket implements Runnable {

		private ObjectInputStream input;

		public ListenerSocket(Socket socket) {

			try {

				input = new ObjectInputStream(socket.getInputStream());

			} catch (IOException e) {
				// TODO Auto-generated catch block

				e.printStackTrace();

			}

		}

		public void run() {

			message = new Message();

			try {

				while((message = (Message) input.readObject()) != null){

					Action action = message.getAction();

					if(action.equals(Action.SolicitarConexão)){



					} else {

						if(action.equals(Action.AceitarConexão)){

							liberaChat(message);

						} else {

							if(action.equals(Action.RecusarConexão)){

								JOptionPane.showMessageDialog(frame,"Escolha outro nome para realizar a conexão");

							} else {

								if(action.equals(Action.Desconectar)){



								} else {

									if(action.equals(Action.MensagemAll)){

										escreveMensagemNoChat(message);

									} else {

										if(action.equals(Action.MensagemPrivada)){

											escreveMensagemNoChat(message);

										} else {

											if(action.equals(Action.ArquivoAll)){

												baixarArquivo(message);

											} else {

												if(action.equals(Action.ArquivoPrivado)){

													baixarArquivo(message);

												} else {

													if(action.equals(Action.ListaDeUsuariosOnline)){

														listarUsuariosOnline(message);

													}

												}

											}

										}

									}

								}

							}

						}

					}

				}

			} catch (Exception e) {
				// TODO: handle exception
			}

		}

	}


	/**
	 * Create the application.
	 */
	public ScreenClient() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setBounds(100, 100, 1036, 710);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblIpServidor = new JLabel("IP servidor");
		lblIpServidor.setBounds(630, 11, 86, 14);
		frame.getContentPane().add(lblIpServidor);

		JLabel lblPortaServidor = new JLabel("Porta Servidor");
		lblPortaServidor.setBounds(726, 11, 93, 14);
		frame.getContentPane().add(lblPortaServidor);

		textFieldIpServidor = new JTextField();
		textFieldIpServidor.setText("192.168.56.1");
		textFieldIpServidor.setBounds(630, 36, 86, 20);
		frame.getContentPane().add(textFieldIpServidor);
		textFieldIpServidor.setColumns(10);

		textFieldPortaServidor = new JTextField();
		textFieldPortaServidor.setText("5555");
		textFieldPortaServidor.setBounds(726, 36, 86, 20);
		frame.getContentPane().add(textFieldPortaServidor);
		textFieldPortaServidor.setColumns(10);

		btnConectarAoChat = new JButton("Conectar ao Chat");
		btnConectarAoChat.setEnabled(false);
		btnConectarAoChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				if(textFieldNomeUsuario.getText().length() > 0){

					solicitarConexãoAoChat();
					System.out.println("nome nao vazio entao solicita conexão, nome:" + textFieldNomeUsuario.getText());

				} else {

					JOptionPane.showMessageDialog(frame,"Nome vazio não é Aceito!");

				}

			}

		});
		btnConectarAoChat.setBounds(106, 36, 155, 20);
		frame.getContentPane().add(btnConectarAoChat);

		JLabel lblUsuario = new JLabel("Usuario");
		lblUsuario.setBounds(10, 11, 61, 14);
		frame.getContentPane().add(lblUsuario);

		textFieldNomeUsuario = new JTextField();
		textFieldNomeUsuario.setEnabled(false);
		textFieldNomeUsuario.setBounds(10, 36, 86, 20);
		frame.getContentPane().add(textFieldNomeUsuario);
		textFieldNomeUsuario.setColumns(10);

		listUsuariosOnline = new JList<String>();
		listUsuariosOnline.setBorder(new TitledBorder(null, "Usuarios Online", TitledBorder.LEADING, TitledBorder.ABOVE_TOP, null, null));
		listUsuariosOnline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		listUsuariosOnline.setBounds(884, 67, 126, 585);
		frame.getContentPane().add(listUsuariosOnline);

		textAreaChat = new JTextArea();
		textAreaChat.setEditable(false);
		textAreaChat.setBounds(10, 67, 706, 530);
		frame.getContentPane().add(textAreaChat);

		textAreaEnvio = new JTextArea();
		textAreaEnvio.setEnabled(false);
		textAreaEnvio.setBounds(10, 602, 706, 50);
		frame.getContentPane().add(textAreaEnvio);

		btnEnviarMensagem = new JButton("Enviar");
		btnEnviarMensagem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				enviarMensagemNoChat();

			}
		});
		btnEnviarMensagem.setEnabled(false);
		btnEnviarMensagem.setBounds(726, 603, 148, 23);
		frame.getContentPane().add(btnEnviarMensagem);

		btnLimparMensagem = new JButton("Limpar Mensagem");
		btnLimparMensagem.setEnabled(false);
		btnLimparMensagem.setFont(new Font("Tahoma", Font.PLAIN, 9));
		btnLimparMensagem.setBounds(726, 629, 148, 23);
		frame.getContentPane().add(btnLimparMensagem);

		btnConectarAoServidor = new JButton("Conectar ao Servidor");
		btnConectarAoServidor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				conectarAoServidor(textFieldIpServidor.getText(), Integer.parseInt(textFieldPortaServidor.getText()));

			}
		});
		btnConectarAoServidor.setBounds(829, 11, 181, 23);
		frame.getContentPane().add(btnConectarAoServidor);

		btnDesconectarDoChat = new JButton("Desconectar do chat");
		btnDesconectarDoChat.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				desconectarDoChat();

			}
		});
		btnDesconectarDoChat.setEnabled(false);
		btnDesconectarDoChat.setBounds(271, 35, 153, 23);
		frame.getContentPane().add(btnDesconectarDoChat);

		btnDesconectarDoServidor = new JButton("Desconectar do Servidor");
		btnDesconectarDoServidor.setEnabled(false);
		btnDesconectarDoServidor.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				desconectarDoServidor();

			}
		});
		btnDesconectarDoServidor.setBounds(829, 39, 181, 23);
		frame.getContentPane().add(btnDesconectarDoServidor);

		btnEnviarArquivo = new JButton("Enviar Arquivo");
		btnEnviarArquivo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {

				enviarArquivo();

			}
		});
		btnEnviarArquivo.setEnabled(false);
		btnEnviarArquivo.setBounds(726, 574, 148, 23);
		frame.getContentPane().add(btnEnviarArquivo);
		frame.setVisible(true);

	}

	private void solicitarConexãoAoChat(){

		message = new Message();

		message.setAction(Action.SolicitarConexão);
		message.setRemetente(textFieldNomeUsuario.getText());
		message.setDestinatario("Servidor");
		services.enviarMensagem(message);

	}

	private void conectarAoServidor(String ip, int porta){

		services = new ServiçosCliente();
		socket = services.conectarAoServidor(ip, porta);

		if(socket == null){

			JOptionPane.showMessageDialog(frame,"IP ou PORTA invalida!");

		} else {
			System.out.println("listenersocket usuario iniciado");
			new Thread(new ListenerSocket(socket)).start();
			liberaLogin();

		}

	}

	private void liberaLogin(){

		btnConectarAoServidor.setEnabled(false);
		textFieldIpServidor.setEnabled(false);
		textFieldPortaServidor.setEnabled(false);

		btnDesconectarDoServidor.setEnabled(true);
		btnConectarAoChat.setEnabled(true);
		textFieldNomeUsuario.setEnabled(true);

	}

	private void bloqueiaLogin(){

		btnConectarAoServidor.setEnabled(true);
		textFieldIpServidor.setEnabled(true);
		textFieldPortaServidor.setEnabled(true);

		btnDesconectarDoServidor.setEnabled(false);
		btnConectarAoChat.setEnabled(false);
		textFieldNomeUsuario.setEnabled(false);

	}

	private void liberaChat(Message message){

		btnConectarAoChat.setEnabled(false);
		textFieldNomeUsuario.setEnabled(false);
		btnDesconectarDoServidor.setEnabled(false);

		textAreaEnvio.setEnabled(true);
		btnEnviarArquivo.setEnabled(true);
		btnEnviarMensagem.setEnabled(true);
		btnLimparMensagem.setEnabled(true);
		btnDesconectarDoChat.setEnabled(true);

		textAreaChat.setText(message.getHistoricoDeMensagens());

	}

	private void desconectarDoChat(){

		message = new Message();
		message.setAction(Action.Desconectar);
		message.setDestinatario("Servidor");
		message.setRemetente(textFieldNomeUsuario.getText());
		services.enviarMensagem(message);

		bloquearChat();

	}

	private void bloquearChat(){

		btnConectarAoChat.setEnabled(true);
		textFieldNomeUsuario.setEnabled(true);
		btnDesconectarDoServidor.setEnabled(true);

		textAreaEnvio.setEnabled(false);
		btnEnviarArquivo.setEnabled(false);
		btnEnviarMensagem.setEnabled(false);
		btnLimparMensagem.setEnabled(false);
		btnDesconectarDoChat.setEnabled(false);

	}

	private void listarUsuariosOnline(Message message){

		listUsuariosOnline.removeAll();

		String[] arrayDeUsuariosOnline = message.getListaUsuariosOnlines().toArray(new String[message.getListaUsuariosOnlines().size()]);

		listUsuariosOnline.setListData(arrayDeUsuariosOnline);

	}

	private void enviarMensagemNoChat(){

		if(textAreaEnvio.getText() != ""){

			Message message = new Mensagem();
			System.out.println("Valor selecionado na lista de usuarios online: " + listUsuariosOnline.getSelectedValue());
			if(listUsuariosOnline.getSelectedValue().equals("All") || (listUsuariosOnline.getSelectedValue() == null)){
				System.out.println("Seta mensagem para todos");
				message.setAction(Action.MensagemAll);

			} else {
				System.out.println("seta mensagem para privado");
				message.setAction(Action.MensagemPrivada);

			}

			message.setDestinatario(listUsuariosOnline.getSelectedValue());
			message.setRemetente(textFieldNomeUsuario.getText());
			message.setMensagemDeTexto(textAreaEnvio.getText());

			textAreaEnvio.setText("");

			services.enviarMensagem(message);

		}

	}

	private void escreveMensagemNoChat(Message message){

		if(message.getAction() == Action.MensagemAll){

			if(!message.getRemetente().equals(textFieldNomeUsuario.getText())){

				textAreaChat.append("["+ message.getDataHora() +"] \n" + message.getRemetente() + " disse para todos: " + message.getMensagemDeTexto() + "\n");

			} else {

				textAreaChat.append("["+ message.getDataHora() +"] \n" + "Você disse para todos: " + message.getMensagemDeTexto() + "\n");

			}

		} else {

			if(!mensagem.getRemetente().equals(textFieldNomeUsuario.getText())){

				textAreaChat.append("["+ mensagem.getDataHora() +"] \n" + mensagem.getRemetente() + " disse para você: " + mensagem.getMensagemDeTexto() + "\n");

			} else {

				textAreaChat.append("["+ mensagem.getDataHora() +"] \n"+"você disse ao "+ mensagem.getDestinatario() + " :" + mensagem.getMensagemDeTexto() + "\n");

			}

		}

	}

	private void desconectarDoServidor(){

		try {

			socket.getInputStream().close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {

			socket.getOutputStream().close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {

			socket.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		bloqueiaLogin();

	}

	private void enviarArquivo(){

		try{	
			
			JFileChooser arquivo = new JFileChooser();

			arquivo.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int botaoPressionado = arquivo.showSaveDialog(null);
			if(botaoPressionado == 1){
				System.out.println("ERRO NA SELEção DE ARQUIVO");
				return ;
			}
						
			byte[] copiaDoArquivo = Files.readAllBytes(arquivo.getSelectedFile().toPath());
			String nomeArquivo = arquivo.getSelectedFile().getName();
			
			if(listUsuariosOnline.getSelectedValue().equals("All") || (listUsuariosOnline.getSelectedValue() == null)){

				enviarArquivoParaTodos(copiaDoArquivo, nomeArquivo);

			} else {

				enviarArquivoPrivado(copiaDoArquivo, nomeArquivo);

			}

		} catch (IOException e) {



		}

	}

	private void enviarArquivoPrivado(byte[] arquivo, String nomeArquivo){

		Message message = new Message();
		
		message.setAction(Action.ArquivoPrivado);
		message.setArquivo(arquivo);
		message.setNomeArquivo(nomeArquivo);
		message.setRemetente(textFieldNomeUsuario.getText());
		message.setDestinatario(listUsuariosOnline.getSelectedValue());

		services.enviarMensagem(message);

	}

	private void enviarArquivoParaTodos(byte[] arquivo, String nomeArquivo){

		Message message = new Mensagem();

		message.setAction(Action.ArquivoAll);
		message.setArquivo(arquivo);
		message.setNomeArquivo(nomeArquivo);
		message.setRemetente(textFieldNomeUsuario.getText());
		message.setDestinatario(listUsuariosOnline.getSelectedValue());

		services.enviarMensagem(message);

	}

	private void baixarArquivo(Message message){

		if(JOptionPane.showConfirmDialog(frame, message.getRemetente() + " quer te enviar um arquivo chamado: "+ mensagem.getNomeArquivo(), "Transferencia de arquivo", JOptionPane.YES_NO_OPTION) == 0){

			try{

				JFileChooser arquivo = new JFileChooser();

				arquivo.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
				int botaoPressionado = arquivo.showSaveDialog(null);
				if(botaoPressionado == 1){
					return ;
				}
				
				
				
				FileOutputStream gravarArquivo = new FileOutputStream(arquivo.getSelectedFile().toString()+"\\"+mensagem.getNomeArquivo());
				System.out.println(arquivo.getSelectedFile().toString()+"\\"+mensagem.getNomeArquivo());
				gravarArquivo.write(mensagem.getArquivo());
				gravarArquivo.close();

			} catch (IOException e) {



			}
		}

	}

}
