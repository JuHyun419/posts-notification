package jh.postsnotification.config

import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.StringDeserializer
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory
import org.springframework.kafka.core.ConsumerFactory
import org.springframework.kafka.core.DefaultKafkaConsumerFactory
import org.springframework.kafka.listener.ContainerProperties
import org.springframework.kafka.listener.DefaultErrorHandler
import org.springframework.kafka.support.serializer.JacksonJsonDeserializer
import org.springframework.util.backoff.BackOff
import org.springframework.util.backoff.FixedBackOff


@Configuration
class KafkaConsumerConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: List<String>,
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Bean
    fun kafkaListenerContainerFactory(): ConcurrentKafkaListenerContainerFactory<String, Any> =
        ConcurrentKafkaListenerContainerFactory<String, Any>().apply {
            this.setConsumerFactory(consumerFactory())
            this.setCommonErrorHandler(errorHandler())
            this.containerProperties.ackMode = ContainerProperties.AckMode.MANUAL
            this.setConcurrency(3)
        }

    /**
     * https://kafka.apache.org/documentation.html#consumerconfigs
     */
    @Bean
    fun consumerFactory(): ConsumerFactory<String, Any> {
        val consumerConfigs =
            mutableMapOf<String, Any>().apply {
                this[ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
                this[ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG] = StringDeserializer::class.java
                this[ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG] = JacksonJsonDeserializer::class.java

                // 아래 설정은 default와 동일한 설정값
                this[ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG] = false
                this[ConsumerConfig.MAX_POLL_RECORDS_CONFIG] = 500
                this[ConsumerConfig.ALLOW_AUTO_CREATE_TOPICS_CONFIG] = false

                this[JacksonJsonDeserializer.TRUSTED_PACKAGES] = "kr.postsnotification.*"
            }

        return DefaultKafkaConsumerFactory(consumerConfigs)
    }

    // TODO: Dead Letter Topic
    @Bean
    fun errorHandler(): DefaultErrorHandler {
        val fixedBackOff: BackOff = FixedBackOff(1000, 2)
        return DefaultErrorHandler({ consumerRecord, exception ->
            log.error("Consumer Error, Data: ${consumerRecord.value()}")
        }, fixedBackOff)
    }
}
