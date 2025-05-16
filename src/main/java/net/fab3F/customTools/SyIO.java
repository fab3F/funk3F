package net.fab3F.customTools;

import java.io.File;
import java.util.Scanner;

public class SyIO {
    private static SyIO syIO = null;
    public static SyIO getSyIO(){
        syIO = syIO == null ? new SyIO() : syIO;
        return syIO;
    }
    private final Scanner in;
    public SyIO(){
        in = new Scanner(System.in);
    }
    public String readLine(){return in.nextLine();}
    public void closeSys(){in.close();}

    // static
    public static void print(String s){System.out.print(s);}
    public static void print(int i){System.out.print(i);}
    public static void print(long l){System.out.print(l);}
    public static void println(String s){System.out.println(s);}
    public static void println(int i){System.out.println(i);}
    public static void println(long l){System.out.println(l);}

    public static final String sep = File.separator;

    public static String replaceLast(String input, String regex, String replacement) {
        return input.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

}
