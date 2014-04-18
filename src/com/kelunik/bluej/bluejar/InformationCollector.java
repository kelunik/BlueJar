package com.kelunik.bluej.bluejar;

import bluej.extensions.BClass;
import bluej.extensions.BMethod;
import bluej.extensions.BPackage;
import bluej.extensions.BlueJ;

import javax.swing.*;
import java.io.File;
import java.util.ArrayList;

public class InformationCollector extends SwingWorker<Void, Void> {
	private BlueJ bluej;
	private String[] runnableClasses;
	private ArrayList<String> systemLibraries, projectLibraries;

	public InformationCollector(BlueJ bluej) {
		this.bluej = bluej;
	}

	@Override
	protected Void doInBackground () throws Exception {
		System.out.println("running...");

		runnableClasses = collectMainClasses();

		try {
			systemLibraries = collectSystemLibraries();
			projectLibraries = new ArrayList<>();
			Utils.searchForLibraries(new File(bluej.getCurrentPackage().getProject().getDir(), "+libs"), projectLibraries);
		} catch(Exception e) {
			// probably no open project
			e.printStackTrace();
		}

		return null;
	}

	public String[] getMainClasses() {
		return runnableClasses;
	}

	public ArrayList<String> getSystemLibraries() {
		return systemLibraries;
	}

	public ArrayList<String> getProjectLibraries() {
		return projectLibraries;
	}

	private String[] collectMainClasses() {
		ArrayList<String> runnableClasses = new ArrayList<>();
		runnableClasses.add("");

		try {
			for(BPackage bPackage : bluej.getCurrentPackage().getProject().getPackages()) {
				for(BClass bClass : bPackage.getClasses()) {
					for(BMethod bMethod : bClass.getMethods()) {
						if(bMethod.toString().equals("public static void main([Ljava.lang.String;)")) {
							if(bPackage.getName().equals("")) {
								runnableClasses.add(bClass.getName());
							} else {
								runnableClasses.add(bPackage.getName() + "." + bClass.getName());
							}
						}
					}
				}
			}
		} catch(Exception e) {
			e.printStackTrace();
		}

		return runnableClasses.toArray(new String[runnableClasses.size()]);
	}

	private ArrayList<String> collectSystemLibraries() throws Exception {
		ArrayList<String> systemLibraries = new ArrayList<>();

		int count = 0;
		while(true) {
			String path = bluej.getBlueJPropertyString("bluej.userlibrary" + ++count + ".location", null);

			if(path == null) {
				break;
			} else {
				systemLibraries.add(path);
			}
		}

		return systemLibraries;
	}
}
