package com.company;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import java.io.*;
import java.net.Socket;
import java.util.Arrays;
import java.util.Date;

public class Main {
    static {
        String ourKeyStore = "not set yet";
        try {
            //server needs access to private key so that it can encrypt outbound to client
            //   which can check result with public key. Not sure if DDI does client auth as well....
            ourKeyStore = Main.class.getClassLoader().getResource("privateKeyStore.p12").getFile();
            System.setProperty("javax.net.ssl.keyStore", ourKeyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        } catch (Exception e) {
            log("Unable to load keystore %s", ourKeyStore);
            log("TLS communications will fail");
            log(e.getMessage());
            System.exit(-1);
        }
    }


    private static final int PORT = 8025;

    public static void main(String[] args) throws Exception {

        SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
        SSLServerSocket ss = (SSLServerSocket) ssf.createServerSocket(PORT);
        printServerSocketInfo(ss);

        while (true) {
            try (
                    Socket s = ss.accept();
                    BufferedOutputStream out = new BufferedOutputStream(s.getOutputStream());
                    BufferedInputStream in = new BufferedInputStream(s.getInputStream());
            ) {

                byte[] buffer = new byte[8096];
                forward(in, out, buffer);

                log("normal exit");
            } catch (Exception ex) {
                ex.printStackTrace();
            } finally {
                log("==================");
            }
        }
    }

    private static void forward(InputStream in, OutputStream out, byte[] buffer) throws IOException {
        int i = 0;
        while (i >= 0) {
            log("read->");
            i = in.read(buffer);
            log("read bytes " + i);
            out.write(buffer, 0, i);
            out.flush();

        }
        log("done forwarding");
    }


    private static void log(String message, Object... args) {
        String formattedMessage = String.format(message, args);
        System.out.println(String.format("%tT %s", new Date(), formattedMessage));
    }


    private static void printServerSocketInfo(SSLServerSocket s) {
        log("Server socket class: " + s.getClass());
        log("   Socket address = "
                + s.getInetAddress().toString());
        log("   Socket port = "
                + s.getLocalPort());
        log("   Need client authentication = "
                + s.getNeedClientAuth());
        log("   Want client authentication = "
                + s.getWantClientAuth());
        log("   Use client mode = "
                + s.getUseClientMode());
        log("   Enabled protocols = "
                + Arrays.toString(s.getEnabledProtocols()));

    }


}