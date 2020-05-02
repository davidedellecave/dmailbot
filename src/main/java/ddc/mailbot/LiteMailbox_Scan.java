package ddc.mailbot;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;

import ddc.email.DefaultMailHeaderParser;
import ddc.email.LiteMailConfig;
import ddc.email.LiteMailbox;

public class LiteMailbox_Scan {
	public static void main(String[] args) throws Throwable {
		scan1();
	}
	
	private static void scan1() throws Throwable {
		LiteMailConfig config = new LiteMailConfig();
		config.setPopHost("mail.medisportgottardo.it");
		config.setUsername("info@medisportgottardo.it");
		config.setPassword("Dica-Trenta3");
		config.setMailboxLimit(1);
		config.setMailboxSimulation(true);

		LiteMailbox mbox = new LiteMailbox();
		
		final int OLDER_THAN_DAYS=15;
		//
		final ZonedDateTime olderThanFilter = ZonedDateTime.now().minus(OLDER_THAN_DAYS, ChronoUnit.DAYS);
		mbox.log("olderThanFilter", olderThanFilter.toString());
		// final ZonedDateTime olderThanFilter = ZonedDateTime.parse("2016-12-31T23:59:59+02:00[Europe/Rome]");
		final String fromFilter = "info@medisportgottardo.it";
		mbox.log("fromFilter", fromFilter);
		
		mbox.scan(config, new DefaultMailHeaderParser());
		//		
//		mbox.scan(config, new MailHeaderParser() {			
//			@Override
//			public void parse(LiteMailConfig config, int messageId, String rawLine, MailHeader header) throws Throwable {
//				mbox.log(rawLine);
//				header.parseLine(rawLine);
//			}
//		}, new MailHeaderFilter() {
//			@Override
//			public boolean filter(LiteMailConfig config, MailHeader header) throws ParseException {
//				boolean isOlder = header.getDate().isBefore(olderThanFilter);
//				boolean isFrom = header.getFrom().startsWith(fromFilter);
//				return isOlder && isFrom;
//			}
//		}, new MailEval() {
//			@Override
//			public int execute(LiteMailConfig config, POP3Client client, MailHeader header) throws IOException {
//				if (client.deleteMessage(header.id)) {
//					return 1;
//				} else {
//					return 0;
//				}
//			}
//		});
		mbox.log("terminated", config.toString(), mbox.getStats().toString());
	}
}
