package com.company;

import javax.net.ssl.*;
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
            ourKeyStore = Main.class.getClassLoader().getResource("ddi.p12").getFile();
            System.setProperty("javax.net.ssl.keyStore", ourKeyStore);
            System.setProperty("javax.net.ssl.keyStorePassword", "changeit");
        } catch (Exception e) {
            log("Unable to load keystore %s", ourKeyStore);
            log("TSL communications will fail");
            log(e.getMessage());
            //TODO: this is fatal...
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
                    BufferedReader inbr = new BufferedReader(new InputStreamReader(s.getInputStream()));
                    BufferedInputStream in = new BufferedInputStream(s.getInputStream());
            ) {

                //               echoLine(out, inbr);


                //   try { Thread.sleep(1000);} catch(Exception ex) {}
                byte[] buffer = new byte[8096];
                // forward("c-s", in, out, buffer);
                forward(in, out, buffer);

//                try { Thread.sleep(1000);} catch(Exception ex) {}
//                byte[] buffer = new byte[20000];
//
//                while (true) {
//                    System.out.print(in.available());
//                    try { Thread.sleep(1000);} catch(Exception ex) {}
//                    forward("client->Server", in, out, buffer);
//                }
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


    private static int forward(String sourceName, InputStream inFromSource, OutputStream outToDest, byte[] buffer) {
        int totalForwardedCount = 0;
        int availAtSourceCount = 0;
        try {
            while (true) {
                availAtSourceCount = inFromSource.available();
                // log("something to read: "+ availAtSourceCount);
                Arrays.fill(buffer, (byte) 0);
                int bytesReadCount = 0;
                try {
                    bytesReadCount = inFromSource.read(buffer, 0, Math.min(buffer.length, availAtSourceCount));
                } catch (IOException e) {
                    throw new RuntimeException("failed to read from " + sourceName, e);
                }
                if (bytesReadCount > 0) {
                    log("%s sent %d bytes [\n%s\n]", sourceName, bytesReadCount, new String(buffer));
                    sendData(outToDest, buffer, bytesReadCount);
                    totalForwardedCount += bytesReadCount;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {

            if (totalForwardedCount > 0)
                log("Finished forwarding %s, forwarded %d", sourceName, totalForwardedCount);
            return totalForwardedCount;
        }
    }

    private static void sendData(OutputStream outToDest, byte[] buffer, int count) throws Exception {
        outToDest.write(buffer, 0, count);
        outToDest.flush();
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