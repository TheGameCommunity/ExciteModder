package com.gamebuster19901.excite.modding.ui;

import java.awt.Color;

import javax.swing.JPanel;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import javax.swing.SwingConstants;

public class ColorKeyComponent extends JPanel {

	private static final long serialVersionUID = 1L;

	public ColorKeyComponent(Color color, String name) {
		this.setBorder(BorderFactory.createEmptyBorder());
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[]{0, 0};
		gridBagLayout.rowHeights = new int[]{25};
		gridBagLayout.columnWeights = new double[]{0.0, 0.0};
		gridBagLayout.rowWeights = new double[]{0.0};
		setLayout(gridBagLayout);
		JLabel coloring = new JLabel("■");
		coloring.setBorder(BorderFactory.createEmptyBorder());
		coloring.setHorizontalAlignment(SwingConstants.TRAILING);
		FlowLayout flowLayout = new FlowLayout();
		flowLayout.setAlignment(FlowLayout.LEFT);
		coloring.setForeground(color);
		GridBagConstraints gbc_coloring = new GridBagConstraints();
		gbc_coloring.anchor = GridBagConstraints.EAST;
		gbc_coloring.insets = new Insets(0, 0, 2, 0);
		gbc_coloring.gridx = 0;
		gbc_coloring.gridy = 0;
		add(coloring, gbc_coloring);
		JLabel lblName = new JLabel(":" + name);
		GridBagConstraints gbc_lblName = new GridBagConstraints();
		gbc_lblName.anchor = GridBagConstraints.WEST;
		gbc_lblName.gridx = 1;
		gbc_lblName.gridy = 0;
		add(lblName, gbc_lblName);

	}

}
