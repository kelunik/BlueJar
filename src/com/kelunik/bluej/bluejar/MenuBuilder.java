package com.kelunik.bluej.bluejar;

import bluej.extensions.BPackage;
import bluej.extensions.BlueJ;
import bluej.extensions.MenuGenerator;

import javax.swing.*;
import java.awt.event.ActionEvent;

class MenuBuilder extends MenuGenerator {
	private JMenuItem menuItem;

	public MenuBuilder(final BlueJ bluej) {
		this.menuItem = new JMenuItem(new AbstractAction() {
			{
				putValue(AbstractAction.NAME, ".jar-Datei mit BlueJar exportieren");
			}

			public void actionPerformed(ActionEvent e) {
				new ExportDialog(bluej);
			}
		});
	}

	public JMenuItem getToolsMenuItem(BPackage aPackage) {
		return this.menuItem;
	}

	public void setEnabled (boolean b) {
		this.menuItem.setEnabled(b);
	}
}