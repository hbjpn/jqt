/**
 * 
 * Copyright (C) 2012-2013 Hiroyuki Baba, All Rights Reserved.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License or any
 * later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR POURPOSE. See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package net.abcabba.jqt.gui;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class PreferenceDialog extends JDialog implements ActionListener{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	

	JButton OKButton = new JButton("OK");
	JButton CancelButton = new JButton("Cancel");
	
	JTextField maxAvailableProcessorTextField = new JTextField("",3);
	
	Object lastOperation = null;
	
	public PreferenceDialog(JFrame owner) {
		super(owner);		
		setTitle("Preference");
		setBounds(64, 64, 300, 120);
		
		setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
		Container c = getContentPane();
		
		JPanel panel = new JPanel();
		c.add(panel);
	

		// Parent panel
		panel.setBorder(new EmptyBorder(5,5,5,5));
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		// Panel for configurations
		JPanel configPanel = new JPanel();
		
		configPanel.add(new JLabel("Maximum processors to use : "));
		configPanel.add(maxAvailableProcessorTextField);
		
		panel.add(configPanel);
		
		// Panel for buttons
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(OKButton);
		buttonPanel.add(CancelButton);
		
		OKButton.addActionListener(this);
		CancelButton.addActionListener(this);
		
		panel.add(buttonPanel);
	}
	
	public void actionPerformed(ActionEvent e) {
		
		if( e.getSource() == OKButton ){
			lastOperation = OKButton;
			setVisible(false);
		}else if( e.getSource() == CancelButton ){
			lastOperation = CancelButton;
			setVisible(false);
		}		
	}

}