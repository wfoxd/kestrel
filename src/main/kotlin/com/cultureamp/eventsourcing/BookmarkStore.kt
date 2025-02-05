package com.cultureamp.eventsourcing

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.jodatime.datetime
import org.jetbrains.exposed.sql.transactions.transaction
import org.joda.time.DateTime

interface BookmarkStore {
    fun bookmarkFor(bookmarkName: String): Bookmark
    fun save(bookmark: Bookmark)
}

class RelationalDatabaseBookmarkStore(val db: Database, val table: Bookmarks = Bookmarks()) : BookmarkStore {
    override fun bookmarkFor(bookmarkName: String): Bookmark = transaction(db) {
        val matchingRows = rowsForBookmark(bookmarkName)
        val bookmarkVal = if (matchingRows.count() > 0) matchingRows.single()[table.sequence] else 0
        Bookmark(bookmarkName, bookmarkVal)
    }

    override fun save(bookmark: Bookmark): Unit = transaction(db) {
        if (!isExists(bookmark.name)) {
            table.insert {
                it[name] = bookmark.name
                it[sequence] = bookmark.sequence
                it[createdAt] = DateTime.now()
                it[updatedAt] = DateTime.now()
            }
        } else {
            table.update({ table.name eq bookmark.name }) {
                it[sequence] = bookmark.sequence
                it[updatedAt] = DateTime.now()
            }
        }
    }

    private fun rowsForBookmark(bookmarkName: String) = table.select { table.name eq bookmarkName }
    private fun isExists(bookmarkName: String) = rowsForBookmark(bookmarkName).count() > 0
}

class Bookmarks(tableName: String = "bookmarks") : Table(tableName) {
    val name = varchar("name", 160)
    val sequence = long("value")
    val createdAt = datetime("created_at")
    val updatedAt = datetime("updated_at")
    override val primaryKey = PrimaryKey(name)
}

data class Bookmark(val name: String, val sequence: Long)

