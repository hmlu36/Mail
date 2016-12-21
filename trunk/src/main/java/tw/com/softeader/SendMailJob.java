package tw.com.softeader;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class SendMailJob {
	
	private static String path;
	private static String leaderXls;
	private static String memberXls;
	private static String cc;

	private static String host;
	private static int port;
	private static String username;
	private static String password;
	private static String subject;
	
	private static Config config = Config.getInstance();

	// read config from properties
	public static void init() {
		// 檔案路徑設定
		path = config.getValue("path");
		leaderXls = config.getValue("leader_xls");
		memberXls = config.getValue("member_xls");

		// email 設定
		host = config.getValue("host");
		port = Integer.parseInt(config.getValue("port"));
		username = config.getValue("email");
		password = config.getValue("password");
		cc = config.getValue("cc");
		subject = config.getValue("subject");
		
		log.debug("path:" + path);
		log.debug("leaderXls:" + leaderXls);
		log.debug("memberXls:" + memberXls);

		log.debug("host:" + host);
		log.debug("port:" + port);
		log.debug("username:" + username);
		log.debug("password:" + password);
		log.debug("cc:" + cc);

	}


	@SuppressWarnings("resource")
	public static List<User> readExcel() throws IOException {

		List<User> users = new ArrayList<User>();
		log.debug("path={}", path + memberXls);
		File myFile = new File(path + memberXls);
		log.debug("file exists={}", myFile.exists());
		FileInputStream fis = new FileInputStream(myFile);

		// Finds the workbook instance for XLSX file
		XSSFWorkbook myWorkBook = new XSSFWorkbook(fis);

		// Return first sheet from the XLSX workbook
		XSSFSheet mySheet = myWorkBook.getSheetAt(0);

		// Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = mySheet.iterator();

		int count = 0;
		// Traversing over each row of XLSX file
		while (rowIterator.hasNext()) {
			Row row = rowIterator.next();
			count = 0;
			User user = new User();
			// For each row, iterate through each columns
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {

				Cell cell = cellIterator.next();

				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					// System.out.println("count:" + count + ", content:" +
					// cell.getStringCellValue() + "\t");
					if (count == 0) {
						System.out.println("count:" + count + ",name:" + cell.getStringCellValue());
						user.setName(cell.getStringCellValue());
					} else if (count == 1) {
						System.out.println("count:" + count + ",email:" + cell.getStringCellValue());
						user.setEmail(cell.getStringCellValue());
					} else if (count == 2) {
						System.out.println("count:" + count + ",path:" + cell.getStringCellValue());
						user.setPath(path + cell.getStringCellValue());
					}
					break;
				// case Cell.CELL_TYPE_NUMERIC:
				// System.out.print(cell.getNumericCellValue() + "\t");
				// break;
				// case Cell.CELL_TYPE_BOOLEAN:
				// System.out.print(cell.getBooleanCellValue() + "\t");
				// break;
				default:
				}
				count++;
			}
			users.add(user);
		}

		return users;
	}

	public static void sendMail(User user) {

		Properties props = new Properties();
		props.put("mail.smtp.host", host);
		props.put("mail.smtp.auth", "true");
		props.put("mail.smtp.starttls.enable", "true");
		props.put("mail.smtp.port", port);

		Session session = Session.getInstance(props, new Authenticator() {
			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(username, password);
			}
		});

		try {

			Message message = new MimeMessage(session);
			message.setFrom(new InternetAddress(username));
			message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(user.getEmail()));
			message.setSubject(subject);

			setMimeBodyPart(message, user);
			Transport transport = session.getTransport("smtp");
			transport.connect(host, port, username, password);

			System.out.println("Sending " + user.getName() + " 錄音檔 ing");
			Transport.send(message);

			System.out.println("Done");

		} catch (MessagingException e) {
			throw new RuntimeException(e);
		}
	}

	private static void setMimeBodyPart(Message message, User user) {

		// creates message part
		Multipart multipart = new MimeMultipart();

		setTextBodyPart(multipart, user);

		try {
			for (String path : user.getPath().split(",")) {
				
				MimeBodyPart messageBodyPart = new MimeBodyPart();
				DataSource source = new FileDataSource(path);
				messageBodyPart.setDataHandler(new DataHandler(source));
				messageBodyPart.setFileName(MimeUtility.encodeText(source.getName(), "UTF-8", null));
				// add the attachment
				multipart.addBodyPart(messageBodyPart);
			}
			message.setContent(multipart);
		} catch (MessagingException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	private static void setTextBodyPart(Multipart multipart, User user) {

		MimeBodyPart textPart = new MimeBodyPart();
		StringBuffer html = new StringBuffer();
		html.append("<h4>Dear " + user.getName() + ": </h4>");
		html.append(" 附件為" + subject.replace("[淡水讚美基督教會]", "")+ "<br>");
		html.append(" 祝福你在神的話語中得著鼓勵、方向  <br/><br/>");
		
		html.append("<font color=\"blue\"><Strong>Best Wishes!</Strong></font><br/>");
		html.append("<font color=\"blue\"><Strong>[約伯記10:12 你將生命和慈愛賜給我；你也眷顧保全我的心靈]</Strong></font><br/><br/>");
        
		html.append("=====================================================<br/>");
		html.append("推薦可以使用聽打逐字稿雲端服務，不用下載安裝軟體<a href='http://otranscribe.com/'><Strong>oTranscribe</Strong></a>     <a href='https://goo.gl/kqUb3S'>教學網誌</a><br/>");
		html.append("點擊下方的藍色按鈕「<Strong>Start Transcribing</Strong>」<br/>");
		html.append("選取上傳mp3檔案後可以進行聽打<br/>");
		html.append("從左上角可以進行<Strong>播放、暫停(ESC)、倒退(F1)、快轉(F2)</Strong><br/><br/>");

		try {
			textPart.setContent(html.toString(), "text/html; charset=UTF-8");
			multipart.addBodyPart(textPart);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) throws IOException {
		// 讀設定檔
		init();
		
		List<User> users = readExcel();
		log.debug("users={}", users);
		users.remove(0); // 去掉標頭
		
		// 過濾相同人但重複檔案
		filterMultipleFile(users);
		
		for (User user : users) {
			
			if (!user.getEmail().trim().equals("n/a")) {
				log.debug("user={}", user);
				sendMail(user);
			}
		}
		log.debug("finish!");
	}

	// 過濾同一個人但是被預言兩次
	private static void filterMultipleFile(List<User> users) {
		// 取得重複檔案
		List<User> duplicateUsers = users.stream().filter(user -> users.stream().filter(tempUser -> tempUser.getName().equals(user.getName())).count() > 1).collect(Collectors.toList());
//		log.debug("duplicateUsers={}", duplicateUsers);
		
		// 刪除多出來的那筆資料
		users.removeIf(user -> duplicateUsers.contains(user));
		
		// merge到原本那一筆
		List<String> existDuplicateUser = duplicateUsers.stream().map(User::getName).distinct().collect(Collectors.toList());
//		log.debug("existDuplicateUser={}", existDuplicateUser);
		
		List<User> distinctUsers = new ArrayList<User>();
		existDuplicateUser.forEach(userName -> {
			User tempUser = duplicateUsers.stream().filter(user -> user.getName().equals(userName)).findFirst().get();
//			log.debug("tempUser={}", tempUser);
			tempUser.setPath(duplicateUsers.stream().filter(user -> user.getName().equals(userName)).map(user -> user.getPath()).collect(Collectors.joining(",")));
			distinctUsers.add(tempUser);
		});
		log.debug("distinctUsers={}", distinctUsers);

		users.addAll(distinctUsers);
	}

}
