package jh.postsnotification.config

import org.apache.kafka.clients.producer.ProducerConfig
import org.apache.kafka.common.serialization.StringSerializer
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.annotation.EnableKafka
import org.springframework.kafka.core.DefaultKafkaProducerFactory
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.kafka.core.ProducerFactory
import org.springframework.kafka.support.serializer.JacksonJsonSerializer

@Configuration
@EnableKafka
class KafkaProducerConfig(
    @Value("\${spring.kafka.bootstrap-servers}") private val bootstrapServers: List<String>,
) {

    @Bean
    fun kafkaTemplate(): KafkaTemplate<String, Any> = KafkaTemplate(producerFactory())

    /**
     * https://kafka.apache.org/documentation.html#producerconfigs
     */
    @Bean
    fun producerFactory(): ProducerFactory<String, Any> {
        val configProps =
            mutableMapOf<String, Any>().apply {
                this[ProducerConfig.BOOTSTRAP_SERVERS_CONFIG] = bootstrapServers
                this[ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG] = StringSerializer::class.java
                this[ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG] = JacksonJsonSerializer::class.java

                // 아래 설정은 모두 default 값들과 동일함
                this[ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG] = true
                this[ProducerConfig.ACKS_CONFIG] = "all"
                this[ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG] = 120000 // 2 minutes
                this[ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION] = 5
            }

        return DefaultKafkaProducerFactory(configProps)
    }
}
