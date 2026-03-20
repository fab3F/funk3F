package net.fab3F;

import dev.arbjerg.lavalink.client.Helpers;
import net.fab3F.bot.Bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {

    public static final String VERSION = "???";

    private static final String configPath = "config";
    private static Logger logger;
    private static ConfigWorker configW;
    private static ConfigWorker.BotConfig botConfig;
    private static Bot bot;


    public static void main(String[] args) {
        setupMain();
    }

    private static void setupMain(){
        configW = new ConfigWorker(configPath);
        try{
            botConfig = configW.readBotConfig();
        } catch (IllegalArgumentException ex){
            System.err.println("\n=========================================");
            System.err.println("BOT START CANCELED!");
            System.err.println(ex.getMessage());
            System.err.println("=========================================\n");
            System.exit(1);
        }
        // Set logging properties
        System.setProperty("bot.log.path", botConfig.getLogPath());
        System.setProperty("bot.log.level", botConfig.getLogMode().toUpperCase());

        configW.setLogger();

        logger = LoggerFactory.getLogger(Main.class);
        logger.info("Started Main and now Starting Bot");
        try{
            bot = new Bot(botConfig.getToken(), configW);
        }catch(IllegalArgumentException ex){
            System.out.println("[ERROR] Exception: " + ex.getMessage());
        }

    }

    // set region (auch für yt search)



    public static void reloadBotConfig(){
        logger.info("Reloading Bot Config.");
        try{
            botConfig = configW.readBotConfig();
        } catch (IllegalArgumentException ex){
            System.err.println("\n=========================================");
            System.err.println("BOT START CANCELED!");
            System.err.println(ex.getMessage());
            System.err.println("=========================================\n");
            exit();
            System.exit(1);
        }
        bot.reloadActivity();
        // loudness manager set new scale

    }

    public static void restart(){
        logger.info("Restaring (Stopping Bot + Main and then starting Main + Bot)");
        stopMain();
        setupMain();
    }

    public static void exit(){
        logger.info("Stopping All.");
        stopMain();
    }

    private static void stopMain(){
        logger.info("Stopping Main.");
        bot.stop();
        // other things?
    }


    public static String replaceLast(String input, String regex, String replacement) {
        return input.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

}
