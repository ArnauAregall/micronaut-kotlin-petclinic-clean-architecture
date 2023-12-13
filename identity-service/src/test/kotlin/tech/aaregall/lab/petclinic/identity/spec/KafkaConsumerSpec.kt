package tech.aaregall.lab.petclinic.identity.spec

import io.micronaut.configuration.kafka.annotation.KafkaKey
import io.micronaut.configuration.kafka.annotation.KafkaListener
import io.micronaut.configuration.kafka.annotation.OffsetReset
import io.micronaut.configuration.kafka.annotation.Topic
import org.slf4j.LoggerFactory
import tech.aaregall.lab.petclinic.identity.domain.model.IdentityId
import java.util.concurrent.ConcurrentHashMap

@KafkaListener(offsetReset = OffsetReset.EARLIEST)
class KafkaConsumerSpec {

    companion object {
        private val logger = LoggerFactory.getLogger(KafkaConsumerSpec::class.java)
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