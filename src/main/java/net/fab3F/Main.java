package net.fab3F;

import net.fab3F.bot.Bot;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class Main {

    public static final String VERSION = "???";
    public enum ExitCode {EXIT_OK, EXIT_ERROR, EXIT_RESTART};

    private static final AtomicBoolean shutdownStarted = new AtomicBoolean(false);
    private static final CountDownLatch shutdownDone = new CountDownLatch(1);

    private static final String configPath = "config";
    private static Logger logger;
    private static ConfigWorker configW;
    private static Bot bot;


    public static void main(String[] args) {
        Runtime.getRuntime().addShutdownHook(new Thread(Main::shutdownBot, "funk3F-main-shutdown-hook"));
        setupMain();
    }

    private static void setupMain(){
        configW = new ConfigWorker(configPath);
        try {
            configW.readBotConfig(true);
        } catch (IllegalArgumentException ex) {
            System.err.println("\n=========================================");
            System.err.println("BOT START CANCELED!");
            System.err.println(ex.getMessage());
            System.err.println("=========================================\n");
            exit(ExitCode.EXIT_ERROR);
            return;
        }
        // Set logging properties
        System.setProperty("bot.log.path", configW.readBotConfig(false).getLogPath());
        System.setProperty("bot.log.level", configW.readBotConfig(false).getLogMode().toUpperCase());

        configW.setLogger();

        logger = LoggerFactory.getLogger(Main.class);
        logger.info("Started Main and now Starting Bot");
        try {
            bot = new Bot(configW.readBotConfig(false).getToken(), configW);
        } catch(IllegalArgumentException ex) {
            System.out.println("[ERROR] Exception: " + ex.getMessage());
            exit(ExitCode.EXIT_ERROR);
            return;
        }

    }

    public static void exit(ExitCode code) {
        new Thread(() -> {
            shutdownBot();
            System.exit(code.ordinal());
        }, "funk3F-main-shutdown").start();
    }

    public static void restart() {
        exit(ExitCode.EXIT_RESTART);
    }

    private static void shutdownBot() {
        if (!shutdownStarted.compareAndSet(false, true)) {
            try {
                shutdownDone.await(15, TimeUnit.SECONDS);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            return;
        }
        try {
            if (bot != null) {
                bot.stop();
            }
        } catch (Exception e) {
            System.err.println("Error when shutting down: " + e);
        } finally {
            shutdownDone.countDown();
        }
    }


    // set region (auch für yt search)



    public static boolean reloadBotConfig(){
        logger.info("Reloading Bot Config.");
        try {
            configW.readBotConfig(true);
        } catch (IllegalArgumentException ex){
            System.err.println("\n=========================================");
            System.err.println("Config Reload failed, same config stays active!");
            System.err.println(ex.getMessage());
            System.err.println("=========================================\n");
            return false;
        }
        bot.reloadActivity();
        // loudness manager set new scale
        return true;
    }



    public static String replaceLast(String input, String regex, String replacement) {
        return input.replaceFirst("(?s)(.*)" + regex, "$1" + replacement);
    }

}
