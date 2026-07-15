package com.finrein.pals.domain.repository

interface GroupRepository {
    /**
     * Deletes the single row inside the 'user_pals' join table where both [userId] and [groupId] (palCode) match.
     */
    suspend fun leaveGroup(userId: String, groupId: String): Result<Unit>

    /**
     * Deletes the core record from the 'pals' table using [groupId] (palCode) which
     * automatically cascades and clears out all memberships (user_pals), submissions, and messages.
     */
    suspend fun deleteGroup(groupId: String): Result<Unit>
}
