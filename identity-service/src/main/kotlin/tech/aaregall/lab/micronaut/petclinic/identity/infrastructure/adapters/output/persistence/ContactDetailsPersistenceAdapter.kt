package tech.aaregall.lab.micronaut.petclinic.identity.infrastructure.adapters.output.persistence

import io.micronaut.security.utils.SecurityService
import jakarta.inject.Singleton
import tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output.ContactDetailsOutputPort
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity
import java.util.UUID

@Singleton
internal class ContactDetailsPersistenceAdapter(
    private val contactDetailsJpaRepository: ContactDetailsJpaRepository,
    private val contactDetailsPersistenceMapper: ContactDetailsPersistenceMapper,
    private val securityService: SecurityService
): ContactDetailsOutputPort {

    override fun updateIdentityContactDetails(identity: Identity, contactDetails: ContactDetails): ContactDetails =
        contactDetailsJpaRepository.findById(UUID.fromString(identity.id.toString()))
            .map { updateContactDetails(it, contactDetails) }
            .map { contactDetailsPersistenceMapper.mapToDomain(it) }
            .orElseGet {
                contactDetailsPersistenceMapper.mapToDomain(createContactDetailsEntity(identity, contactDetails))
            }

    private fun createContactDetailsEntity(identity: Identity, contactDetails: ContactDetails): ContactDetailsJpaEntity {
        val jpaEntity = contactDetailsPersistenceMapper.mapToEntity(identity, contactDetails)
        jpaEntity.createdBy = securityService.username().map(UUID::fromString).orElse(SYSTEM_ACCOUNT_AUDIT_ID)
        return contactDetailsJpaRepository.save(jpaEntity)
    }

    private fun updateContactDetails(jpaEntity: ContactDetailsJpaEntity, contactDetails: ContactDetails): ContactDetailsJpaEntity {
        jpaEntity.email = contactDetails.email
        jpaEntity.phoneNumber = contactDetails.phoneNumber
        return contactDetailsJpaRepository.update(jpaEntity)
    }

}