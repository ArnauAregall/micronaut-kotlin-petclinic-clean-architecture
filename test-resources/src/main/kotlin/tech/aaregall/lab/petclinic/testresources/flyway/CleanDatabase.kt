package tech.aaregall.lab.petclinic.testresources.flyway

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.MicronautJunit5Extension
import org.flywaydb.core.Flyway
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.ExtensionContext.Namespace

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@ExtendWith(CleanDatabaseCallback::class)
@Property(name = "flyway.datasources.default.clean-schema", value = "true")
annotation class CleanDatabase

private class CleanDatabaseCallback: BeforeEachCallback {

    companion object {
        private val NAMESPACE: Namespace = Namespace.create(MicronautJunit5Extension::class.java)

        private fun getApplicationContext(extensionContext: ExtensionContext): ApplicationContext =
            extensionContext.root.getStore(NAMESPACE)[ApplicationContext::class.java] as ApplicationContext
    }

    override fun beforeEach(extensionContext: ExtensionContext) {
        val flyway = getApplicationContext(extensionContext).getBean(Flyway::class.java)
        if (!flyway.configuration.isCleanDisabled) {
            flyway.clean()
            flyway.migrate()
        }
    }

}