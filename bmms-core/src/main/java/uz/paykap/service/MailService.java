package uz.paykap.service;

import tech.jhipster.config.JHipsterProperties;
import liquibase.util.file.FilenameUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.gateway.dto.GatewayTranDTO;

import javax.mail.internet.MimeMessage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

/**
 * Service for sending emails.
 * <p>
 * We use the @Async annotation to send emails asynchronously.
 */
@Service
public class MailService {

    private final Logger log = LoggerFactory.getLogger(MailService.class);

    private static final String TRANS = "trans";
    private static final String RECIPIENT = "recipient";
    private static final String BASE_URL = "baseUrl";

    private final JHipsterProperties jHipsterProperties;
    private final JavaMailSender javaMailSender;

    private final ApplicationProperties properties;

    private final MessageSource messageSource;
    private final SpringTemplateEngine templateEngine;

    public MailService(JHipsterProperties jHipsterProperties, JavaMailSender javaMailSender,
                       ApplicationProperties properties, MessageSource messageSource, SpringTemplateEngine templateEngine) {

        this.jHipsterProperties = jHipsterProperties;
        this.javaMailSender = javaMailSender;
        this.properties = properties;
        this.messageSource = messageSource;
        this.templateEngine = templateEngine;
    }

    //    @Async
    public void sendEmail(String to, String subject, String content) {
        sendEmail(to, properties.getMail().getFrom(), subject, content, null, false);
    }

    //    @Async
    public void sendEmailWithFile(String to, String from, String subject, String filePath, String content, Boolean isHtml) {
        sendEmailWithFile(to, from, subject, filePath, content, isHtml, null);
    }

    public void sendEmailWithFile(String to, String from, String subject, String filePath, String content, Boolean isHtml, String fileName) {
        if (!properties.getSimulateSendFileToEmail()) {
            sendEmail(to, from, subject, content, filePath, isHtml, fileName);
        } else {
            writeToFile("test");
            log.info("Email Send File Simulation: sending email...");
        }
    }

    private void writeToFile(String test) {
        try {
            String folderPath = System.getProperty("user.dir") + File.separator + "src" + File.separator + "main" + File.separator + "resources" + File.separator + "testEventListener" + File.separator;
            String fileName = folderPath + "test.txt";
            File file = new File(fileName);
            file.getParentFile().mkdirs();
            new File(folderPath).mkdir();

            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter writer = new FileWriter(fileName);
            writer.append(test);
            writer.flush();
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //    @Async
    public void sendEmail(String to, String from, String subject, String content, String filePath, boolean isHtml) {
        sendEmail(to, from, subject, content, filePath, isHtml, null);
    }

    public void sendEmail(String to, String from, String subject, String content, String filePath, boolean isHtml, String fileName) {
        log.debug("Send email[multipart '{}' and html '{}'] to '{}' with subject '{}' and content={}",
            filePath, isHtml, to, subject, content);

        // Prepare message using a Spring helper
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        try {
            boolean isMultipart = filePath != null;

            if (isMultipart) {
                //if filePath given, check if file exists
                isMultipart = Files.exists(Paths.get(filePath));
            }

            MimeMessageHelper message = new MimeMessageHelper(mimeMessage, isMultipart, StandardCharsets.UTF_8.name());
            message.setSubject(subject);
            message.setTo(to);
            message.setFrom(from);
            message.setText(content, isHtml);

            if (isMultipart) {
                FileSystemResource file = new FileSystemResource(new File(filePath));
                String extension = FilenameUtils.getExtension(filePath);
                if (fileName == null || Objects.equals(fileName, "")) {
                    message.addAttachment("attachment." + extension, file);
                } else
                    message.addAttachment(fileName + "." + extension, file);
            }

            javaMailSender.send(mimeMessage);
            log.debug("Sent email to User '{}'", to);
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.warn("Email could not be sent to user '{}'", to, e);
            } else {
                log.warn("Email could not be sent to user '{}': {}", to, e.getMessage());
            }
        }
    }

    @Async
    public void sendEmailFromTemplate(String email, String templateName, String titleKey, String recipient, Set<GatewayTranDTO> differs) {
        Locale locale = Locale.forLanguageTag("ru");
        Context context = new Context(locale);
        context.setVariable(TRANS, differs);
        context.setVariable(RECIPIENT, recipient);
        context.setVariable(BASE_URL, jHipsterProperties.getMail().getBaseUrl());
        String content = templateEngine.process(templateName, context);
        String subject = messageSource.getMessage(titleKey, null, locale);
        sendEmail(email, "", subject, content, null, true);
    }

    @Async
    public void sendReconciliationHasDifferEmail(String recipient, Set<GatewayTranDTO> differs) {
        String email = properties.getDefaultEmailTo();
        log.info("Sending reconciliation result email to '{}'", email);
        sendEmailFromTemplate(email, "mail/reconciliationEmail", "email.reconciliation.title", recipient, differs);
    }

    @Async
    public void sendReconciliationSuccessEmail(String recipient) {
        String email =  properties.getDefaultEmailTo();
        log.info("Sending reconciliation result email to '{}'", email);
        sendEmailFromTemplate(email, "mail/reconciliationEmail", "email.reconciliation.title", recipient, null);
    }
}

