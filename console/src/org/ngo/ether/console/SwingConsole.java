/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 */
package org.ngo.ether.console;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;


import org.ngo.ether.endpoint.EndpointCallback;
import org.ngo.ether.endpoint.EndpointHandler;
import org.ngo.ether.endpoint.EndpointSupport;

/**
 * Simple chat client based on Swing &amp; MINA that implements the chat protocol.
 *
 * @author <a href="http://mina.apache.org">Apache MINA Project</a>
 */
public class SwingConsole extends JFrame implements EndpointCallback {
    private static final long serialVersionUID = 1538675161745436968L;

    private JTextField inputText;

    private JButton loginButton;

    private JButton quitButton;

    private JButton closeButton;

    private JTextField serverField;

    private JTextField portField;

    private JTextArea area;

    private JScrollBar scroll;

    private EndpointSupport client;

    private EndpointHandler handler;

    public SwingConsole() {
        super("Console Client based on Apache MINA");
        loginButton = new JButton(new ConnectAction());
        loginButton.setText("Connect");
        quitButton = new JButton(new DisconnectAction());
        quitButton.setText("Disconnect");
        closeButton = new JButton(new QuitAction());
        closeButton.setText("Quit");
        inputText = new JTextField(30);
        inputText.setAction(new SendMessageAction());
        area = new JTextArea(10, 50);
        area.setLineWrap(true);
        area.setEditable(false);
        scroll = new JScrollBar();
        scroll.add(area);
        portField = new JTextField(10);
        portField.setEditable(true);
        serverField = new JTextField(10);
        serverField.setEditable(false);

        JPanel h = new JPanel();
        h.setLayout(new BoxLayout(h, BoxLayout.LINE_AXIS));
        h.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        JLabel portLabel = new JLabel("Destination Port: ");
        JLabel serverLabel = new JLabel("Server: ");
        h.add(portLabel);
        h.add(Box.createRigidArea(new Dimension(10, 0)));
        h.add(portField);
        h.add(Box.createRigidArea(new Dimension(10, 0)));
        h.add(Box.createHorizontalGlue());
        h.add(Box.createRigidArea(new Dimension(10, 0)));
        h.add(serverLabel);
        h.add(Box.createRigidArea(new Dimension(10, 0)));
        h.add(serverField);

        JPanel p = new JPanel();
        p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
        p.setBorder(new EmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel();
        left.setLayout(new BoxLayout(left, BoxLayout.PAGE_AXIS));
        left.add(area);
        left.add(Box.createRigidArea(new Dimension(0, 5)));
        left.add(Box.createHorizontalGlue());
        left.add(inputText);

        JPanel right = new JPanel();
        right.setLayout(new BoxLayout(right, BoxLayout.PAGE_AXIS));
        right.add(loginButton);
        right.add(Box.createRigidArea(new Dimension(0, 5)));
        right.add(quitButton);
        right.add(Box.createHorizontalGlue());
        right.add(Box.createRigidArea(new Dimension(0, 25)));
        right.add(closeButton);

        p.add(left);
        p.add(Box.createRigidArea(new Dimension(10, 0)));
        p.add(right);

        getContentPane().add(h, BorderLayout.NORTH);
        getContentPane().add(p);

        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            	if (client!=null)
            		client.quit();
                dispose();
            }
        });

        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public class ConnectAction extends AbstractAction {
        private static final long serialVersionUID = 3596719854773863244L;

        public void actionPerformed(ActionEvent e) {

            ConnectDialog dialog = new ConnectDialog(SwingConsole.this);
            dialog.pack();
            dialog.setVisible(true);

            if (dialog.isCancelled()) {
                return;
            }

            SocketAddress address = parseSocketAddress(dialog.getServerAddress());
            int port = parsePort(dialog.getPort());

    		handler= new EndpointHandler(SwingConsole.this, (short)port);
    		client = new EndpointSupport("Console", handler);
    	                
    		serverField.setText(dialog.getServerAddress() + "/"+ dialog.getPort());
            
            if (!client.connect(address, dialog.isUseSsl())) {
                JOptionPane.showMessageDialog(SwingConsole.this,
                        "Could not connect to " + dialog.getServerAddress()
                                + ". ");
            } 
        }
    }

    private class SendMessageAction extends AbstractAction {
        private static final long serialVersionUID = 1655297424639924560L;

        public void actionPerformed(ActionEvent e) {
            try {
                client.sendMessage(inputText.getText(), parsePort(portField.getText()));
                inputText.setText("");
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(SwingConsole.this,
                        "message could not be sent.");
            }
        }
    }
    

    private class DisconnectAction extends AbstractAction {
        private static final long serialVersionUID = 1655297424639924560L;

        public void actionPerformed(ActionEvent e) {
            try {
                client.quit();
            } catch (Exception e1) {
                JOptionPane.showMessageDialog(SwingConsole.this, "Session could not be closed.");
            }
        }
    }
    

    private class QuitAction extends AbstractAction {
        private static final long serialVersionUID = -6389802816912005370L;

        public void actionPerformed(ActionEvent e) {
            if (client != null) {
                 client.quit();
            }
            SwingConsole.this.dispose();
        }
    }


    private void append(String text) {
        area.append(text);
    }

    private void notifyError(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    private SocketAddress parseSocketAddress(String s) {
        s = s.trim();
        int colonIndex = s.indexOf(":");
        if (colonIndex > 0) {
            String host = s.substring(0, colonIndex);
            int port = parsePort(s.substring(colonIndex + 1));
            return new InetSocketAddress(host, port);
        } else {
            int port = parsePort(s.substring(colonIndex + 1));
            return new InetSocketAddress(port);
        }
    }

    private int parsePort(String s) {
        try {
            return Integer.parseInt(s);
        } catch (NumberFormatException nfe) {
            throw new IllegalArgumentException("Illegal port number: " + s);
        }
    }
    
    private void setconnected() {
    	inputText.setEnabled(true);
    	portField.setEnabled(true);;
        quitButton.setEnabled(true);
        loginButton.setEnabled(false);
    }

    public void connected() {
    	area.setText("");
        append("You have Connected to the bridge.\n");
        setconnected();
    }

    public void disconnected() {
        append("Connection closed.\n");
        inputText.setEnabled(false);
        quitButton.setEnabled(false);
        loginButton.setEnabled(true);
    }

    public void error(String message) {
        notifyError(message + "\n");
    }


    public void messageReceived(String message) {
        append(message + "\n");
    }

    public static void main(String[] args) {
        SwingConsole client = new SwingConsole();
        client.pack();
        client.setVisible(true);
    }
}
