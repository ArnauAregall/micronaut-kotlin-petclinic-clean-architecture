package tech.aaregall.lab.micronaut.petclinic.identity.application.ports.output

import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.micronaut.petclinic.identity.domain.model.Identity

fun interface ContactDetailsOutputPort {

    fun updateIdentityContactDetails(identity: Identity, contactDetails: ContactDetails): ContactDetails

}