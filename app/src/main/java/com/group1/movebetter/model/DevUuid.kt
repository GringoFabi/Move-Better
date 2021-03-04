package com.group1.movebetter.model

import com.group1.movebetter.database.DatabaseDevUuid


data class DevUuid (
        val uuid : String,
)

fun DevUuid.asDatabaseDevUuid(): DatabaseDevUuid{
    return DatabaseDevUuid("1", this.uuid)
}
