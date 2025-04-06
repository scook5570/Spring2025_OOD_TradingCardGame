package shared;

import java.io.File;

class Card {
    private String name; // Name of the card
    private int rarity; // Rarity of the card (1-5)
    private File image; // Image of the card
    
    /**
     * Trading card constructor
     * @param name name of the card
     */
    Card(String name, int rarity, File image) {
        this.name = name;
        this.rarity = rarity;
        this.image = image;
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
    public File getImage() {
        return image;
    }
}