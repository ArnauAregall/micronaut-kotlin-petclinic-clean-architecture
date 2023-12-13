package tech.aaregall.lab.petclinic.identity.application.ports.output

import tech.aaregall.lab.petclinic.identity.domain.model.ContactDetails
import tech.aaregall.lab.petclinic.identity.domain.model.Identity

fun interface ContactDetailsOutputPort {

    fun updateIdentityContactDetails(identity: Identity, contactDetails: ContactDetails): ContactDetails

}