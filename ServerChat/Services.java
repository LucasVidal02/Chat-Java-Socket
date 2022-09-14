package ServerChat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import Message.Message;
import Message.Message.Action;

public class Services {

	private ServerSocket serverSocket;
	private Socket socket;
	private Map<String, ObjectOutputStream> mapOnlines = new HashMap<String, ObjectOutputStream>();
	private String historicoDeMensagens = "";

	public Services(int porta){

		//iniciar o servidor
		iniciarServidor(porta);

	}

	private void iniciarServidor(int porta){

		try {

			serverSocket = new ServerSocket(porta);
			System.out.println("testando");

			while(true){

				System.out.println("Loop no aguardando no serverSocket.accept() porta: " + porta);

				socket = serverSocket.accept();

				System.out.println("Novo socket aceito!");

				new Thread(new ListenerSocket(socket)).start();
				System.out.println("Nova thread criada para ouvir o novo socket");

			}

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	class ListenerSocket implements Runnable{

		private ObjectOutputStream output;
		private ObjectInputStream input;

		public ListenerSocket(Socket socket) {
			// TODO Auto-generated constructor stub

			try {

				output = new ObjectOutputStream(socket.getOutputStream());
				input = new ObjectInputStream(socket.getInputStream());

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	

		}

		@Override
		public void run() {
			// TODO Auto-generated method stub

			Message message;
			try {

				while((message = (Message) input.readObject()) != null){
					System.out.println("\n \n Loops thread servidor: ação: " + message.getAction());
					System.out.println("Destino: "+ message.getDestinatario());
					System.out.println("Remetente: " + message.getRemetente());
					javax.swing.Action action = message.getAction();

					if(action.equals(Action.SolicitarConexão)){
						System.out.println("solicitar conexão servidor");
						solicitarConexão(message);

					} else {

						if(action.equals(Action.AceitarConexão)){



						} else {

							if(action.equals(Action.RecusarConexão)){



							} else {

								if(action.equals(Action.Desconectar)){

									removeUsuarioDaLista(message);
									System.out.println("removeu o usuario: "+ message.getRemetente() + " do chat");

								} else {

									if(action.equals(Action.MensagemAll)){

										enviarMensagemNoChatParaTodos(message);

									} else {

										if(action.equals(Action.MensagemPrivada)){

											enviarMensagemNoChatPrivado(message);

										} else {

											if(action.equals(Action.ArquivoAll)){

												enviarArquivoParaTodos(message);

											} else {

												if(action.equals(Action.ArquivoPrivado)){

													enviarArquivoPrivado(message);

												} else {

													if(action.equals(Action.ListaDeUsuariosOnline)){



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

		private void solicitarConexão(Message message){

			if(mapOnlines.size() == 0){

				aceitarUsuarioNoServidor(message);

			} else {

				if(mapOnlines.containsKey(message.getRemetente())){

					recusarUsuarioNoServidor(message);

				} else {

					aceitarUsuarioNoServidor(message);

				}

			}

		}

		private void aceitarUsuarioNoServidor(Message message){
			
			message.setHistoricoDeMensagens(carregarHistorico());
			
			adicionaUsuarioNaLista(message);
			message.setAction(Action.AceitarConexão);
			message.setDestinatario(message.getRemetente());
			message.setRemetente("Servidor");
			enviaMensagem(message, output);

			enviarListaDeUsuarios();

		}

		private void recusarUsuarioNoServidor(Message message){

			message.setAction(Action.RecusarConexão);
			message.setDestinatario(message.getRemetente());
			message.setRemetente("Servidor");
			enviaMensagem(message, output);

		}

		private void adicionaUsuarioNaLista(Message message){
			
			System.out.println("Adicionando novo usuario na lista, nome do usuario: " + message.getRemetente());
			mapOnlines.put(message.getRemetente(), output);

		}

		private void removeUsuarioDaLista(Message message){
			
			System.out.println("removendo o usuario "+ message.getRemetente() + " da lista de usuarios online");
			mapOnlines.remove(message.getRemetente(), output);
			enviarListaDeUsuarios();

		}

		private void enviaMensagem(Message message, ObjectOutputStream output){

			try {

				output.writeObject(message);

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

		private void enviarListaDeUsuarios(){

			Message mensagem = new Message();
			mensagem.setAction(Action.ListaDeUsuariosOnline);
			mensagem.setRemetente("Servidor");
			mensagem.setDestinatario("All");
			mensagem.setListaUsuariosOnlines(criaListaUsuarios());

			for(Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()){

				enviaMensagem(mensagem, kv.getValue());

			}

		}

		private Set<String> criaListaUsuarios(){

			Set<String> listaUsuarios = new HashSet<String>();
			listaUsuarios.add("All");
			for(Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()){
				System.out.println(kv.getKey().toString());
				listaUsuarios.add(kv.getKey().toString());

			}


			return listaUsuarios;

		}

		private void enviarMensagemNoChatPrivado(Message message){

			if(mapOnlines.containsKey(message.getDestinatario())){
				
				message.setDataHora(LocalDateTime.now().toString());
				
				enviaMensagem(message, mapOnlines.get(message.getDestinatario()));
				enviaMensagem(message, mapOnlines.get(message.getRemetente()));				

			}

		}
		
		private void enviarMensagemNoChatParaTodos(Message message){
			
			message.setDataHora(LocalDateTime.now().toString());
			
			salvarHistorico(message);
			
			for(Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()){
				System.out.println("q ta acontecendo " + kv.getKey() + ", " + message.getAction());
				enviaMensagem(message, kv.getValue());

			}
			
		}
		
		private void salvarHistorico(Message message){		
				System.out.println("Salvou no historico");
				historicoDeMensagens = historicoDeMensagens.concat("["+ message.getDataHora() +"] \n"+message.getRemetente() + " disse para todos: " + message.getMensagemDeTexto() + "\n");			
			
		}
		
		private String carregarHistorico(){
			
			return historicoDeMensagens;
			
		}
		
		private void enviarArquivoPrivado(Message message){
			
			if(mapOnlines.containsKey(message.getDestinatario())){
				
				enviaMensagem(message, mapOnlines.get(message.getDestinatario()));
				
			}
			
		}
		
		private void enviarArquivoParaTodos(Message message){
			
			for(Map.Entry<String, ObjectOutputStream> kv : mapOnlines.entrySet()){
				System.out.println("q ta acontecendo arquivos " + kv.getKey() + ", " + message.getAction());
				enviaMensagem(message, kv.getValue());

			}
			
		}

	}


	public ServerSocket getServerSocket() {
		return serverSocket;
	}
	public void setServerSocket(ServerSocket serverSocket) {
		this.serverSocket = serverSocket;
	}
	public Socket getSocket() {
		return socket;
	}
	public void setSocket(Socket socket) {
		this.socket = socket;
	}
	public Map<String, ObjectOutputStream> getMapOnlines() {
		return mapOnlines;
	}
	public void setMapOnlines(Map<String, ObjectOutputStream> mapOnlines) {
		this.mapOnlines = mapOnlines;
	}

}