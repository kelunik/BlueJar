package com.kelunik.bluej.bluejar;

import bluej.extensions.BlueJ;
import bluej.extensions.Extension;
import bluej.extensions.event.PackageEvent;
import bluej.extensions.event.PackageListener;

import java.net.URL;

public class BlueJar extends Extension implements PackageListener {
	private static String VERSION = "v1.0";

	/**
	 * IntelliJ requires this for the export with a main class.
	 * @param args command line arguments
	 */
	public static void main(String[] args) {
		System.out.println("BlueJar - a BlueJ extension.");
		System.out.println("Version: " + VERSION);
		System.out.println();
	}

	private BlueJ bluej;
	private MenuBuilder menus;

	public void startup(BlueJ bluej) {
		this.bluej = bluej;
		this.bluej.addPackageListener(this);
		this.menus = new MenuBuilder(bluej);
		this.bluej.setMenuGenerator(menus);
	}

	public void packageOpened(PackageEvent ev) {
		this.menus.setEnabled(true);
	}

	public void packageClosing(PackageEvent ev) {
		this.menus.setEnabled(false);
	}

	public boolean isCompatible() {
		return true;
	}

	public String getVersion () {
		return VERSION;
	}

	public String getName () {
		return "BlueJar";
	}

	public String getDescription () {
		return "Packt Bibliotheken in exportierte .jar-Dateien.";
	}

	public URL getURL () {
		try {
			return new URL("http://github.com/kelunik/BlueJar");
		} catch (Exception e) {
			System.out.println("BlueJar extension: getURL: Exception = " + e.getMessage());
			return null;
		}
	}
}