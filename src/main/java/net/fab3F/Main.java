package net.fab3F;

import net.fab3F.bot.Bot;
import net.fab3F.customTools.ConfigWorker;
import net.fab3F.customTools.Logger;
import net.fab3F.customTools.SyIO;

public class Main {

    private static final String configPath = "config";
    public static Logger logger;

    public static ConfigWorker configW;
    public static ConfigWorker.BotConfig botConfig;
    public static Bot bot;


    public static void main(String[] args) {
        setupMain();
    }

    private static void setupMain(){
        configW = new ConfigWorker(configPath);
        botConfig = configW.getBotConfig();
        logger = new Logger(botConfig.getLogPath(), botConfig.getLogMode());
        configW.setLogger(logger);
        logger.log("Started Main and now Starting Bot");
        bot = new Bot(botConfig.getToken(), logger);
    }

    // set region (auch für yt search)



    public static void reloadBotConfig(){
        logger.log("Reloading Bot Config.");
        botConfig = configW.getBotConfig();
        bot.reloadActivity();
        // loudness manager set new scale

    }

    public static void restart(){
        logger.log("Restaring (Stopping Bot + Main and then starting Main + Bot)");
        stopMain();
        setupMain();
    }

    public static void exit(){
        logger.log("Stopping All.");
        stopMain();
        SyIO.getSyIO().closeSys();
    }

    private static void stopMain(){
        logger.log("Stopping Main.");
        logger.close();
        bot.stop();
        // other things?
    }

}
