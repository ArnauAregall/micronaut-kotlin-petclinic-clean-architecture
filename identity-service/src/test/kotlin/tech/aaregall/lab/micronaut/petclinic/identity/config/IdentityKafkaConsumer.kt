package tech.aaregall.lab.micronaut.petclinic.identity.config

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import org.slf4j.LoggerFactory
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.IdentityId
import java.util.concurrent.ConcurrentHashMap

@KafkaListener(offsetReset = OffsetReset.EARLIEST)
class IdentityKafkaConsumer {

    companion object {
        private val logger = LoggerFactory.getLogger(IdentityKafkaConsumer::class.java)
        private val consumedRecordsMap: MutableMap<IdentityId, Any> = ConcurrentHashMap()
    }

    @Topic("identity")
    fun consume(@KafkaKey id: String, record: Any) {
        logger.info("Consumed record with id '{}'", id)
        consumedRecordsMap[IdentityId.of(id)] = record
    }

    fun clear() = consumedRecordsMap.clear()

    fun hasConsumedRecords() = consumedRecordsMap.isNotEmpty()

    fun get(identityId: IdentityId) = consumedRecordsMap[identityId]

}