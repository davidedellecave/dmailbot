package ddc.mailbot;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.pop3.POP3Client;

import ddc.email.LiteMailConfig;
import ddc.email.LiteMailbox;
import ddc.email.MailEval;
import ddc.email.MailHeader;
import ddc.email.MailHeaderFilter;

public class LiteMailbox_Delete {
	public static void main(String[] args) throws Throwable {
		if (args.length != 1) {
			System.out.println("Properties file required");
			return;
		}
		Path path = Paths.get(args[0]);
		if (!Files.exists(path)) {
			System.out.println("Properties file not found:[" + path + "]");
			return;
		}

		Properties props = new Properties();
		props.load(new FileInputStream(path.toFile()));
		System.out.println("conf:" + props.toString());
		Boolean simulate = Boolean.valueOf(props.getProperty("app.simulation"));
		String mailHost = props.getProperty("mail.pop");
		String mailUsername = props.getProperty("mail.username");
		String mailPassword = props.getProperty("mail.password");
		String deleteFrom = props.getProperty("delete.from");
		int deleteOlderThanDays = Integer.valueOf(props.getProperty("delete.olderThanDays"));

		deleteMail(mailHost, mailUsername, mailPassword, deleteOlderThanDays, deleteFrom, simulate);
		// deleteGottardo_AllOlder();
	}

	private static void deleteMail(String host, String username, String password, int deleteOlderThanDays, String deleteFrom, boolean simulate) throws Throwable {
		LiteMailConfig config = new LiteMailConfig();
		config.setPopHost(host);
		config.setUsername(username);
		config.setPassword(password);
		config.setMailboxLimit(0);
		config.setMailboxSimulation(simulate);
		LiteMailbox mbox = new LiteMailbox();
		//
		final ZonedDateTime olderThanFilter = ZonedDateTime.now().minus(deleteOlderThanDays, ChronoUnit.DAYS);
		mbox.log("olderThanFilter", olderThanFilter.toString());
		mbox.log("fromFilter", deleteFrom);
		//
		mbox.scan(config, new MailHeaderFilter() {
			@Override
			public boolean filter(LiteMailConfig config, MailHeader header) throws ParseException {
				if (header != null && header.getDate() != null && header.getFrom() != null) {
					boolean isOlder = header.getDate().isBefore(olderThanFilter);
					boolean isFrom=true;
					if (!StringUtils.isBlank(deleteFrom)) {
						isFrom = header.getFrom().startsWith(deleteFrom);	
					}
					return isOlder && isFrom;
				} else {
					mbox.log("One or more field is null", header);
				}
				return false;
			}
		}, new MailEval() {
			@Override
			public int execute(LiteMailConfig config, POP3Client client, MailHeader header) throws IOException {
				if (client.deleteMessage(header.id)) {
					return 1;
				} else {
					return 0;
				}
			}
		});
		mbox.log("Terminated", config.toString(), mbox.getStats().toString());
	}

	private static void deleteGottardo_AllOlder() throws Throwable {
		LiteMailConfig config = new LiteMailConfig();
		config.setPopHost("mail.medisportgottardo.it");
		config.setUsername("info@medisportgottardo.it");
		config.setPassword("Dica-Trenta3");
		config.setMailboxLimit(0);
		//
		config.setMailboxSimulation(true);

		LiteMailbox mbox = new LiteMailbox();

		final int OLDER_THAN_MONTHS = 3;
		//
		final ZonedDateTime olderThanFilter = ZonedDateTime.now().minus(OLDER_THAN_MONTHS, ChronoUnit.MONTHS);
		mbox.log("olderThanFilter", olderThanFilter.toString());
		// final ZonedDateTime olderThanFilter =
		// ZonedDateTime.parse("2016-12-31T23:59:59+02:00[Europe/Rome]");
		//
		mbox.scan(config, new MailHeaderFilter() {
			@Override
			public boolean filter(LiteMailConfig config, MailHeader header) throws ParseException {

				if (header != null && header.getDate() != null && header.getFrom() != null) {

					boolean isOlder = header.getDate().isBefore(olderThanFilter);

					String fromFilter = "Postino@smtp100.ext.armada.it";
					boolean isPostino = header.getFrom().startsWith(fromFilter);

					return isOlder && !isPostino;
				} else {
					mbox.log("One or more field is null" + header);
				}
				return false;
			}
		}, new MailEval() {
			@Override
			public int execute(LiteMailConfig config, POP3Client client, MailHeader header) throws IOException {
				if (client.deleteMessage(header.id)) {
					mbox.log("Delete message", header.toString());
					return 1;
				} else {
					return 0;
				}
			}
		});
		mbox.log("terminated", config.toString(), mbox.getStats().toString());
	}
}
