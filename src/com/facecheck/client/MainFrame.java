package com.facecheck.client;

import com.facecheck.tools.AppInfo;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 *
 * @author user
 */
public class MainFrame extends JFrame {
    public MainFrame(){
        super(AppInfo.APP_PRINT_NAME);
        initBody();
        pack();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(640,480));
        setLocationRelativeTo(null);
    }
    
    private JLabel lblTitle;
    private JPanel body;
    private void initBody(){
        Border padding = BorderFactory.createEmptyBorder(5,5,5,5);
        ((JComponent) getContentPane()).setBorder(padding);
        lblTitle = new JLabel(AppInfo.APP_PRINT_NAME);
        lblTitle.setFont(new Font(null, Font.BOLD, 18));
        getContentPane().add(lblTitle, BorderLayout.NORTH);
        
        body = new JPanel(new BorderLayout());
        getContentPane().add(body, BorderLayout.CENTER);
    }
    
    public void setTitle(final String s){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                lblTitle.setText(s);
            }
        });
    }
    
    public void setContent(final JPanel content, final String title){
        SwingUtilities.invokeLater(new Runnable(){
            @Override
            public void run() {
                body.removeAll();
                body.add(content, BorderLayout.CENTER);
                lblTitle.setText(title);
                body.repaint();
                pack();
            }
        });
    }
}
