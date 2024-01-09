package tech.aaregall.lab.petclinic.identity.spec

import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.header.Headers
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentHashMap.newKeySet

@KafkaListener(offsetReset = OffsetReset.EARLIEST)
class KafkaConsumerSpec {

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaConsumerSpec::class.java)
        private val consumedRecordsMap: MutableMap<String, MutableSet<KafkaRecord>> = ConcurrentHashMap()
    }

    @Topic("identity")
    fun consume(record: ConsumerRecord<String, Any>) {
        logger.info("Consumed record with id '{}'", record.key())
        val records = consumedRecordsMap.getOrPut(record.key()) { newKeySet() }
        records.add(KafkaRecord(record.headers(), record.value()))
    }

    fun clear() = consumedRecordsMap.clear()

    fun hasConsumedRecords() = consumedRecordsMap.isNotEmpty()

    fun get(key: String) = consumedRecordsMap[key]

}

data class KafkaRecord(val headers: Headers, val body: Any?) {

    fun getActionHeader() = String(headers.headers("X-Action").first().value())

}