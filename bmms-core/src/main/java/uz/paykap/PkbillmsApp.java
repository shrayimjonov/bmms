package uz.paykap;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.core.env.Environment;
import org.springframework.retry.annotation.EnableRetry;
import tech.jhipster.config.DefaultProfileUtil;
import tech.jhipster.config.JHipsterConstants;
import uz.devops.humo.config.HumoConfiguration;
import uz.devops.humoMiddleware.config.HumoMiddlewareConfiguration;
import uz.paykap.config.ApplicationProperties;
import uz.paykap.config.PaymentProperties;

import javax.annotation.PostConstruct;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Optional;

@EntityScan(basePackages = {"uz.paykap", "uz.javlon.svgate"})
//@ComponentScan(
//        basePackages = {"uz.javlon", "uz.paykap", "uz.devops", "uz.devops.bmcore", "uz.devops.nci", "uz.devops.bm.ncitrans", "uz.devops.atto", "uz.devops.bm.paynet"}
//        ,
//    excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = PaynetFeignConfig.class)
//        ,
//        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern = "uz.devops.humo.config.*")
//)
@EnableRetry
@SpringBootApplication(scanBasePackages = {"uz.javlon", "uz.paykap", "uz.devops", "uz.devops.libs.keycloak", "uz.devops.bmcore", "uz.devops.nci", "uz.devops.bm.ncitrans", "uz.devops.atto", "uz.devops.bm.paynet", "uz.devops.bm.payme", "uz.devops.fido"}, exclude = {HumoConfiguration.class, HumoMiddlewareConfiguration.class})
@EnableConfigurationProperties({LiquibaseProperties.class, ApplicationProperties.class, PaymentProperties.class})
@EnableDiscoveryClient
public class PkbillmsApp {

    private static final Logger log = LoggerFactory.getLogger(PkbillmsApp.class);

    private final Environment env;

    public PkbillmsApp(Environment env) {
        this.env = env;
    }

    /**
     * Initializes pkbillms.
     * <p>
     * Spring profiles can be configured with a program argument --spring.profiles.active=your-active-profile
     * <p>
     * You can find more information on how profiles work with JHipster on <a href="https://www.jhipster.tech/profiles/">https://www.jhipster.tech/profiles/</a>.
     */
    @PostConstruct
    public void initApplication() {
        Collection<String> activeProfiles = Arrays.asList(env.getActiveProfiles());
        if (
                activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) &&
                        activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_PRODUCTION)
        ) {
            log.error(
                    "You have misconfigured your application! It should not run " + "with both the 'dev' and 'prod' profiles at the same time."
            );
        }
        if (
                activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_DEVELOPMENT) &&
                        activeProfiles.contains(JHipsterConstants.SPRING_PROFILE_CLOUD)
        ) {
            log.error(
                    "You have misconfigured your application! It should not " + "run with both the 'dev' and 'cloud' profiles at the same time."
            );
        }
    }

    /**
     * Main method, used to run the application.
     *
     * @param args the command line arguments.
     */
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(PkbillmsApp.class);
        DefaultProfileUtil.addDefaultProfile(app);
        Environment env = app.run(args).getEnvironment();
        logApplicationStartup(env);
    }

    private static void logApplicationStartup(Environment env) {
        String protocol = Optional.ofNullable(env.getProperty("server.ssl.key-store")).map(key -> "https").orElse("http");
        String serverPort = env.getProperty("server.port");
        String contextPath = Optional
                .ofNullable(env.getProperty("server.servlet.context-path"))
                .filter(StringUtils::isNotBlank)
                .orElse("/");
        String hostAddress = "localhost";
        try {
            hostAddress = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            log.warn("The host name could not be determined, using `localhost` as fallback");
        }
        log.info(
                "\n----------------------------------------------------------\n\t" +
                        "Application '{}' is running! Access URLs:\n\t" +
                        "Local: \t\t{}://localhost:{}{}\n\t" +
                        "External: \t{}://{}:{}{}\n\t" +
                        "Profile(s): \t{}\n----------------------------------------------------------",
                env.getProperty("spring.application.name"),
                protocol,
                serverPort,
                contextPath,
                protocol,
                hostAddress,
                serverPort,
                contextPath,
                env.getActiveProfiles()
        );

        String configServerStatus = env.getProperty("configserver.status");
        if (configServerStatus == null) {
            configServerStatus = "Not found or not setup for this application";
        }
        log.info(
                "\n----------------------------------------------------------\n\t" +
                        "Config Server: \t{}\n----------------------------------------------------------",
                configServerStatus
        );
    }
}
