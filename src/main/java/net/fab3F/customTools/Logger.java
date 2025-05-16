package net.fab3F.customTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger{

    private PrintStream console;
    private PrintStream file;

    private static final String s_debug = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + " [DEBUG] ";
    private static final String s_log = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + " [LOG] ";
    private static final String s_thread = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + " [THREAD] ";
    private static final String s_error = new SimpleDateFormat("yyyy.MM.dd-HH:mm:ss").format(new Date()) + " [ERROR] ";


    private String mode = "DEBUG"; //can be DEBUG, NORMAL, MINIMAL

    public void debug(String message){
        if(this.mode.equalsIgnoreCase("debug"))
            System.out.println(s_debug + message);
    }
    public void log(String message){
        if(this.mode.equalsIgnoreCase("debug") || this.mode.equalsIgnoreCase("normal"))
            System.err.println(s_log + message);
    }
    public void thread(String message){
        if(this.mode.equalsIgnoreCase("debug") || this.mode.equalsIgnoreCase("normal"))
            System.out.println(s_thread + message);
    }
    public void error(String message){
        System.out.println(s_error + message);
    }


    public Logger(File f, String mode){
        if(cantCreateFile(f))
            return;
        this.mode = mode;
        this.console = System.out;
        try {
            this.file = new PrintStream(new FileOutputStream(f));
        } catch (FileNotFoundException e) {
            return;
        }

        System.setOut(new PrintStream(new LoggerOutputStream(this.console, this.file)));
        System.setErr(new PrintStream(new LoggerOutputStream(this.console, this.file)));
    }

    public void close(){
        if (this.console != null) {
            this.console.close();
        }
        if (this.file != null) {
            this.file.close();
        }
    }

    private boolean cantCreateFile(File f){
        if(f.getParentFile().exists() && f.exists())
            return false;

        if(!f.getParentFile().exists()){
            if(!f.getParentFile().mkdirs()){
                return true;
            }
        }

        if(!f.exists()){
            try {
                if(f.createNewFile()){
                    return false;
                }
            }catch (IOException ex){
                return true;
            }
        }
        return true;
    }

}
