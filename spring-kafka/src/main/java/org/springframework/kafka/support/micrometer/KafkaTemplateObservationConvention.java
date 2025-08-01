/*
 * Copyright 2022-present the original author or authors.
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

package org.springframework.kafka.support.micrometer;

import io.micrometer.observation.Observation.Context;
import io.micrometer.observation.ObservationConvention;

/**
 * {@link ObservationConvention} for Kafka template key values.
 *
 * @author Gary Russell
 * @since 3.0
 *
 */
public interface KafkaTemplateObservationConvention extends ObservationConvention<KafkaRecordSenderContext> {

	@Override
	default boolean supportsContext(Context context) {
		return context instanceof KafkaRecordSenderContext;
	}

	@Override
	default String getName() {
		return "spring.kafka.template";
	}

}
