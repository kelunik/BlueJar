package com.kelunik.bluej.bluejar;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.JarOutputStream;
import java.util.jar.Manifest;

public class Utils {
	public static boolean deleteDirectory(File directory) {
		if(directory.exists()) {
			File[] files = directory.listFiles();

			if(files != null){
				for(File f : files) {
					if(f.isDirectory()) {
						deleteDirectory(f);
					} else {
						if(!f.delete()) {
							return false;
						}
					}
				}
			}
		}

		return directory.delete();
	}

	public static void searchForLibraries (File root, ArrayList<String> libraries) {
		if(root == null || libraries == null) {
			throw new IllegalArgumentException("root or libraries was null");
		}

		if(root.isDirectory()) {
			File[] files  = root.listFiles();

			if(files != null) {
				for(File file : files) {
					searchForLibraries(file, libraries);
				}
			}
		} else if(root.isFile() && root.getName().endsWith(".jar")) {
			libraries.add(root.getAbsolutePath());
		}
	}

	public static void jar(File src, File dest, Manifest manifest) throws IOException {
		if(!src.isDirectory()) {
			throw new IllegalArgumentException("Source is not a directory!");
		}

		JarOutputStream target = new JarOutputStream(new FileOutputStream(dest), manifest);
		addFileToJar(src, src, target);
		target.close();
	}

	private static void addFileToJar(File root, File source, JarOutputStream target) throws IOException {
		BufferedInputStream in = null;

		try {
			if (source.isDirectory()) {
				String name = root.toPath().relativize(source.toPath()).toString().replace("\\", "/");

				if (!name.isEmpty()) {
					if (!name.endsWith("/")) {
						name += "/";
					}

					JarEntry entry = new JarEntry(name);
					entry.setTime(source.lastModified());
					target.putNextEntry(entry);
					target.closeEntry();
				}

				File[] files = source.listFiles();

				if(files != null) {
					for (File nestedFile : files) {
						addFileToJar(root, nestedFile, target);
					}
				}
			} else {
				String name = root.toPath().relativize(source.toPath()).toString().replace("\\", "/");
				JarEntry entry = new JarEntry(name);
				entry.setTime(source.lastModified());

				try {
					target.putNextEntry(entry);

					in = new BufferedInputStream(new FileInputStream(source));

					byte[] buffer = new byte[1024];
					int count;

					while ((count = in.read(buffer)) != -1) {
						target.write(buffer, 0, count);
					}

					target.closeEntry();
				} catch(Exception e) {
					// may be other manifest files
				}
			}
		} finally {
			if (in != null) {
				in.close();
			}
		}
	}

	public static void unjar(File file) throws IOException {
		JarFile jar = new JarFile(file);

		for (Enumeration<JarEntry> enums = jar.entries(); enums.hasMoreElements();) {
			JarEntry entry = enums.nextElement();
			String name = entry.getName();

			if(!name.isEmpty()) {
				File f = new File(file.getParent(), name);

				if(entry.isDirectory()) {
					f.mkdir();
					continue;
				}

				InputStream is = jar.getInputStream(entry);
				FileOutputStream fos = new FileOutputStream(f);

				while (is.available() > 0) {
					fos.write(is.read());
				}

				fos.close();
				is.close();
			}
		}
	}
}