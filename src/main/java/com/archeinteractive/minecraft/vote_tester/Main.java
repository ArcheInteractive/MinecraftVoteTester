package com.archeinteractive.minecraft.vote_tester;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import com.archeinteractive.minecraft.vote_tester.protocol.HandshakeHandler;

import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Pattern;

public class Main extends Application {

    public static AttributeKey<Session> SESSION_KEY = AttributeKey.valueOf("session_key");

    public static final Pattern MINECRAFT_USERNAME = Pattern.compile("[a-zA-Z0-9_]{1,16}");
    public static final Pattern IPV4 = Pattern.compile("\\b((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)(\\.|$)){4}\\b");

    private static NioEventLoopGroup eventLoopGroup;

    public TextField userNameField = new TextField("Favorlock");

    public TextField hostNameField = new TextField("localhost");

    public TextField portField = new TextField("8192");

    public TextField serviceNameField = new TextField("vote.list.site");

    public TextField tokenField = new TextField();

    public static void main(String[] args) {
        Logger logger = Logger.getLogger("VoteTester");
        FileHandler fh;

        try {
            fh = new FileHandler("./application.log");
            logger.addHandler(fh);
            SimpleFormatter formatter = new SimpleFormatter();
            fh.setFormatter(formatter);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        logger.info("Starting...");

        try {
            eventLoopGroup = new NioEventLoopGroup(1);
            launch(args);
            eventLoopGroup.shutdownGracefully();
        } catch (Exception ex) {
            logger.log(Level.SEVERE, "An exception was caught: ",ex);
        }
    }

    @Override
    public void start(Stage stage) {
        // Text

        Text userNameText = new Text("Minecraft Username");

        Text hostNameText = new Text("Hostname");

        Text portText = new Text("Port");

        Text serviceNameText = new Text("Service Name");

        Text tokenText = new Text("Token");

        // Button Controls

        Button submitButton = new Button("Submit");

        Button clearButton = new Button("Clear");

        // Set event handlers

        submitButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            try {
                Session session = new Session();
                if (session.validate()) {
                    Bootstrap bootstrap = new Bootstrap()
                            .group(eventLoopGroup)
                            .channel(NioSocketChannel.class)
                            .handler(new ChannelInitializer<SocketChannel>() {
                                @Override
                                protected void initChannel(SocketChannel channel) throws Exception {
                                    channel.attr(SESSION_KEY).set(new Session());
                                    channel.pipeline().addLast("handshakeHandler", new HandshakeHandler());
//                                    channel.pipeline().addLast("resultHandler", new ResultHandler());
                                }
                            });

                    System.out.println("Connecting to Votifier...");
                    ChannelFuture future = bootstrap.connect(session.getHostName(), session.getPort());
                } else {
                    System.out.println("Invalid input detected.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        clearButton.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            userNameField.clear();
            hostNameField.clear();
            portField.clear();
            serviceNameField.clear();
            tokenField.clear();
        });

        // Create grid pane
        GridPane pane = new GridPane();

        // Set pane minimum size
        pane.setMinSize(600, 600);

        // Set pane padding
        pane.setPadding(new Insets(10, 10, 10, 10));

        // Set vertical and horizontal gap
        pane.setVgap(5);
        pane.setHgap(5);

        // Center the pane
        pane.setAlignment(Pos.CENTER);

        // Arrange grid
        pane.add(userNameText, 0, 0);
        pane.add(userNameField, 1, 0);
        pane.add(hostNameText, 0, 1);
        pane.add(hostNameField, 1, 1);
        pane.add(portText, 0, 2);
        pane.add(portField, 1, 2);
        pane.add(serviceNameText, 0, 3);
        pane.add(serviceNameField, 1, 3);
        pane.add(tokenText, 0, 4);
        pane.add(tokenField, 1, 4);
        pane.add(submitButton, 0, 5);
        pane.add(clearButton, 1, 5);

        // Create scene
        Scene scene = new Scene(pane);

        // Set scene fill
        scene.setFill(Color.WHITE);

        // Set stage title
        stage.setTitle("Vote Tester");

        // Set stage scene
        stage.setScene(scene);

        // Show the stage
        stage.show();
    }

    public class Session {

        public String getUserName() {
            return Main.this.userNameField.getText();
        }

        public String getHostName() {
            return Main.this.hostNameField.getText();
        }

        public int getPort() {
            int port = 0;
            try {
                port = Integer.parseInt(Main.this.portField.getText());
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
            return port;
        }

        public String getServiceName() {
            return Main.this.serviceNameField.getText();
        }

        public String getPublicKey() {
            return Main.this.tokenField.getText();
        }

        public boolean validate() {
            if (getUserName() == null || getUserName().isEmpty()
                    || !MINECRAFT_USERNAME.matcher(getUserName()).matches())
                return false;
            if (getHostName() == null || getHostName().isEmpty()
                    || (!getHostName().equalsIgnoreCase("localhost") && !IPV4.matcher(getHostName()).matches()))
                return false;
            int port = getPort();
            if (port <= 0 || port >= 65535)
                return false;
            if (getServiceName() == null || getServiceName().isEmpty())
                return false;
            if (getPublicKey() == null || getPublicKey().isEmpty())
                return false;
            return true;
        }

    }

}
