package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.locks.ReentrantLock;

import merrimackutil.json.types.JSONArray;

/**
 * logger for trade-related operations
 */
public class TradeLogger {
    
    private static TradeLogger instance;
    private final File logFile; 
    private final ReentrantLock lock = new ReentrantLock();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

    /**
     * 
     */
    private TradeLogger() {
        // create logs directory if it doesn't exits
        File logsDir = new File("src/server/logs");
        if (!logsDir.exists()) {
            logsDir.mkdirs();
        }

        // create log file with timestamp in name
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        this.logFile = new File(logsDir, "trades-" + timestamp + ".log");

        // initialize log file with header
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println("--- Trade System Log Started at " + dateFormat.format(new Date()) + " ---");
            writer.println("Timestamp | Event | Trade_ID | Initiator | Recipient | Status | Details");
            writer.println("-----------------------------------------------------------------------");
        } catch (IOException e) {
            System.err.println("Failed to initialize log file: " + e.getMessage());
        }
    }

    /**
     * get the singleton instance of the logger
     * @return
     */
    public static synchronized TradeLogger getInstance() {

        if (instance == null) {
            instance = new TradeLogger();
        }
        return instance;
    }

    /**
     * log trade creation
     * @param tradeId
     * @param initiator
     * @param recipient
     * @param cards
     */
    public void logTradeCreation(String tradeId, String initiator, String recipient, JSONArray cards) {
        String cardCount = cards != null ? String.valueOf(cards.size()) : "0";
        log("CREATION", tradeId, initiator, recipient, "pending", "Offered " + cardCount + " cards");
    }

    /**
     * log trade acceptance
     * @param tradeId
     * @param initiator
     * @param recipient
     */
    public void logTradeAcceptance(String tradeId, String initiator, String recipient) {
        log("ACCEPTANCE", tradeId, initiator, recipient, "accepted", "Trade accepted by recipient");
    }

    /**
     * log trade rejection
     * @param tradeId
     * @param initiator
     * @param recipient
     */
    public void logTradeRejection(String tradeId, String initiator, String recipient) {
        log("REJECTION", tradeId, initiator, recipient, "rejected", "Trade rejected by recipient");
    }

    /**
     * log trade completion
     * @param tradeId
     * @param initiator
     * @param recipient
     * @param errorMsg
     */
    public void logTradeCompletion(String tradeId, String initiator, String recipient) {
        log("COMPLETION", tradeId, initiator, recipient, "completed", "Cards transferred succesfully");
    }

    /**
     * log trade failure
     * @param tradeId
     * @param initiator
     * @param recipient
     * @param errorMsg
     */
    public void logTradeFailure(String tradeId, String initiator, String recipient, String errorMsg) {
        log("FAILED", tradeId, initiator, recipient, "failed", "Error: " + errorMsg);
    }

    /**
     * log trade expiration
     * @param tradeId
     * @param initiator
     * @param recipient
     */
    public void logTradeExpiration(String tradeId, String initiator, String recipient) {
        log("EXPIRATIOJN", tradeId, initiator, recipient, "expired", "Trade expired due to inactivity");
    }

    /**
     * log the transferring of a card from and to specified users
     * @param tradeId
     * @param from
     * @param to
     * @param cardId
     * @param cardName
     */
    public void logCardTransfer(String tradeId, String from, String to, String cardId, String cardName) {
        log("CARD_TRANSFER", tradeId, from, to, "transfering", "Card" + cardId + " (" + cardName + ")");
    }

    /**
     * log an entry to the log file
     * @param event
     * @param tradeId
     * @param initiator
     * @param recipient
     * @param status
     * @param details
     */
    private void log(String event, String tradeId, String initiator, String recipient, String status, String details) {

        lock.lock();
        try(PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            StringBuilder sb = new StringBuilder();
            sb.append(dateFormat.format(new Date())).append(" | ");
            sb.append(event).append(" | ");
            sb.append(tradeId).append(" | ");
            sb.append(initiator).append( " | ");
            sb.append(recipient).append(" | ");
            sb.append(status).append(" | ");
            sb.append(details);

            writer.println(sb.toString());
        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
