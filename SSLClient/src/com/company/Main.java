package com.company;

import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Main {

    private static final String HOST = "localhost";

    private static final int PORT = 8025;

    public static void main(String[] args) throws Exception {
        SSLSocketFactory sf = (SSLSocketFactory) SSLSocketFactory.getDefault();
        try (
                SSLSocket s = (SSLSocket) sf.createSocket(HOST, PORT);
                BufferedInputStream in = new BufferedInputStream(s.getInputStream());
                OutputStream out = s.getOutputStream();
        )
        {
            startReading(in);

            out.write("Connection established.\n".getBytes());
            out.flush();

            System.out.println("reading input. send with new line\n");
            int theCharacter = 0;
            theCharacter = System.in.read();
            while (theCharacter != '~') // The '~' is an escape character to exit
            {
                out.write(theCharacter);
                theCharacter = System.in.read();
                if ('\n' == theCharacter) out.flush();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            System.out.println("exiting");
        }
    }

    private static void startReading(BufferedInputStream in) throws IOException {
        Runnable reader = () -> readResponse(in);
        Thread readerthread = new Thread(reader);
        readerthread.setDaemon(true);
        readerthread.start();
    }

    private static void readResponse(InputStream in) {
        int r;
        System.out.println("starting reader");
        try {
            do {
                r = in.read();
                System.err.print((char) r);
                //  else if (r >= 0) System.err.print("'" + (char) r + "'");
            } while (r >= 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("done waiting");
    }


}