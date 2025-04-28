package client;

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;

import client.objects.MenuButtons;
import client.objects.PlayerInfoPanel;

public class CollectionWindow extends JFrame {
    private JPanel collectionPanel;
    private JScrollPane collectionScrollPane;
    private String username;

    public CollectionWindow(String username) {
        this.username = username;
        setTitle("Home");
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        // Sets the frame the full screen size
        Rectangle rect = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        setBounds(rect);
        // Makes it none rezisable after its adjusted to not overtake the tool bar
        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentShown(ComponentEvent e) {
                setResizable(false);
            }
        });
        setLayout(new GridBagLayout());

        PlayerInfoPanel userInfoPanel = new PlayerInfoPanel(this.username);

        // temporary until we can obtain user from database

        // I think we shoudl just obtain the username when we log in and just keep
        // passing it arund the frames
        // we wouldnt even need to validete it anymre and since all usernames should be
        // unique it should be easier to find


        // Panel where the cards will be displayed
        collectionPanel = new JPanel();
        collectionPanel.setBackground(Color.GRAY);
        collectionPanel.setLayout(new GridBagLayout());
        collectionScrollPane = new JScrollPane(collectionPanel);
        collectionScrollPane.setBackground(Color.GRAY);
        // Stops it from streaching horizontally
        collectionScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        // Change scroll speed
        collectionScrollPane.getVerticalScrollBar().setUnitIncrement(16);

        // Panel with menu buttons
        MenuButtons bottomPanel = new MenuButtons(this);

        GridBagConstraints gbc = new GridBagConstraints();

        // Top panel aligned to the top right
        gbc.gridx = 3; // right side
        gbc.anchor = GridBagConstraints.NORTHEAST;
        add(userInfoPanel, gbc);

        // Centered collection panel
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // last item on row
        gbc.weighty = 1; // allocates extra space so nicely cnetered
        gbc.anchor = GridBagConstraints.CENTER;
        add(collectionScrollPane, gbc);

        // Botton panel full width
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 4; // spans all columns
        gbc.weightx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(bottomPanel, gbc);

        // Had to set size after settting visible to get get frame width and height
        // other wise it thinks its 0
        collectionScrollPane.setPreferredSize(new Dimension((int) (rect.width * 0.7), (int) (rect.height * 0.7)));
        setVisible(true);
    }

    private void addCards() {
        // TODO: add the cards a user has to collection scroll pane
    }
}
