package fr.dinnerwolph.otl.base;

import fr.dinnerwolph.otl.servers.OTLServer;

import java.io.IOException;

/**
 * @author Dinnerwolph
 */

public class Main {

    private static Base base;

    public static void main(String[] args) {
        if (args.length == 0)
            base = new Base();
        else if(args[0].equals("proxy")) {
            try {
                new OTLServer();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static Base getBase() {
        return base;
    }
}
