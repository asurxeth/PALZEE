package com.finrein.pals.domain.model

data class ActivePalState(
    val palCode: String,
    val submissions: List<SubmissionDbItem>,
    val messages: List<MessageDbItem>,
    val members: List<String>,
    val dailyHourHistory: Map<Int, List<SubmissionDbItem>>,
    val activeHourSubmissions: Map<String, SubmissionDbItem>,
    val exportData: Map<Int, List<SubmissionDbItem>>,
    val memberCount: Int
)
