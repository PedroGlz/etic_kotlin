package com.example.etic.features.inspection.domain.model

import java.time.Instant

data class LatLng(val lat: Double, val lng: Double)

enum class LocationType { Site, Area, Asset }

enum class Status { Good, Warning, Critical, Unknown }

data class Location(
    val id: String,
    val name: String,
    val path: List<String>,
    val type: LocationType,
    val status: Status,
    val coords: LatLng?,
    val kpis: Map<String, Double>
)

enum class Severity { Low, Medium, High, Critical }

enum class IssueStatus { Open, Acknowledged, Resolved }

data class Issue(
    val id: String,
    val locationId: String,
    val severity: Severity,
    val category: String,
    val title: String,
    val status: IssueStatus,
    val createdAt: Instant
)

data class Baseline(
    val id: String,
    val name: String,
    val createdAt: Instant,
    val metrics: Map<String, Double>
)

data class BaselineDiff(
    val metric: String,
    val current: Double?,
    val baseline: Double?,
    val delta: Double?
)
