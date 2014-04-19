package com.kelunik.bluej.bluejar;

import bluej.extensions.BClass;
import bluej.extensions.BPackage;
import bluej.extensions.BlueJ;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

public class ExportDialog extends JDialog implements ActionListener {
	private BlueJ bluej;
	private JCheckBox[] systemLibraries;
	private JCheckBox[] projectLibraries;
	private JComboBox<String> mainClasses;

	public ExportDialog(final BlueJ bluej) {
		super(bluej.getCurrentFrame(), false);

		this.bluej = bluej;

		JPanel pa = new JPanel();
		pa.setLayout(new BoxLayout(pa, BoxLayout.PAGE_AXIS));
		pa.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));
		pa.add(new JLabel("Informationen werden gesammelt..."));
		this.getContentPane().add(pa);
		this.packAndCenter();
		this.setVisible(true);

		new InformationCollector(bluej) {
			@Override
			protected void done () {
				ArrayList<String> systemLibrariesList = getSystemLibraries();
				ArrayList<String> projectLibrariesList = getProjectLibraries();

				systemLibraries = new JCheckBox[systemLibrariesList.size()];
				projectLibraries = new JCheckBox[projectLibrariesList.size()];
				mainClasses = new JComboBox<>(getMainClasses());

				JPanel p = new JPanel();
				p.setLayout(new BoxLayout(p, BoxLayout.PAGE_AXIS));
				p.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

				p.add(new JLabel("Wähle eine der Klassen mit main-Methode, falls dein Java Archiv ausführbar sein soll:"));
				p.add(mainClasses);
				p.add(Box.createRigidArea(new Dimension(0, 15)));

				p.add(new JLabel("System-Bibliotheken"));
				p.add(new JSeparator(SwingConstants.HORIZONTAL));

				for(int i = 0; i < systemLibraries.length; i++) {
					systemLibraries[i] = new JCheckBox(systemLibrariesList.get(i), false);
					p.add(systemLibraries[i]);
				}

				p.add(Box.createRigidArea(new Dimension(0, 15)));
				p.add(new JLabel("Projekt-Bibliotheken"));
				p.add(new JSeparator(SwingConstants.HORIZONTAL));

				for(int i = 0; i < projectLibraries.length; i++) {
					projectLibraries[i] = new JCheckBox(projectLibrariesList.get(i), true);
					p.add(projectLibraries[i]);
				}

				p.add(Box.createRigidArea(new Dimension(0, 15)));

				JButton button = new JButton("Exportieren");
				button.addActionListener(ExportDialog.this);
				p.add(button);

				getContentPane().removeAll();
				getContentPane().add(p);
				packAndCenter();
			}
		}.execute();
	}

	@Override
	public void actionPerformed (ActionEvent actionEvent) {
		final ArrayList<String> export = new ArrayList<>();

		for(JCheckBox box : systemLibraries) {
			if(box.isSelected()) {
				export.add(box.getText());
			}
		}

		for(JCheckBox box : projectLibraries) {
			if(box.isSelected()) {
				export.add(box.getText());
			}
		}

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.PAGE_AXIS));
		panel.setBorder(BorderFactory.createEmptyBorder(40, 80, 40, 80));

		JLabel label = new JLabel( "Exportiert. Bitte warten...");
		panel.add(label);

		getContentPane().removeAll();
		getContentPane().add(panel);
		packAndCenter();

		new SwingWorker<Void, Void>() {
			@Override
			protected Void doInBackground () throws Exception {
				return export(export);
			}

			public void done() {
				setVisible(false);
				dispose();
			}
		}.execute();
	}

	private void packAndCenter() {
		pack();

		Frame wnd = bluej.getCurrentFrame();
		setLocation(wnd.getX() + (wnd.getWidth() - getWidth()) / 2, wnd.getY() + (wnd.getHeight() - getHeight()) / 2);
	}

	private Void export(ArrayList<String> libs) throws Exception {
		try {
			bluej.getCurrentPackage().compileAll(true);

			File path = bluej.getCurrentPackage().getProject().getDir();
			File build = new File(path, "tmp");

			Utils.deleteDirectory(build);
			build.mkdirs();

			for(BPackage bPackage : bluej.getCurrentPackage().getProject().getPackages()) {
				for(BClass bClass : bPackage.getClasses()) {
					if(bClass.isCompiled()) {
						String name = bClass.getName();
						Path target = build.toPath().resolve(name.replaceAll("\\.", File.separator) + ".class");
						target.toFile().getParentFile().mkdirs();
						Files.copy(bClass.getClassFile().toPath(), target);
					} else {
						setVisible(false);
						dispose();

						return null;
					}
				}
			}

			try {
				for(String lib : libs) {
					Path newPath = Paths.get(build.toString(), new File(lib).getName());
					Files.copy(Paths.get(lib), newPath);
					Utils.unjar(newPath.toFile());
				}
			} catch(IOException e) {
				e.printStackTrace();
				return null;
			}

			Manifest manifest = new Manifest();
			manifest.getMainAttributes().put(Attributes.Name.MANIFEST_VERSION, "1.0");

			String mainClass = mainClasses.getSelectedItem().toString();

			if(!mainClass.isEmpty()) {
				manifest.getMainAttributes().put(Attributes.Name.MAIN_CLASS, mainClass);
			}

			File output = new File(path, bluej.getCurrentPackage().getProject().getName() + ".jar");

			try {
				Utils.jar(build, output, manifest);
			} catch(Exception e) {
				return null;
			}

			output.setExecutable(true);

			return null;
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
	}
}
