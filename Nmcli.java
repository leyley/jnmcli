import java.awt.*;
import java.io.*;
import java.util.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.net.URL;

class Nmcli {
    private static Nmcli singleton;
    private JFrame mainWindow;
    private JLabel banner;
    private JComboBox<String> guiConnections;
    private JPanel buttons;
    private JButton editorButton;
    private JButton connectButton;
    private JButton disConnectButton;
    private String errorMsg;

    public String[] getConnections() {
        Process process = null;
        try {
            process = Runtime.getRuntime().exec("nmcli connection");
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        String line = null;
        ArrayList<String> connections = new ArrayList<String>();
        try {
            while ((line = reader.readLine()) != null) {
                // System.out.println(line);
                String[] cArray = line.split(" ");
                if (!cArray[0].isEmpty() && cArray[0].compareTo("NAME") != 0) {
                    connections.add(cArray[0]);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        String[] ret = connections.toArray(new String[0]);
        return ret;
    }

    public boolean connect(String connection, boolean disConnect) {
        if (connection == null) {
            return false;
        }
        Process process = null;
        try {
            String opCode = "up";
            if (disConnect) {
                opCode = "down";
            }
            process = Runtime.getRuntime().exec("nmcli -t -m multiline connection " + opCode + " " + connection);
            process.waitFor();
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (0 == process.exitValue()) {
            return true;
        }
        BufferedReader reader = new BufferedReader(new InputStreamReader(process.getErrorStream()));
        String line = null;
        System.out.println("Fail to connect to " + connection);
        try {
            this.errorMsg = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                this.errorMsg += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void initGUI() {
        this.mainWindow = new JFrame("网络连接");
        this.mainWindow.setLayout(new GridBagLayout());
        this.mainWindow.addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent windowEvent) {
                System.exit(0);
            }
        });
        this.guiConnections = new JComboBox<String>(this.getConnections());

        this.editorButton = new JButton("编辑连接");
        this.editorButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                Process process = null;
                try {
                    process = Runtime.getRuntime().exec("nm-connection-editor");
                    process.waitFor();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                guiConnections.setModel(new DefaultComboBoxModel<String>(getConnections()));
            }
        });

        this.connectButton = new JButton("连接所选");
        this.connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String connection = guiConnections.getSelectedItem().toString();
                connectButton.setText("正在连接 " + connection + "......");
                mainWindow.validate();
                mainWindow.repaint();
                boolean isConnect = connect(connection, false);
                if (!isConnect) {
                    String errorStr = "连接失败 " + connection;
                    errorStr += "\r\n";
                    errorStr += errorMsg;
                    JOptionPane.showMessageDialog(null, errorStr);
                } else {
                    System.exit(0);
                }
                connectButton.setText("连接所选");
            }
        });
        this.disConnectButton = new JButton("断开所选");
        this.disConnectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String connection = guiConnections.getSelectedItem().toString();
                disConnectButton.setText("正在断开 " + connection + "......");
                mainWindow.validate();
                mainWindow.repaint();
                boolean isConnect = connect(connection, true);
                if (!isConnect) {
                    String errorStr = "Fail to connect " + connection;
                    errorStr += "\r\n";
                    errorStr += errorMsg;
                    JOptionPane.showMessageDialog(null, errorStr);
                } else {
                    System.exit(0);
                }
                disConnectButton.setText("断开所选");
            }
        });

        GridBagConstraints c = new GridBagConstraints();
        c.fill = GridBagConstraints.VERTICAL;
        c.anchor = GridBagConstraints.PAGE_END;
        c.weightx = 1;
        c.gridwidth = 3;
        c.insets = new Insets(0, 5, 5, 5);
        // Banner;
        this.banner = new JLabel();
        this.banner.setIcon(new ImageIcon("banner.png"));
        this.mainWindow.add(this.banner, c);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = 0;
        c.gridy = 1;
        c.gridwidth = 3;
        c.insets = new Insets(0, 5, 0, 5);
        this.mainWindow.add(this.guiConnections, c);
        this.buttons = new JPanel(new FlowLayout(FlowLayout.TRAILING));
        this.buttons.add(this.editorButton);
        this.buttons.add(this.connectButton);
        this.buttons.add(this.disConnectButton);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.gridx = GridBagConstraints.RELATIVE;
        c.gridy = 2;
        c.weightx = 3;
        // c.weighty = 1;
        c.gridwidth = 1;
        c.ipady = 0;
        c.insets = new Insets(0, 0, 0, 0);
        this.mainWindow.add(this.buttons, c);
        this.mainWindow.setSize(400, 250);
        this.mainWindow.setLocationRelativeTo(null);
        this.mainWindow.setResizable(false);
    }

    public void show() {
        this.mainWindow.setVisible(true);
    }

    public static Nmcli getInstance() {
        if (Nmcli.singleton == null) {
            Nmcli.singleton = new Nmcli();
        }
        return Nmcli.singleton;
    }

    public static void main(String args[]) {
        Color progressColor = new Color(0x5b, 0x7a, 0x84);
        SplashScreen splash = SplashScreen.getSplashScreen();
        try {
            if (splash == null) {
                System.out.println("No splash image...");
            } else {
                Graphics2D g2 = splash.createGraphics();
                Rectangle bounds = splash.getBounds();
                int progressY = (int) bounds.getHeight() - 24;
                int progressW = (int) bounds.getWidth() - 8;
                g2.setColor(Color.BLACK);
                g2.fillRect(3, progressY - 1, progressW + 2, 22);
                g2.setColor(Color.WHITE);
                g2.fillRect(4, progressY, progressW, 20);

                g2.setColor(Color.WHITE);
                g2.drawString("初始化对象......", 4, progressY - 12);
                g2.setColor(progressColor);
                int curW = (int) (progressW * 0.3);
                g2.fillRect(4, progressY, curW, 20);
                Thread.sleep(90);
                splash.update();
                Nmcli gui = Nmcli.getInstance();
                g2.setColor(Color.WHITE);
                g2.drawString("初始化窗口......", 4, progressY - 12);
                g2.setColor(progressColor);
                curW = (int) (progressW * 0.6);
                g2.fillRect(4, progressY, curW, 20);
                Thread.sleep(90);
                splash.update();
                gui.initGUI();
                g2.setColor(progressColor);
                g2.fillRect(4, progressY, progressW, 20);
                gui.show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
