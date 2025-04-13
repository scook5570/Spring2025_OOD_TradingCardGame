package shared;

public class Card {
    private String cardID;
    private String name; // Name of the card
    private int rarity; // Rarity of the card (1-5)
    private String image; // Image of the card
    
    /**
     * Trading card constructor
     * @param name name of the card
     */
    public Card(String cardID, String name, int rarity, String image) {
        this.cardID = cardID;
        this.name = name;
        this.rarity = rarity;
        this.image = image;
    }

    /**
     * Get the ID of the card
     * @return ID of the card
     */
    public String getCardID() {
        return cardID;
    }

    /**
     * Get the name of the card
     * @return name of the card
     */
    public String getName() {
        return name;
    }

    /**
     * Get the rarity of the card
     * @return rarity of the card
     */
    public int getRarity() {
        return rarity;
    }

    /**
     * Get the image of the card
     * @return image of the card
     */
    public String getImage() {
        return image;
    }
}