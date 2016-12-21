package tw.com.softeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class Test {

	private static final String INPUT_FILE = "D://temp//20161117~18預言檔案//預言//整理//20161118_師母//20161118師母預言_001(凱悅2).MP3";

	private static final String OUTPUT_FILE = "D://temp//20161117~18預言檔案//預言//整理//20161118_師母//20161118師母預言_001(凱悅).zip";

	public static void main(String[] args) {
	    String test = "[淡水讚美基督教會]20161210~11_劉代華牧師聚會預言錄音檔";
	    System.out.println(test.substring(test.indexOf("_") + 1, test.length()));
	    
//		System.out.println(Double.valueOf("1.1935995E+7") / 100);
		////		zipFile(new File(INPUT_FILE), OUTPUT_FILE);

//		String sourceFile = INPUT_FILE;
//		try {
//			FileOutputStream fos = new FileOutputStream(OUTPUT_FILE);
//			
//			ZipOutputStream zipOut = new ZipOutputStream(fos);
//			
//			File fileToZip = new File(sourceFile);
//			if (fileToZip.exists()) {
//				System.out.println("exists!");
//				System.out.println(fileToZip.getUsableSpace());
//			} else {
//				System.out.println("not exists!");
//			}
//			FileInputStream fis = new FileInputStream(fileToZip);
//			ZipEntry zipEntry = new ZipEntry(fileToZip.getName());
//			zipOut.putNextEntry(zipEntry);
//			final byte[] bytes = new byte[1024];
//			int length;
//			while ((length = fis.read(bytes)) >= 0) {
//				zipOut.write(bytes, 0, length);
//			}
//			zipOut.close();
//			fis.close();
//			fos.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
	

	}

	public static void zipFile(File inputFile, String zipFilePath) {

		try {

			//// Wrap a FileOutputStream around a ZipOutputStream

			//// to store the zip stream to a file. Note that this is

			//// not absolutely necessary

			FileOutputStream fileOutputStream = new FileOutputStream(zipFilePath);

			ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);

			//// a ZipEntry represents a file entry in the zip archive

			//// We name the ZipEntry after the original file's name

			ZipEntry zipEntry = new ZipEntry(inputFile.getName());

			zipOutputStream.putNextEntry(zipEntry);

			FileInputStream fileInputStream = new FileInputStream(inputFile);

			byte[] buf = new byte[1024];

			int bytesRead;

			//// Read the input file by chucks of 1024 bytes

			//// and write the read bytes to the zip stream

			while ((bytesRead = fileInputStream.read(buf)) > 0) {

				zipOutputStream.write(buf, 0, bytesRead);

			}

			//// close ZipEntry to store the stream to the file

			zipOutputStream.closeEntry();

			zipOutputStream.close();

			fileOutputStream.close();

			System.out.println("Regular file :" + inputFile.getCanonicalPath() + " is zipped to archive :" + zipFilePath);

		} catch (IOException e) {

			e.printStackTrace();

		}

	}
}
