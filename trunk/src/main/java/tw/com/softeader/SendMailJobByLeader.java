package tw.com.softeader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

public class SendMailJobByLeader {
	
	public enum GroupEnum {

		Glory, David, Pure, Beloved, BeHappy, Joy, Agape, Vine;

		public static String getName(String s) {
			if (Glory.name().equals(s)) {
				return "榮耀";
			} else if (David.name().equals(s)) {
				return "大衛";
			} else if (Pure.name().equals(s)) {
				return "蒲兒";
			} else if (Beloved.name().equals(s)) {
				return "Beloved";
			} else if (BeHappy.name().equals(s)) {
				return "BeHappy";
			} else if (Joy.name().equals(s)) {
				return "喜樂泉";
			} else if (Agape.name().equals(s)) {
				return "愛加倍";
			} else if (Vine.name().equals(s)) {
				return "葡萄樹";
			}
			throw new IllegalArgumentException("No Enum specified for this string");
		}

		public static String getType(String s) {
			if (Glory.name().equals(s) || David.name().equals(s) || Pure.name().equals(s) || Beloved.name().equals(s) || BeHappy.name().equals(s)) {
				return "小組";
			} else if (Joy.name().equals(s) || Agape.name().equals(s) || Vine.name().equals(s)) {
				return "小家";
			}
			throw new IllegalArgumentException("No Enum specified for this string");
		}
	}

	private static String path;
	private static String leaderXls;
	private static String memberXls;
	private static String cc;

	private static String host;
	private static int port;
	private static String username;
	private static String password;
	private static String subject;
	
	private static String groupString;
	private static Map<String, Group> groups = new HashMap<String, Group>();

	private static Logger log = LoggerFactory.getLogger(SendMailJobByLeader.class.getClass());

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

		// 讀設定檔, 設定小組名稱
		groupString = config.getValue("group");
		String[] groupStrArray = groupString.split("[(;)\\s]+");

		for (int i = 0; i < groupStrArray.length; i++) {
			groups.put(groupStrArray[i], null);
		}

		log.debug("path:" + path);
		log.debug("leaderXls:" + leaderXls);
		log.debug("memberXls:" + memberXls);

		log.debug("host:" + host);
		log.debug("port:" + port);
		log.debug("username:" + username);
		log.debug("password:" + password);
		log.debug("cc:" + cc);

		log.debug(groups.toString());
	}

	public static List<Leader> readExcel4Leader() {
		List<Leader> leaders = new ArrayList<Leader>();
		List<String> messages = readExcel(leaderXls);

		for (String message : messages) {
			Leader leader = new Leader();
			// 用(;)切割字串，再塞入leader
			String[] leaderString = message.split("[(;)\\s]+");

			for (int i = 0; i < leaderString.length; i++) {
				if (i == 0) {
					leader.setGroup(leaderString[i]);
				} else if (i == 1) {
					leader.setCname(leaderString[i]);
				} else if (i == 2) {
					leader.setMember(leaderString[i]);
				} else if (i == 3) {
					leader.setEmail(leaderString[i]);
				}
			}

			// log.debug(leader);
			leaders.add(leader);
		}
		return leaders;
	}

	public static List<Member> readExcel4Member() {
		List<Member> members = new ArrayList<Member>();
		List<String> messages = readExcel(memberXls);

		for (String message : messages) {
			Member member = new Member();
			// 用(;)切割字串，再塞入member
			String[] memberString = message.split("[(;)\\s]+");
			
			for (int i = 0; i < memberString.length; i++) {
//				log.debug("memberString={}", memberString[i]);
				if (i == 0) {
					member.setName(memberString[i]);
				} else if (i == 1) {
					member.setEmail("n/a".equals(memberString[i]) ? "" : memberString[i]);
				} else if (i == 2) {
					member.setPath(path + memberString[i]);
				} else if (i == 3) {
					member.setGroup(memberString[i]);
				} else if (i == 4) {
					member.setSize(memberString[i]);
				}
			}

			// log.debug(leader);
			members.add(member);
		}
		return members;
	}

	public static List<String> readExcel(String filename) {

		List<String> messages = new ArrayList<String>();

		File file = new File(path + filename);
		FileInputStream fis = null;

		// Finds the workbook instance for XLSX file
		XSSFWorkbook myWorkBook = null;

		try {
			fis = new FileInputStream(file);
			myWorkBook = new XSSFWorkbook(fis);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		// Return first sheet from the XLSX workbook
		XSSFSheet mySheet = myWorkBook.getSheetAt(0);

		// Get iterator to all the rows in current sheet
		Iterator<Row> rowIterator = mySheet.iterator();

		int count = 0;

		// Traversing over each row of XLSX file
		while (rowIterator.hasNext()) {

			Row row = rowIterator.next();

			// 略過第一行
			if (count == 0) {
				row = rowIterator.next();
				count++;
			}

			StringBuffer message = new StringBuffer();

			// For each row, iterate through each columns
			Iterator<Cell> cellIterator = row.cellIterator();
			while (cellIterator.hasNext()) {

				Cell cell = cellIterator.next();

				switch (cell.getCellType()) {
				case Cell.CELL_TYPE_STRING:
					log.debug(cell.getStringCellValue() + "\t");
					message.append(cell.getStringCellValue() + "(;)");
					break;
				case Cell.CELL_TYPE_NUMERIC:
					log.debug(String.valueOf(cell.getNumericCellValue()) + "\t");
					message.append(String.valueOf(cell.getNumericCellValue()) + "(;)");
					break;
				case Cell.CELL_TYPE_BOOLEAN:
					log.debug(String.valueOf(cell.getBooleanCellValue()) + "\t");
					message.append(String.valueOf(cell.getBooleanCellValue()) + "(;)");
					break;
				default:
					message.append(" (;)");
				}
			}
			log.debug("\n");
			messages.add(message.toString());
		}

		return messages;
	}

	// 將人員根據小組長/小家長 -> 小組/小家分類
	private static void categorize(Map<String, Group> groups, List<Leader> leaders, List<Member> members) {
		String groupName = "";

		for (Map.Entry<String, Group> entry : groups.entrySet()) {
			log.debug(entry.getKey());
			groupName = entry.getKey();

			List<Leader> leaderGroup = new ArrayList<Leader>();
			List<Member> memberGroup = new ArrayList<Member>();
			Group groupEntry = new Group();

			for (Leader leader : leaders) {
				if (leader.getGroup().contains(groupName)) {
					leaderGroup.add(leader);
				}
			}

			groupEntry.setLeaders(leaderGroup);

			for (Member member : members) {
				if (member.getGroup().contains(groupName)) {
					memberGroup.add(member);
				}
			}

			groupEntry.setMembers(memberGroup);

			entry.setValue(groupEntry);
		}
	}

	public static void sendMail(Entry<String, Group> entry) {

		if (entry.getValue().getLeaders().isEmpty() || entry.getValue().getMembers().isEmpty()) {
			return;
		}
		
		// 根據群組底下的mp3大小分割, 上限為25MB
		List<Entry<String, Group>> splitGroups = new ArrayList<Entry<String, Group>>();
		List<Member> members = new ArrayList<Member>();
		double size = 0;
		for (Member member : entry.getValue().getMembers()) {
//			log.debug("member size={}", member.getSize().replace("E", "E+"));
			size = size + (Double.parseDouble(member.getSize().replace("E", "E+")) / 1024 / 1024);
//			log.debug("size={}", size);
			if (size < 23) {
				members.add(member);
			} else {
//				log.debug("members={}", members);
				size = 0;
				size = size + (Double.parseDouble(member.getSize().replace("E", "E+")) / 1024 / 1024);
				Group group = new Group();
				BeanUtils.copyProperties(entry.getValue(), group);
				group.setMembers(members);
				splitGroups.add(new java.util.AbstractMap.SimpleEntry<String, Group>(entry.getKey(), group));
				
			    members = new ArrayList<Member>();
			    members.add(member);
			}
		}
//		log.debug("members={}", members);
		Group group = new Group();
		BeanUtils.copyProperties(entry.getValue(), group);
		group.setMembers(members);
		splitGroups.add(new MyEntry<String, Group>(entry.getKey(), group));
		
		
//		log.debug("splitGroup={}", splitGroups);

		for (Entry<String, Group> splitGroup : splitGroups) {
			// log.debug("splitGroup={}", splitGroup);
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
				message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(getReceipients(splitGroup.getValue())));
				message.setRecipients(Message.RecipientType.CC, InternetAddress.parse(cc));
				
				String splitGroupString = (splitGroups.size() > 1) ? " - (" + (splitGroups.indexOf(splitGroup) + 1) + ")" : "";
				log.debug(subject + "(" + GroupEnum.getName(splitGroup.getKey()) + ")" + splitGroupString);
				message.setSubject(subject + "(" + GroupEnum.getName(splitGroup.getKey()) + ")" + splitGroupString);
				
				setMimeBodyPart(message, splitGroup);
				
				// log.debug(message.toString());
				Transport transport = session.getTransport("smtp");
				transport.connect(host, port, username, password);
				
				log.debug("Sending " + GroupEnum.getName(splitGroup.getKey()) + " 錄音檔 ing");
				Transport.send(message);
				
				log.debug("Done");
				
			} catch (MessagingException e) {
				throw new RuntimeException(e);
			}
		}
	}

	// 取得多個小組長/小家長email
	private static String getReceipients(Group group) {
		int leaderCount = group.getLeaders().size();
		String concatMail = "";
		for (int i = 0; i < leaderCount; i++) {
			concatMail = (concatMail.trim().length() == 0 ? concatMail : concatMail + ",") + group.getLeaders().get(i).getEmail();
		}
		log.debug("leader mail:" + concatMail);
		return concatMail;
	}

	private static void setMimeBodyPart(Message message, Entry<String, Group> entry) {

		List<Member> members = entry.getValue().getMembers();

		// creates message part
		Multipart multipart = new MimeMultipart();
		MimeBodyPart messageBodyPart = new MimeBodyPart();

		// 信件文字內容
		setTextBodyPart(multipart, GroupEnum.getName(entry.getKey()), GroupEnum.getType(entry.getKey()));

		// 附加檔案
		try {

			for (Member member : members) {
				messageBodyPart = new MimeBodyPart();
				log.debug(member.toString());
				DataSource source = new FileDataSource(member.getPath());
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

	// 信件文字內容
	private static void setTextBodyPart(Multipart multipart, String groupName, String type) {

		MimeBodyPart textPart = new MimeBodyPart();
		StringBuffer html = new StringBuffer();
		html.append("<h4>Dear <font color=\"red\"><u>" + groupName + "</u></font> <font color=\"purple\"> " + type + "同工:</font> </h4>");
		html.append("附件為該" + type + subject.substring(subject.indexOf("_") + 1, subject.length()) +"<br/>");
		html.append("ps.若有遺漏組員或有問題，請再回覆網路組!Tks!<br/><br/>");

		html.append("<font color=\"blue\"><Strong>Best Wishes!</Strong></font><br/>");
		html.append("<font color=\"blue\"><Strong>[箴言8:17 愛我的，我也愛他；懇切尋求我的，必尋得見]</Strong></font>");

		try {
			textPart.setContent(html.toString(), "text/html; charset=UTF-8");
			multipart.addBodyPart(textPart);
		} catch (MessagingException e) {
			e.printStackTrace();
		}
	}

	public static void main(String args[]) {

		// read config from properties
		init();

		// 讀取小組長, 小家長 Excel(姓名, email, 小組)
		List<Leader> leaders = readExcel4Leader();
		for (Leader leader : leaders) {
			log.debug(leader.toString());
		}

		// 讀小組成員清單(姓名, email, 檔案位置, 小組)
		List<Member> members = readExcel4Member();
		for (Member member : members) {
			log.debug(member.toString());
		}

		// 分類By 小組, 小家長, 小組員
		categorize(groups, leaders, members);

		// 寄信
		for (Map.Entry<String, Group> entry : groups.entrySet()) {
			log.debug(entry.toString());
			sendMail(entry);
		}
		
	}

}
