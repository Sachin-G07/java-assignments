/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mycompany.mclient;

/**
 *
 * @author sachin
 */
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.List;
import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NetworkClient extends Thread {
    Socket s1 = null;
    String line = null;
    BufferedReader br = null;
    BufferedReader in = null;
    PrintWriter os = null;
    static JTextArea conversationView = new JTextArea();
    JTextField messageField = new JTextField();
    JButton sendMessageBtn = new JButton("Send");
    JButton closeBtn = new JButton ("End chat session and logout");
    private String username;
    private boolean isConnectionCreated;
    public static final String NL = System.getProperty("line.separator");

    public NetworkClient(String username) {
        this.username = username;
    }
    public void boardMaker(){
        String BOARD_MAKER= "select * from messages ";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:record.db");
        PreparedStatement preparedStatement = conn.prepareStatement(BOARD_MAKER)) {
                ResultSet resultSet= preparedStatement.executeQuery();
                ArrayList<Integer> ids = new ArrayList<Integer>(1000);
                ArrayList<String> users = new ArrayList<String>(1000);
                ArrayList<String> messages = new ArrayList<String>(1000);
                while(resultSet.next()){
                    ids.add(resultSet.getInt("id"));
                    users.add(resultSet.getString("username"));
                    messages.add(resultSet.getString("message"));
                }
                for (int i = 0; i < ids.size(); i++) {
                    if(ids.get(i)>ids.size()-5){
                        conversationView.append(users.get(i)+":"+NL);
                        conversationView.append(messages.get(i)+NL);
                        conversationView.append("___________________________"+NL);                        
                    }
                }
                
        } catch (SQLException ex) {
                System.out.print(ex.getMessage());
        } catch (Exception ex) {
                ex.printStackTrace();
        }
    }
    @Override
    public void run() {
        JFrame userFrame = new JFrame();
        userFrame.setLayout(null);
        userFrame.setSize(500, 500);

        conversationView.setEditable(false);
        conversationView.setBounds(0, 0, 500, 350);
        userFrame.add(conversationView);
        
        messageField.setBounds(0, 355, 350, 65);
        userFrame.add(messageField);

        sendMessageBtn.setBounds(355, 355, 145, 65);
        userFrame.add(sendMessageBtn);
        
        closeBtn.setBounds(0, 420, 520, 40);
        userFrame.add(closeBtn);
        
        
        Runnable boardRunnable = new Runnable(){
            public void run(){
                conversationView.setText("");
                boardMaker();
            }
        };
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        executor.scheduleAtFixedRate(boardRunnable, 0, 3, TimeUnit.SECONDS);

        closeBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String SQL_SETTER="update users_table set connected=false where username= '"+username+"'";
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:record.db");
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_SETTER)) {
                preparedStatement.executeQuery();
                } catch (SQLException ex) {
                System.out.print(ex.getMessage());
                } catch (Exception ex) {
                ex.printStackTrace();
                }
                userFrame.dispose();
            }
        });
        
        sendMessageBtn.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                System.out.println(messageField.getText());
                InetAddress address = null;
                try {
                    address = InetAddress.getLocalHost();
                } catch (UnknownHostException ex) {

                }
                if (!isConnectionCreated) {
                    try {
                        s1 = new Socket(address, 5000); // You can use static final constant PORT_NUM
                        in = new BufferedReader(new InputStreamReader(s1.getInputStream()));
                        os = new PrintWriter(s1.getOutputStream());
                        isConnectionCreated = true;
                    } catch (IOException e) {
                        e.printStackTrace();
                        System.err.print("IO Exception");
                    }

                    System.out.println("Client Address : " + address);
                    System.out.println("Enter Data to echo Server ( Enter QUIT to end):");
                }

                String response = null;
                line = messageField.getText();
                
                String SQL_MESSAGE="insert into messages(username,message) values(?,?)";
                try (Connection conn = DriverManager.getConnection("jdbc:sqlite:record.db");
                PreparedStatement preparedStatement = conn.prepareStatement(SQL_MESSAGE)) {
                    preparedStatement.setString(2, messageField.getText());
                    preparedStatement.setString(1, username);
                    preparedStatement.executeQuery();
                } catch (SQLException ex) {
                System.out.print(ex.getMessage());
                } catch (Exception ex) {
                ex.printStackTrace();
                }
                conversationView.setText("");
                boardMaker();
            }
        });

        userFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
        userFrame.setVisible(true);
       
    }
}
