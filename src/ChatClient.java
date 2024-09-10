import java.io.*;
import java.net.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class ChatClient {
    private BufferedReader in;
    private PrintWriter out;
    private JFrame frame;
    private JTextField textField;
    private JTextArea messageArea;
    private String username;
    private JButton sendButton;
    private JPanel panel;
    private JList<String> userList;  // JList để hiển thị danh sách người dùng
    private DefaultListModel<String> userListModel;
    private JLabel name;// Model để quản lý danh sách người dùng
    public ChatClient() {
        // Thiết lập giao diện người dùng
        frame = new JFrame("Chat Room");
        panel = new JPanel();
        System.out.println(username);
        name = new JLabel("username:");
        textField = new JTextField(40);
        textField.setPreferredSize(new Dimension(700, 40));
        messageArea = new JTextArea(20, 40);
        sendButton = new JButton("Send");
        sendButton.setPreferredSize(new Dimension(150, 40));
        messageArea.setEditable(false);
        panel.add(name, BorderLayout.WEST);
        panel.add(textField, BorderLayout.CENTER);
        panel.add(sendButton, BorderLayout.EAST);
        userListModel = new DefaultListModel<>();
        userList = new JList<>(userListModel);

        // Đặt JList vào một JScrollPane để có thể cuộn danh sách người dùng
        JScrollPane userScrollPane = new JScrollPane(userList);
        userScrollPane.setPreferredSize(new Dimension(150, 0)); // Chiều rộng 150px
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);  // Khu vực hiển thị tin nhắn ở giữa
        frame.getContentPane().add(userScrollPane, BorderLayout.WEST);  // Panel chứa danh sách người dùng bên trái
        frame.getContentPane().add(panel, BorderLayout.SOUTH);
        frame.getContentPane().add(new JScrollPane(messageArea), BorderLayout.CENTER);
        frame.pack();

        // Thoát chương trình khi đóng cửa sổ
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
        // Lắng nghe sự kiện khi người dùng nhập văn bản và nhấn Enter
        textField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                out.println(textField.getText());
                textField.setText("");
            }
        });
    }

    private void run() throws IOException {
        // Kết nối tới server
        Socket socket = new Socket("localhost", 12345);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream(), true);

        // Nhập tên người dùng
        while (true) {
            String serverMessage = in.readLine();
            if (serverMessage.startsWith("SUBMITNAME")) {
                username = JOptionPane.showInputDialog(frame, "Enter your username:", "Username", JOptionPane.PLAIN_MESSAGE);
                System.out.println(username);
                name.setText(username);
                out.println(username);
            } else if (serverMessage.startsWith("USERLIST")) {
                // Khi server gửi danh sách người dùng, cập nhật JList
                String[] users = serverMessage.substring(9).split(",");
                updateUsersList(users);
            } else {
                messageArea.append(serverMessage + "\n");
            }
        }
    }
    private void updateUsersList(String[] users) {
        // Cập nhật danh sách người dùng trong JList
        userListModel.clear();
        for (String user : users) {
            userListModel.addElement(user);
        }
    }
    public static void main(String[] args) throws Exception {
        ChatClient client = new ChatClient();
        client.frame.setVisible(true);
        client.run();
    }
}