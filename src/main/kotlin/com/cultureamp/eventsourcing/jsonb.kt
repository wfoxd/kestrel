package com.cultureamp.eventsourcing


import org.jetbrains.exposed.sql.Column
import org.jetbrains.exposed.sql.StringColumnType
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.statements.api.PreparedStatementApi
import org.postgresql.util.PGobject

/**
 * Modified from: https://gist.github.com/quangIO/a623b5caa53c703e252d858f7a806919
 */

fun Table.jsonb(name: String): Column<String> =
    registerColumn(name, Jsonb())


private class Jsonb : StringColumnType() {
    override fun sqlType() = "jsonb"

    override fun setParameter(stmt: PreparedStatementApi, index: Int, value: Any?) {
        val obj = PGobject()
        obj.type = "jsonb"
        obj.value = value as String
        stmt[index] = obj
    }

    override fun valueFromDB(value: Any): Any {
        value as PGobject
        return value.value as Any
    }
}