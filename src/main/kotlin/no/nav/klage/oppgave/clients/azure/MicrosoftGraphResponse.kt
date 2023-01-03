package no.nav.klage.oppgave.clients.azure

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureUser(
    val displayName: String,
    val givenName: String,
    val surname: String,
    val userPrincipalName: String,
    val streetAddress: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureSlimUser(
    val userPrincipalName: String,
    val onPremisesSamAccountName: String,
    val displayName: String
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureOnPremisesSamAccountName(
    val onPremisesSamAccountName: String,
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureGroup(
    val id: String,
    val displayName: String?,
    val mailNickname: String?
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureUserList(val value: List<AzureUser>?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureSlimUserList(val value: List<AzureSlimUser>?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureOnPremisesSamAccountNameList(val value: List<AzureOnPremisesSamAccountName>?)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AzureGroupList(val value: List<AzureGroup>)

