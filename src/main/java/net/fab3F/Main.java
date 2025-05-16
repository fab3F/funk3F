package net.fab3F;

import net.fab3F.bot.Bot;
import net.fab3F.customTools.ConfigWorker;
import net.fab3F.customTools.Logger;
import net.fab3F.customTools.SyIO;

public class Main {

    public static Main main;

    private static final String configPath = "config";
    private final Logger logger;

    public ConfigWorker configW;
    public ConfigWorker.BotConfig botConfig;
    public Bot bot;


    public static void main(String[] args) {
        main = new Main();
    }

    public Main(){
        this.configW = new ConfigWorker(configPath);
        this.botConfig = this.configW.getBotConfig();
        this.logger = new Logger(this.botConfig.getLogPath(), this.botConfig.getLogMode());
        this.configW.setLogger(this.getLogger());
        this.logger.log("Starting Bot");
        this.bot = new Bot(this.botConfig.getToken());
    }



    public Logger getLogger(){
        return this.logger;
    }


    public void exit(){
        this.logger.close();
        SyIO.getSyIO().closeSys();
    }

    public void reloadBotConfig(){
        this.botConfig = this.configW.getBotConfig();
    }

}
