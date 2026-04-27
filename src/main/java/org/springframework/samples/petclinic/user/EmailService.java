package org.springframework.samples.petclinic.user;

import com.azure.communication.email.EmailClient;
import com.azure.communication.email.EmailClientBuilder;
import com.azure.communication.email.models.EmailAddress;
import com.azure.communication.email.models.EmailMessage;
import com.azure.communication.email.models.EmailSendResult;
import com.azure.core.util.polling.SyncPoller;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

	private final EmailClient emailClient;

	@Value("${azure.communication.sender-email}")
	private String senderAddress;

	private String senderEmail;

	public EmailService(@Value("${azure.communication.connection-string:}") String connectionString) {
		if (connectionString != null && !connectionString.isBlank()) {
			this.emailClient = new EmailClientBuilder().connectionString(connectionString).buildClient();
		}
		else {
			this.emailClient = null;
		}
	}

	public void sendInvitation(String toEmail, String customerName, String inviteLink) {
		EmailMessage message = new EmailMessage().setSenderAddress(senderAddress)
			.setToRecipients(new EmailAddress(toEmail))
			.setSubject("You're invited to the WeFixCar Customer Portal")
			.setBodyHtml(buildInviteHtml(customerName, inviteLink))
			.setBodyPlainText("Hello " + customerName + ",\n\n"
					+ "You've been invited to access the WeFixCar customer portal.\n\n" + "Set up your account here: "
					+ inviteLink + "\n\nThis link expires in 48 hours.\n\nWeFixCar Team");

		if (emailClient == null) return;
		SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message, null);
		poller.waitForCompletion();
	}

	private String buildInviteHtml(String customerName, String inviteLink) {
		return """
				<html>
				<body style="font-family:Arial,sans-serif;max-width:600px;margin:0 auto;padding:24px;">
				  <h2 style="color:#2c3e50;">Welcome to the WeFixCar Customer Portal</h2>
				  <p>Hello <strong>%s</strong>,</p>
				  <p>You have been invited to access your vehicle service history and appointments online.</p>
				  <p>Click the button below to set up your account:</p>
				  <p>
				    <a href="%s"
				       style="display:inline-block;background-color:#007bff;color:white;
				              padding:12px 24px;text-decoration:none;border-radius:4px;font-size:16px;">
				      Set Up My Account
				    </a>
				  </p>
				  <p style="color:#666;font-size:12px;margin-top:24px;">
				    This link expires in 48 hours.<br/>
				    If you did not expect this invitation, please ignore this email.
				  </p>
				</body>
				</html>
				""".formatted(customerName, inviteLink);
	}

	public void sendPasswordResetEmail(String toAddress, String resetLink) {
		String subject = "Password Reset Request";

		String htmlContent = "<html><body>" + "<h2>Password Reset</h2>"
				+ "<p>We received a request to reset your password. Click the link below to set a new password:</p>"
				+ "<p><a href=\"" + resetLink + "\">Reset Password</a></p>"
				+ "<p>If you did not request this, please ignore this email.</p>" + "</body></html>";

		String plainTextContent = "Please reset your password using this link: " + resetLink
				+ "\n\nIf you did not request this, please ignore this email.";

		EmailMessage message = new EmailMessage().setSenderAddress(this.senderEmail)
			.setToRecipients(toAddress)
			.setSubject(subject)
			.setBodyHtml(htmlContent)
			.setBodyPlainText(plainTextContent);

		try {
			// Send the email and wait for the operation to complete
			SyncPoller<EmailSendResult, EmailSendResult> poller = emailClient.beginSend(message, null);
			poller.waitForCompletion();
		}
		catch (Exception e) {
			// Log the error. In a production environment, use a logger like SLF4J.
			System.err.println("Failed to send email to " + toAddress + ": " + e.getMessage());
		}
	}

}
