package fr.dinnerwolph.otl.base;

/**
 * @author Dinnerwolph
 */

public class Main {

    private static Base base;

    public static void main(String[] args) {
        base = new Base();
    }

    public static Base getBase() {
        return base;
    }
}
