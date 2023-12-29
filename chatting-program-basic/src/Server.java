import java.util.*;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private static final int SERVER_PORT = 55607;
    private static final int MAX_CLIENT = 5;
    private static volatile List<ClientHandler> clients = new ArrayList<>(MAX_CLIENT);
    private static final String commandKey = "\\";

    public static void main(String[] args) {


        try (ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
        ) {
            System.out.println("******************* Server Start *******************");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println(clientSocket);
                System.out.printf("\t\t새로운 클라이언트 입장 (%d/5)%n", clients.size() + 1);

                // 인원 초과 시,
                if (clients.size() == MAX_CLIENT) {
                    System.out.println("인원이 초과되었습니다.");
                    try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()))) {
                        bw.write("인원이 초과되었습니다.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        try {
                            clientSocket.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    continue;
                }

                ClientHandler c = new ClientHandler(clientSocket);
                clients.add(c);
                new Thread(c).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static class ClientHandler implements Runnable {
        private Socket clientSocket;
        private BufferedWriter bw;
        private BufferedReader br;
        private String username;

        public ClientHandler(Socket clientSocket) {
            try {
                this.clientSocket = clientSocket;
                this.br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                this.bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {

            try {
                username = br.readLine();
                System.out.printf("\t\t%s 님이 입장하셨습니다.%n", username);
                broadcast(String.format("%s 님이 입장하셨습니다. (%d/5)", username, clients.size()));

                while (true) {
                    String msg = br.readLine();
                    if(msg==null) continue;

                    if (msg.startsWith(commandKey)) {
                        if (msg.substring(1).equals("exit")) {

                            break;
                        }
                    }
                    broadcast(String.format("* %s: %s", username, msg));
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally{
                try {
                    System.out.printf("\t\t%s 님이 나갔습니다.", username);
                    synchronized (this){
                        clientSocket.close();
                        br.close();
                        bw.close();
                        clients.remove(this);
                    }
                    broadcast(String.format("* %s 님이 채팅에서 나갔습니다.", username));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public BufferedWriter getBw() {
            return this.bw;
        }

        private void broadcast(String message) throws IOException {
            for (ClientHandler client : clients) {
                client.getBw().write(message);
                client.getBw().newLine();
                client.getBw().flush();
            }
        }
    }
}
