package com.group1.movebetter.model

import com.group1.movebetter.database.DatabaseDevUuid


data class DevUuid (
        val uuid : String,
)

/**
 * Convert App-Model uuid to Database-Model uuid
 * db-key constant set to 1 to store only one uuid
 */
fun DevUuid.asDatabaseDevUuid(): DatabaseDevUuid{
    return DatabaseDevUuid("1", this.uuid)
}
