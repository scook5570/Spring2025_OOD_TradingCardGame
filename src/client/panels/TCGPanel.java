package client.panels;

import java.awt.*;

import javax.swing.*;

import client.frames.MainFrame;
import client.objects.MenuButtons;
import client.objects.PlayerInfoPanel;
import client.utils.TCGUtils;

public class TCGPanel extends JPanel {
    public MainFrame parentFrame;
    public String username;
    public GridBagConstraints gbc;
    public Rectangle rect;
    
    public TCGPanel(MainFrame parentFrame, String username){
        super();
        this.parentFrame = parentFrame;
        this.username = username;
        this.rect = this.parentFrame.getRect();
        setLayout(new GridBagLayout());
        setBackground(TCGUtils.BACKGROUND_COLOR);

        PlayerInfoPanel userInfoPanel = new PlayerInfoPanel(this.username);

        // Panel with menu buttons
        MenuButtons bottomPanel = new MenuButtons(this.parentFrame);

        this.gbc = new GridBagConstraints();

        // Top panel aligned to the top right
        this.gbc.gridx = 3; // right side
        this.gbc.anchor = GridBagConstraints.NORTHEAST;
        add(userInfoPanel, this.gbc);

        // Botton panel full width
        this.gbc.gridx = 0;
        this.gbc.gridy = 2;
        this.gbc.gridwidth = 4; // spans all columns
        this.gbc.weightx = 1;
        this.gbc.fill = GridBagConstraints.HORIZONTAL;
        add(bottomPanel, this.gbc);

        // Reset the grid baf constarints to avoid bugs
        this.gbc = new GridBagConstraints();
    }

    public void addMainComponent(JComponent component){
        // Size the component properly
        component.setPreferredSize(new Dimension((int) (this.rect.width * 0.7), (int) (this.rect.height * 0.7)));

        // Centered collection panel
        gbc.gridy = 1;
        gbc.gridwidth = GridBagConstraints.REMAINDER; // last item on row
        gbc.weighty = 1; // allocates extra space so nicely cnetered
        gbc.anchor = GridBagConstraints.CENTER;
        add(component, gbc);
    }

    public String getUsername(){
        return this.username;
    }
}
