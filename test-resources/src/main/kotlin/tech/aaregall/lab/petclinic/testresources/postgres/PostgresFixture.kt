package tech.aaregall.lab.petclinic.testresources.postgres

import io.micronaut.context.annotation.Context
import io.micronaut.data.jdbc.runtime.JdbcOperations

@Context
class PostgresFixture(private val jdbc: JdbcOperations) {

    private fun runSql(sql: String): Unit = jdbc.execute { c -> c.prepareCall(sql).execute() }

    companion object {
        @Volatile
        private lateinit var instance: PostgresFixture

        fun runSql(sql: String) = instance.runSql(sql)
    }

    init {
        instance = this
    }

}