/*
 * Copyright 2017-present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.kafka.security.jaas;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.security.auth.login.AppConfigurationEntry;

import com.sun.security.auth.login.ConfigFile;
import org.apache.kafka.common.security.JaasContext;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.kafka.security.jaas.KafkaJaasLoginModuleInitializer.ControlFlag;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author Marius Bogoevici
 * @author Gary Russell
 * @author Edan Idzerda
 *
 * @since 1.3
 */
@SuppressWarnings("restriction")
@SpringJUnitConfig
public class KafkaJaasLoginModuleInitializerTests {

	@Test
	public void testConfigurationParsedCorrectlyWithKafkaClient() throws Exception {
		ConfigFile configFile = new ConfigFile(new ClassPathResource("jaas-sample-kafka-only.conf").getURI());
		final AppConfigurationEntry[] kafkaConfigurationArray = configFile
				.getAppConfigurationEntry(KafkaJaasLoginModuleInitializer.KAFKA_CLIENT_CONTEXT_NAME);

		javax.security.auth.login.Configuration configuration = javax.security.auth.login.Configuration
				.getConfiguration();

		final AppConfigurationEntry[] kafkaConfiguration = configuration
				.getAppConfigurationEntry(KafkaJaasLoginModuleInitializer.KAFKA_CLIENT_CONTEXT_NAME);
		assertThat(kafkaConfiguration).hasSize(1);
		assertThat(kafkaConfiguration[0].getOptions()).isEqualTo(kafkaConfigurationArray[0].getOptions());

		JaasContext context = JaasContext.loadClientContext(Collections.emptyMap());

		List<AppConfigurationEntry> appConfigurationEntries = context.configurationEntries();
		assertThat(appConfigurationEntries).hasSize(1);
		assertThat(appConfigurationEntries.get(0).getOptions()).isEqualTo(kafkaConfigurationArray[0].getOptions());
	}

	@Test
	public void testOtherConfigurationFound() {
		javax.security.auth.login.Configuration configuration = javax.security.auth.login.Configuration
				.getConfiguration();

		final AppConfigurationEntry[] otherConfiguration = configuration
				.getAppConfigurationEntry(PreConfiguredJaasConfig.OtherJaasConfigurationName);
		assertThat(otherConfiguration).hasSize(1);
	}

	@Configuration
	public static class Config {

		@Bean
		public KafkaJaasLoginModuleInitializer jaasConfig() throws IOException {
			KafkaJaasLoginModuleInitializer jaasConfig = new KafkaJaasLoginModuleInitializer();
			jaasConfig.setControlFlag(ControlFlag.REQUIRED);
			Map<String, String> options = new HashMap<>();
			options.put("useKeyTab", "true");
			options.put("storeKey", "true");
			options.put("keyTab", "/etc/security/keytabs/kafka_client.keytab");
			options.put("principal", "kafka-client-1@EXAMPLE.COM");
			jaasConfig.setOptions(options);
			return jaasConfig;
		}

	}

	@Configuration
	public static class PreConfiguredJaasConfig implements BeanPostProcessor {

		private boolean initialized = false;

		public static String OtherJaasConfigurationName = "other-jaas-configuration-name";

		@Override
		public Object postProcessBeforeInitialization(@NonNull Object bean, @NonNull String beanName)
				throws BeansException {

			// Install our "other" configuration before the KAFKA_CLIENT_CONTEXT_NAME is installed
			if (!initialized) {
				javax.security.auth.login.Configuration.setConfiguration(new OtherJaasConfiguration());
				initialized = true;
			}
			return BeanPostProcessor.super.postProcessBeforeInitialization(bean, beanName);
		}

		public static class OtherJaasConfiguration extends javax.security.auth.login.Configuration {
			@Override
			public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
				if (name.equals(OtherJaasConfigurationName)) {
					AppConfigurationEntry dummyAppConfigurationEntry = new AppConfigurationEntry("loginModuleName",
							AppConfigurationEntry.LoginModuleControlFlag.OPTIONAL, new HashMap<>());
					return new AppConfigurationEntry[] { dummyAppConfigurationEntry };
				}
				else {
					return null;
				}
			}
		}

	}

}
