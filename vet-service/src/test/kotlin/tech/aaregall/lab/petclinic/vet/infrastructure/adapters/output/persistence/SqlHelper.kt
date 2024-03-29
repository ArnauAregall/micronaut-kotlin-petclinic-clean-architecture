package tech.aaregall.lab.petclinic.vet.infrastructure.adapters.output.persistence

import io.micronaut.context.annotation.Context
import io.micronaut.data.jdbc.runtime.JdbcOperations

@Context
private class SqlHelper(private val jdbc: JdbcOperations) {

    private fun runSql(sql: String) = jdbc.execute { c -> c.prepareCall(sql).execute() }

    companion object {
        @Volatile
        lateinit var instance: SqlHelper

        internal fun runSql(sql: String) = instance.runSql(sql)
    }

    init {
        instance = this
    }
}

internal fun runSql(sql: String) = SqlHelper.runSql(sql)
