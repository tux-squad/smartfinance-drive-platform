package com.smartfinance.smartfinancedriveplatform.iam.infrastructure.communication;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.mail.javamail.JavaMailSender;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("EmailSenderServiceImpl Spring DI & Constructor Instantiation Tests")
class EmailSenderServiceContextTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(EmailSenderServiceImpl.class);

    @Test
    @DisplayName("Should instantiate EmailSenderServiceImpl cleanly via Spring DI without BeanInstantiationException")
    void shouldInstantiateCleanlyInSpringContext() {
        contextRunner.run(context -> {
            assertThat(context).hasNotFailed();
            assertThat(context).hasSingleBean(EmailSenderServiceImpl.class);
            EmailSenderServiceImpl bean = context.getBean(EmailSenderServiceImpl.class);
            assertThat(bean).isNotNull();
        });
    }
}
