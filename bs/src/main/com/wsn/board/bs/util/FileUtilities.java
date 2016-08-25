package com.wsn.board.bs.util;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;

public class FileUtilities {
	public static String loadFileAsString(String fileRelativePath) {
		StringBuffer str = new StringBuffer();
		try {
			Reader reader = new InputStreamReader(FileUtilities.class
					.getClassLoader().getResourceAsStream(fileRelativePath));
			char[] buffer = new char[1024];
			int count;
			while ((count = reader.read(buffer)) != -1) {
				str.append(buffer, 0, count);
			}
			return str.toString();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load file '"
					+ fileRelativePath + "'", e);
		}

	}

	public static byte[] loadFileAsByteArray(String fileRelativePath) {
		try {
			BufferedInputStream in = new BufferedInputStream(
					FileUtilities.class.getClassLoader().getResourceAsStream(
							fileRelativePath));
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int count;
			while ((count = in.read(buffer)) != -1) {
				out.write(buffer, 0, count);
			}
			return out.toByteArray();
		} catch (IOException e) {
			throw new RuntimeException("Couldn't load file '"
					+ fileRelativePath + "'", e);
		}

	}

	public static File createTemporaryDirectory(String prefix)
			throws IOException {
		return createTemporaryDirectory(prefix, "", null);
	}

	public static File createTemporaryDirectory(String prefix, String suffix,
			File directory) throws IOException {
		if ((directory != null) && (!directory.exists())
				&& (!directory.mkdir())) {
			throw new IOException();
		}
		File tempFile = File.createTempFile(prefix, suffix, directory);
		if (!tempFile.delete()) {
			throw new IOException();
		}
		if (!tempFile.mkdir()) {
			throw new IOException();
		}
		return tempFile;
	}

	public static void compareDirectories(File dir1, File dir2) {
		checkSrcDirContainsTargetDir(dir1, dir2);
		checkSrcDirContainsTargetDir(dir2, dir1);
	}

	private static void checkSrcDirContainsTargetDir(File dirA, File dirB) {
		assert (dirA.isDirectory());
		assert (dirB.isDirectory());
		try {
			List<File> filesInDir1 = listFilesAndEmptyDirs(dirA);
			for (File file : filesInDir1) {
				String filePath = file.getCanonicalPath().substring(
						dirA.getCanonicalPath().length()
								+ File.pathSeparator.length());
				File fileInDir2 = new File(dirB, filePath);
				if (!fileInDir2.exists()) {
					throw new DirectoriesNotEqualException("File '" + filePath
							+ "' in '" + dirA.getPath()
							+ "' was not found in '" + dirB.getPath() + "'");
				}
				if (file.isDirectory()) {
					if (!fileInDir2.isDirectory())
						throw new DirectoriesNotEqualException("Subdirectory '"
								+ filePath + "' in '" + dirA.getPath()
								+ "' was found in '" + dirB.getPath()
								+ "' but it's a file, not a directory");
				} else {
					if (!fileInDir2.isFile()) {
						throw new DirectoriesNotEqualException("File '"
								+ filePath + "' in '" + dirA.getPath()
								+ "' was found in '" + dirB.getPath()
								+ "' but it's a directory, not a file");
					}
					if (!FileUtils.contentEquals(file, fileInDir2))
						throw new DirectoriesNotEqualException("File '"
								+ filePath + "' in '" + dirA.getPath()
								+ "' was found in '" + dirB.getPath()
								+ "' they differ in content");
				}
			}
		} catch (IOException e) {
			throw new FileUtilitiesException(e);
		}
	}

	public static String getRelativePath(File rootDir, File file) {
		try {
			assert (rootDir.isDirectory());
			assert (file.getCanonicalPath().startsWith(rootDir
					.getCanonicalPath()));

			return file.getCanonicalPath().substring(
					rootDir.getCanonicalPath().length() + 1);
		} catch (IOException e) {
			throw new FileUtilitiesException(e);
		}

	}

	public static String calcRelativePath(File rootDir, File file) {
		try {
			assert (rootDir.isDirectory());

			assert (file.getCanonicalPath().startsWith(rootDir
					.getCanonicalPath()));

			String filePathRelativeToRootDir = file.getCanonicalPath()
					.substring(
							rootDir.getCanonicalPath().length()
									+ File.pathSeparator.length());
			String pathRelativeToRootDir = new File(filePathRelativeToRootDir)
					.getParent();

			return pathRelativeToRootDir == null ? "" : pathRelativeToRootDir;
		} catch (IOException e) {
			throw new FileUtilitiesException(e);
		}

	}

	public static void createDirectory(File dir) {
		try {
			if (dir.exists()) {
				if (dir.isFile()) {
					throw new FileUtilitiesException(dir.getCanonicalPath()
							+ " already exists, "
							+ "but it's a file and not a directory");
				}
			} else if (!dir.mkdir())
				throw new FileUtilitiesException("could not create directory '"
						+ dir.getCanonicalPath() + "'");
		} catch (IOException e) {
			throw new FileUtilitiesException(e);
		}
	}

	public static List<File> listFilesRecursive(File rootDir) {
		return new ArrayList(FileUtils.listFiles(rootDir,
				FileFilterUtils.trueFileFilter(),
				FileFilterUtils.makeSVNAware(null)));
	}

	public static List<File> listFilesAndEmptyDirs(File dir) {
		if (!dir.exists())
			throw new FileUtilitiesException(dir.getAbsolutePath()
					+ " does not exists");
		if (!dir.isDirectory()) {
			throw new FileUtilitiesException(dir.getAbsolutePath()
					+ " is not a directory");
		}

		return listFiles(dir);
	}

	private static List<File> listFiles(File dir) {
		assert (dir.exists());
		assert (dir.isDirectory());

		List<File> result = new ArrayList();

		List<File> files = Arrays.asList(dir.listFiles((FileFilter)FileFilterUtils.makeSVNAware(null)));
		for (File file : files) {
			if (file.isDirectory()) {
				if (isEmptyDir(file))
					result.add(file);
				else
					result.addAll(listFiles(file));
			} else {
				result.add(file);
			}
		}
		return result;
	}

	private static boolean isEmptyDir(File dir) {
		assert (dir.exists());
		assert (dir.isDirectory());

		List files = Arrays.asList(dir.listFiles((FileFilter)FileFilterUtils.makeSVNAware(null)));

		return files.size() == 0;
	}

	public static File writeByteArrayToTempFile(byte[] fileContents) {
		assert (fileContents.length > 0);

		String TEST_PREFIX = "test";
		try {
			File tempFile = File.createTempFile(TEST_PREFIX, ".zip");
			FileOutputStream tempFileStream = new FileOutputStream(tempFile);
			tempFileStream.write(fileContents);
			tempFileStream.flush();
			tempFileStream.close();
			return tempFile;
		} catch (IOException e) {
			throw new FileUtilitiesException(e);
		}

	}

	public static FileOutputStream openOutputStream(File file, boolean append)
			throws IOException {
		if (file.exists()) {
			if (file.isDirectory()) {
				throw new IOException("File '" + file
						+ "' exists but is a directory");
			}
			if (!file.canWrite())
				throw new IOException("File '" + file
						+ "' cannot be written to");
		} else {
			File parent = file.getParentFile();
			if ((parent != null) && (!parent.exists()) && (!parent.mkdirs())) {
				throw new IOException("File '" + file
						+ "' could not be created");
			}
		}

		return new FileOutputStream(file, append);
	}

	public static boolean isFileInsideDirectory(File outputFile,
			File outputDirectory) throws IOException {
		return outputFile.getCanonicalPath().startsWith(
				outputDirectory.getCanonicalPath());
	}

	public static File appendToZip(File zipFile, String entryName,
			String content, String tempFilePrefix, String tempFileSuffix)
			throws IOException {
		File tempFile = null;
		ZipOutputStream append = null;
		ZipFile zip = null;
		try {
			try {
				zip = new ZipFile(zipFile);
				tempFile = File.createTempFile(tempFilePrefix, tempFileSuffix);
				append = new ZipOutputStream(new FileOutputStream(tempFile));

				Enumeration entries = zip.entries();
				while (entries.hasMoreElements()) {
					ZipEntry e = (ZipEntry) entries.nextElement();
					append.putNextEntry(e);
					if (!e.isDirectory()) {
						IOUtils.copy(zip.getInputStream(e), append);
					}
					append.closeEntry();
				}

				ZipEntry e = new ZipEntry(entryName);
				append.putNextEntry(e);
				append.write(content.getBytes("UTF-8"));
				append.closeEntry();
			} finally {
				if (zip != null) {
					zip.close();
				}
				if (append != null) {
					append.close();
				}
			}
			FileUtils.copyFile(tempFile, zipFile);
		} finally {
			FileUtils.deleteQuietly(tempFile);
		}
		return zipFile;
	}

	public static void changeAllFilesReadOnlyState(File root,
			boolean toReadOnly, String[] excludeFileNames) {
		File[] dirs = root.listFiles();
		if (dirs != null)
			for (File file : Arrays.asList(dirs))
				if (file.isFile()) {
					if (!Arrays.asList(excludeFileNames).contains(
							file.getName())) {
						file.setWritable(!toReadOnly, false);
					}
				} else if (!Arrays.asList(excludeFileNames).contains(
						file.getName()))
					changeAllFilesReadOnlyState(file, toReadOnly,
							excludeFileNames);
	}

	public static List<File> findAllReadOnlyFiles(File root, boolean recursive) {
		List readOnlyFiles = new LinkedList();
		File[] dirs = root.listFiles();
		if (dirs != null) {
			for (File file : Arrays.asList(dirs)) {
				if ((file.isFile()) && (!file.canWrite()))
					readOnlyFiles.add(file);
				else if ((file.isDirectory()) && (recursive)) {
					readOnlyFiles.addAll(findAllReadOnlyFiles(file, recursive));
				}
			}
		}
		return readOnlyFiles;
	}

	public static void forceMoveFiles(File sourceDirectory, File targetDirectory)
			throws IOException {
		if (sourceDirectory == null) {
			throw new NullPointerException("Source File is null");
		}

		if (targetDirectory == null) {
			throw new NullPointerException("Target File is null");
		}

		if (!sourceDirectory.exists()) {
			throw new IOException("Source File doesn't exist");
		}

		if (!sourceDirectory.isDirectory()) {
			throw new IOException("Source File is not a directory");
		}

		if ((targetDirectory.exists()) && (targetDirectory.isFile())) {
			throw new IOException(
					"Target File exists and is a file (not a directory)");
		}

		File[] listFiles = sourceDirectory.listFiles();
		for (File file : listFiles) {
			File targetFile = new File(targetDirectory, file.getName());
			if (file.isDirectory()) {
				if (!targetFile.exists()) {
					FileUtils.forceMkdir(targetFile);
				}
				forceMoveFiles(file, targetFile);
			} else {
				if (targetFile.exists()) {
					FileUtils.forceDelete(targetFile);
				}
				FileUtils.moveFile(file, targetFile);
			}
		}
	}

	public static byte[] readFileToByteArray(File file) throws IOException {
		return FileUtils.readFileToByteArray(file);
	}
}