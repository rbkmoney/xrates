package com.rbkmoney.xrates;

import org.junit.ClassRule;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.HttpWaitStrategy;

import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.DEFINED_PORT;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = DEFINED_PORT)
@ContextConfiguration(classes = XRatesApplication.class, initializers = AbstractIntegrationTest.Initializer.class)
public abstract class AbstractIntegrationTest {

    public final static String MG_IMAGE = "dr2.rbkmoney.com/rbkmoney/machinegun";
    public final static String MG_TAG = "1d05e14c3678b2761a28a2400b40d0a29c7a3c42";

    @ClassRule
    public static GenericContainer machinegunContainer = new GenericContainer(MG_IMAGE + ":" + MG_TAG)
            .withExposedPorts(8022)
            .withClasspathResourceMapping(
                    "/machinegun/config.yaml",
                    "/opt/machinegun/etc/config.yaml",
                    BindMode.READ_ONLY
            ).withClasspathResourceMapping(
                    "/machinegun/cookie",
                    "/opt/machinegun/etc/config/cookie",
                    BindMode.READ_ONLY
            ).waitingFor(
                    new HttpWaitStrategy()
                            .forPath("/health")
                            .forStatusCode(200)
            );

    public static class Initializer implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        @Override
        public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "service.mg.automaton.url=http://" + machinegunContainer.getContainerIpAddress() + ":" + machinegunContainer.getMappedPort(8022) + "/v1/automaton",
                    "service.mg.eventSink.url=http://" + machinegunContainer.getContainerIpAddress() + ":" + machinegunContainer.getMappedPort(8022) + "/v1/event_sink"
            ).applyTo(configurableApplicationContext);
        }
    }

}
