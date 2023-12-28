import java.io.*;
import java.net.Socket;

public class Client {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int SERVER_PORT = 55607;

    public static void main(String[] args) {
        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader sbr = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             BufferedWriter sbw = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
             BufferedReader cbr = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.print("닉네임을 입력하세요: ");
            String username = cbr.readLine();
            sbw.write(username);
            sbw.newLine();
            sbw.flush();

            // 서버로부터 받는 메시지
            new Thread(() -> {
                while(true){
                    try {
                        String receivedMsg = sbr.readLine();
                        if(receivedMsg==null) continue;
                        System.out.println(receivedMsg);
                    } catch (IOException e) {
                        System.out.println("서버와 연결할 수 없습니다.");
                        break;
                    }
                }

            }).start();

            // 클라이언트가 보내는 메시지
            while(true){
                String msg = cbr.readLine();
                sbw.write(msg);
                sbw.newLine();
                sbw.flush();
                if(msg.equals("\\exit")){
                    System.out.println("프로그램을 종료합니다.");
                    break;
                }
            }
        } catch (IOException e) {
            System.out.println("서버와의 연결이 끊어졌습니다.");
        }
    }
}
