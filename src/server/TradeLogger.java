package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import java.text.SimpleDateFormat;

import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

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

    private AtomicInteger totalTrades = new AtomicInteger(0);
    private AtomicInteger successfulTrades = new AtomicInteger(0);
    private AtomicInteger failedTrades = new AtomicInteger(0);
    /** AtomicInteger
     * thread safe version of an integer, no half changes or race conditions
     */

     private boolean verboseLogging = true; // set to true for detailed transaction


    /**
     * constructor 
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
     * get transaction statistics 
     * @return
     */
    public synchronized String getTradeStatistics() {
        return String.format("Trade Statistics - Total: %d, Successful: %d, Failed: %d", totalTrades.get(), successfulTrades.get(), failedTrades.get());
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
        totalTrades.incrementAndGet();
        String cardCount = cards != null ? String.valueOf(cards.size()) : "0";
        log("CREATION", tradeId, initiator, recipient, "pending", "Offered " + cardCount + " cards");

        // log detailde card information if verbose loggind is enabled
        if (verboseLogging && cards != null) {
            StringBuilder cardDetails = new StringBuilder("Cards offered: ");
            for (int i = 0; i < cards.size(); i++) {
                if (i > 0) {
                    cardDetails.append(", ");
                }
                cardDetails.append(cards.getObject(i).getString("cardID"));
            }
            log(tradeId, "CARDS_OFFERED", initiator, recipient, "pending", cardDetails.toString());
        }
    }

    /**
     * log trade acceptance
     * @param tradeId
     * @param initiator
     * @param recipient
     */
    public void logTradeAcceptance(String tradeId, String initiator, String recipient) {
        log("ACCEPTANCE", tradeId, initiator, recipient, "accepted", "Trade accepted by recipient");

        // log transaction start for debuggingn race conditions
        if (verboseLogging) {
            log(tradeId, "TRANSACTION_START", initiator, recipient, "processing", "Beginning card transfer transaction");
        }
    }

    /**
     * log trade rejection : overloaded without reason
     * @param tradeId
     * @param initiator
     * @param recipient
     */
    public void logTradeRejection(String tradeId, String initiator, String recipient) {
        logTradeRejection(tradeId, initiator, recipient, null);
    }

    /**
     * log trade rejection
     * @param tradeId
     * @param initiator
     * @param recipient
     */
    public void logTradeRejection(String tradeId, String initiator, String recipient, String reason) {
        
        String details = "Trade rejected by recipient";
        if (reason != null && !reason.isEmpty()) {
            details += ": " + reason;
        }
        
        log("REJECTION", tradeId, initiator, recipient, "rejected", details);
    }

    /**
     * log trade completion
     * @param tradeId
     * @param initiator
     * @param recipient
     * @param errorMsg
     */
    public void logTradeCompletion(String tradeId, String initiator, String recipient) {
        successfulTrades.incrementAndGet();
    
        // log transaction completion for debugging
        if (verboseLogging) {
            log(tradeId, "TRANSACTION_END", initiator, recipient, "completed", "Card transfer transaction successful");
        }

        log("COMPLETION", tradeId, initiator, recipient, "completed", "Cards transferred succesfully");
    }

    /**
     * log trade failure
     * @param tradeId
     * @param initiator
     * @param recipient
     * @param errorMsg
     */
    public void logTradeFailure(String tradeId, String initiator, String recipient, String errorMsg, Throwable exception) {
        failedTrades.incrementAndGet();

        // first log the basic error 
        log("FAILED", tradeId, initiator, recipient, "failed", "Error: " + errorMsg);

        // if exception details are available and vebose logging is enabled, log stack trace
        if (verboseLogging && exception != null) {
            StringBuilder stackTrace = new StringBuilder();
            stackTrace.append(exception.getClass().getName()).append(": ").append(exception.getMessage()).append("\n");

            for (StackTraceElement element : exception.getStackTrace()) {
                stackTrace.append("    at ").append(element.toString()).append("\n");

                // limit stack trace depth to keep logs manageable
                if (stackTrace.length() > 500) {
                    stackTrace.append("    ... (truncated)\n");
                    break;
                }
            }
            log(tradeId, "EXCPTION", initiator, recipient, "failed", stackTrace.toString());
        }
    }

    /**
     * log trade failure (overloaded without exception)
     * @param tradeId
     * @param initiator
     * @param recipient
     * @param errorMsg
     */
    public void logTradeFailure(String tradeId, String initiator, String recipient, String errorMsg) {
        logTradeFailure(tradeId, initiator, recipient, errorMsg, null);
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
        log("CARD_TRANSFER", tradeId, from, to, "transfering", "Card" + cardId + " (" + cardName + ") transferred from " + from + " to " + to);
    }

    /**
     * lof database validation operations for integrity checks 
     * @param tradeId
     * @param details
     * @param passed
     */
    public void logDatabaseValidation(String tradeId, String details, boolean passed) {
        log(tradeId, "DB_VALIDATION", "system", "system", passed ? "valid" : "invalid", details);
    }

    /**
     * log concurrent access attempts for debugging race conditions
     * @param tradeId
     * @param username
     * @param operation
     * @param successful
     */
    public void logConcurrentAccess(String tradeId, String username, String operation, boolean successful) {
        log(tradeId, "CONCURRENCY", username, "system", successful ? "acquired" : "blocked", "Thread " + Thread.currentThread().threadId() + " " + (successful ? "acquired" : "blokced from acquiring") + " access for " + operation);
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

            // for multi-line details liek stack traces
            if (details.contains("\n")) {
                String[] lines = details.split("\n"); 
                for (int i = 1; i < lines.length; i++) {
                    writer.println("   " + lines[i]);
                }
                writer.println("------------------------------------");
            }

        } catch (IOException e) {
            System.err.println("Failed to write to log file: " + e.getMessage());
        } finally {
            lock.unlock();
        }
    }
}
