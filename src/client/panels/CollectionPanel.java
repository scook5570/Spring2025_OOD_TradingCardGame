package client.panels;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import client.frames.MainFrame;
import client.objects.MenuButtons;
import client.objects.PlayerInfoPanel;
import client.utils.TCGUtils;

public class CollectionPanel extends TCGPanel {

    public CollectionPanel(MainFrame parentFrame, String username){
        super(parentFrame, username);

        // Panel where the cards will be displayed
        JPanel collectionPanel = new JPanel();
        collectionPanel.setBackground(Color.GRAY);
        collectionPanel.setLayout(new GridBagLayout());
        JScrollPane collectionScrollPane = new JScrollPane(collectionPanel);
        collectionScrollPane.setBackground(Color.GRAY);
        // Stops it from streaching horizontally
        collectionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // Change scroll speed
        collectionScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        Rectangle rect = this.parentFrame.getRect();
        collectionScrollPane.setPreferredSize(new Dimension((int) (rect.width * 0.7), (int) (rect.height * 0.7)));

        addMainComponent(collectionScrollPane);        
    }
}
