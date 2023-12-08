package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import jakarta.inject.Singleton
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import java.util.UUID

@Singleton
internal class ContactDetailsPersistenceMapper {

    fun mapToDomain(entity: ContactDetailsJpaEntity): ContactDetails =
        ContactDetails(entity.email, entity.phoneNumber)

    fun mapToEntity(identity: Identity, contactDetails: ContactDetails): ContactDetailsJpaEntity =
        ContactDetailsJpaEntity(UUID.fromString(identity.id.toString()), contactDetails.email, contactDetails.phoneNumber)

}