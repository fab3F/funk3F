package net.fab3F;

import net.fab3F.customTools.Logger;

import java.io.File;

public class Main {

    public static Main main;

    private Logger logger;


    public static void main(String[] args) {
        System.out.println("Hello World!");
    }





    public Logger getLogger(){
        if(logger == null){
            logger = new Logger(new File(""), "");
        }
        return logger;
    }

    public void exit(){
        // close sys
        // close logger
    }

}
