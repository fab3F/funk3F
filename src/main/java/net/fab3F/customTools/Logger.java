package net.fab3F.customTools;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Logger {

    private PrintStream console;
    private PrintStream file;

    private String mode = "debug" ; //can be DEBUG, NORMAL, MINIMAL

    public void debug(String message){
        if(this.mode.equalsIgnoreCase("debug"))
            System.out.println(getDate() + " [DEBUG] " + message);
    }
    public void log(String message){
        if(this.mode.equalsIgnoreCase("debug") || this.mode.equalsIgnoreCase("normal"))
            System.out.println(getDate() + " [LOG] " + message);
    }
    public void thread(String message){
        if(this.mode.equalsIgnoreCase("debug") || this.mode.equalsIgnoreCase("normal"))
            System.out.println(getDate() + " [THREAD] " + message);
    }
    public void error(String message){
        System.out.println(getDate() + " [ERROR] " + message);
    }


    public Logger(String folder, String mode){
        if(folder == null || folder.isBlank()){
            SyIO.println("[LOGGER-NOT-INITIALIZED] Unknown log folder.");
            System.exit(0);
        }
        File f = new File(folder + SyIO.sep + getDate() + "-bot.log");
        if(cantCreateFile(f))
            return;

        if (mode != null && !mode.isBlank()) {
            String modeLower = mode.toLowerCase();
            switch (modeLower) {
                case "debug", "normal", "minimal" -> mode = modeLower;
                default -> {
                    mode = "debug";
                    SyIO.println("[LOGGER-NOT-INITIALIZED] Unknown log mode. Falling back to \"debug\".");
                }
            }
        } else {
            mode = "debug";
            SyIO.println("[LOGGER-NOT-INITIALIZED] Unknown log mode. Falling back to \"debug\".");
        }
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

    private static String getDate(){
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    }

}
