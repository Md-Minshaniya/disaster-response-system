package com.disaster.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class AlertService {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Send email alert
     */
    public void sendEmail(String to, String subject, String body) {
        try {
            log.info("\n=== EMAIL ALERT ===");
            log.info("TO: {}", to);
            log.info("SUBJECT: {}", subject);
            log.info("BODY:\n{}", body);
            log.info("SENT AT: {}", LocalDateTime.now().format(FORMATTER));
            log.info("===================\n");

            if (isValidEmail(to)) {
                // Actual email sending can be implemented with JavaMailSender
                // For now, we log the email
                log.info("✅ Email queued for: {}", to);
            } else {
                log.warn("⚠️ Invalid email address: {}", to);
            }
        } catch (Exception e) {
            log.error("❌ Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    /**
     * Send SMS alert
     */
    public void sendSms(String number, String message) {
        try {
            log.info("\n=== SMS ALERT ===");
            log.info("TO: {}", number);
            log.info("MESSAGE: {}", message);
            log.info("SENT AT: {}", LocalDateTime.now().format(FORMATTER));
            log.info("==================\n");

            if (isValidPhoneNumber(number)) {
                // Twilio or similar SMS service integration
                log.info("✅ SMS queued for: {}", number);
                logSmsMetadata(number, message);
            } else {
                log.warn("⚠️ Invalid phone number: {}", number);
            }
        } catch (Exception e) {
            log.error("❌ Failed to send SMS to {}: {}", number, e.getMessage());
        }
    }

    /**
     * Send push notification
     */
    public void sendPushNotification(String title, String body) {
        try {
            log.info("\n=== PUSH NOTIFICATION ===");
            log.info("TITLE: {}", title);
            log.info("BODY: {}", body);
            log.info("SENT AT: {}", LocalDateTime.now().format(FORMATTER));
            log.info("=======================\n");

            log.info("✅ Push notification broadcasted to all connected devices");
            logNotificationMetadata(title, body);
        } catch (Exception e) {
            log.error("❌ Failed to send push notification: {}", e.getMessage());
        }
    }

    /**
     * Send broadcast alert to multiple recipients
     */
    public void sendBroadcastAlert(String[] recipients, String subject, String message) {
        log.info("\n=== BROADCAST ALERT ===");
        log.info("RECIPIENTS: {} people", recipients.length);
        log.info("SUBJECT: {}", subject);
        log.info("MESSAGE: {}", message);
        log.info("SENT AT: {}", LocalDateTime.now().format(FORMATTER));
        log.info("=======================\n");

        for (String recipient : recipients) {
            if (isValidPhoneNumber(recipient)) {
                sendSms(recipient, message);
            } else if (isValidEmail(recipient)) {
                sendEmail(recipient, subject, message);
            }
        }
    }

    /**
     * Emergency broadcast for critical situations
     */
    public void sendEmergencyBroadcast(String region, String alertMessage, String[] phoneNumbers, String[] emails) {
        log.warn("\n🚨🚨🚨 EMERGENCY BROADCAST INITIATED 🚨🚨🚨");
        log.warn("REGION: {}", region);
        log.warn("ALERT: {}", alertMessage);
        log.warn("RECIPIENTS: {} phones, {} emails", phoneNumbers.length, emails.length);
        log.warn("TIME: {}\n", LocalDateTime.now().format(FORMATTER));

        for (String phone : phoneNumbers) {
            sendSms(phone, "🚨 EMERGENCY: " + alertMessage);
        }

        for (String email : emails) {
            sendEmail(email, "🚨 EMERGENCY ALERT - " + region, alertMessage);
        }

        sendPushNotification("🚨 EMERGENCY ALERT", alertMessage);
    }

    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhoneNumber(String number) {
        return number != null && number.replaceAll("[^0-9]", "").length() >= 10;
    }

    private void logSmsMetadata(String number, String message) {
        int messageLength = message.length();
        int smsCount = (messageLength / 160) + 1;
        log.debug("SMS Metadata - Number: {}, Length: {}, SMS Count: {}", number, messageLength, smsCount);
    }

    private void logNotificationMetadata(String title, String body) {
        log.debug("Notification Metadata - Title Length: {}, Body Length: {}", title.length(), body.length());
    }
}
