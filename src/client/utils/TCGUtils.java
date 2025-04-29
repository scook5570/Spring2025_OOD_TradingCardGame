package client.utils;

import java.awt.*;

/**
 * Utility class for shared constants and configurations used across the TCG
 * (Trading Card Game) client application.
 */
public class TCGUtils {


    public static final String SERVERADDRESS = "localhost";

    public static final int PORT = 5000;

    /**
     * General background color used throughout the application.
     * Currently set to white (RGB: 255, 255, 255).
     */
    public static final Color BACKGROUND_COLOR = new Color(255, 255, 255);

    /**
     * Background color for the user information panel.
     * Set to a light gray (RGB: 217, 217, 217) to distinguish it from other
     * sections.
     */
    public static final Color USER_INFO_BC = new Color(217, 217, 217);

    /**
     * Identifier for the Home panel.
     * Used for navigation or layout management purposes.
     */
    public static final String HOME = "Home";

    /**
     * Identifier for the Collection panel.
     * Represents the user's card collection section.
     */
    public static final String COLLECTION = "collection";

    /**
     * Identifier for the Trade panel.
     * Represents the trading interface within the application.
     */
    public static final String TRADE = "Trade";
}
