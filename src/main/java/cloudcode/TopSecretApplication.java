package cloudcode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.Executor;

/** This class serves as an entry point for the Spring Boot app. */
@SpringBootApplication
@EnableAsync
public class TopSecretApplication {

  private static final Logger logger = LoggerFactory.getLogger(TopSecretApplication.class);

  public static void main(final String[] args) throws Exception {
    String port = System.getenv("PORT");
    if (port == null) {
      port = "8080";
      logger.warn("$PORT environment variable not set, defaulting to 8080");
    }
    SpringApplication app = new SpringApplication(TopSecretApplication.class);
    app.setDefaultProperties(Collections.singletonMap("server.port", port));

    // Start the Spring Boot application.
    app.run(args);
    logger.info(
        "Hello from Cloud Run! The container started successfully and is listening for HTTP requests on $PORT");
  }

  @Bean
  public MessageSource messageSource() {
    ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
    messageSource.setBasename("messages");
    messageSource.setDefaultLocale(Locale.US);
    return messageSource;
  }

  @Bean
  public Executor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(1);
    executor.setMaxPoolSize(1);
    executor.setQueueCapacity(0);
    executor.setThreadNamePrefix("SplitJoiner-");
    executor.initialize();
    return executor;
  }

}
