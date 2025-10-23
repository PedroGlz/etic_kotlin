package com.example.etic.features.inspection.data.repo

import com.example.etic.features.inspection.domain.model.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.time.Instant
import kotlin.random.Random

interface LocationRepository {
    fun getTree(): StateFlow<List<Location>>
    suspend fun search(query: String): List<Location>
    suspend fun getDetails(id: String): Location?
}

interface IssueRepository {
    fun getIssues(): StateFlow<List<Issue>>
}

interface BaselineRepository {
    fun getBaselines(): StateFlow<List<Baseline>>
}

class MockDataProvider {
    val locations: List<Location>
    val issues: List<Issue>
    val baselines: List<Baseline>

    private val kpiKeys = listOf("availability", "health", "risk")

    init {
        val sites = (1..5).map { s ->
            val siteId = "site$s"
            val site = baseLocation(
                id = siteId,
                name = "Site $s",
                path = listOf("Site $s"),
                type = LocationType.Site,
                coords = LatLng(-33.0 + s, -70.0 - s)
            )
            val areas = (1..Random.nextInt(2, 4)).map { a ->
                val areaId = "$siteId-area$a"
                val area = baseLocation(
                    id = areaId,
                    name = "Area $a",
                    path = listOf("Site $s", "Area $a"),
                    type = LocationType.Area,
                    coords = LatLng(site.coords!!.lat + a * 0.01, site.coords.lng - a * 0.01)
                )
                val assets = (1..Random.nextInt(2, 5)).map { t ->
                    baseLocation(
                        id = "$areaId-asset$t",
                        name = "Asset $t",
                        path = listOf("Site $s", "Area $a", "Asset $t"),
                        type = LocationType.Asset,
                        coords = LatLng(area.coords!!.lat + t * 0.001, area.coords.lng - t * 0.001)
                    )
                }
                listOf(area) + assets
            }.flatten()
            listOf(site) + areas
        }.flatten()
        locations = sites

        val allLocIds = locations.map { it.id }
        issues = (1..40).map { i ->
            Issue(
                id = "issue$i",
                locationId = allLocIds.random(),
                severity = listOf(Severity.Low, Severity.Medium, Severity.High, Severity.Critical).random(),
                category = listOf("Electrical", "Mechanical", "Network", "Safety").random(),
                title = "Issue #$i detected",
                status = listOf(IssueStatus.Open, IssueStatus.Acknowledged, IssueStatus.Resolved).random(),
                createdAt = Instant.now().minusSeconds(Random.nextLong(0, 60L * 60L * 24L * 14L))
            )
        }

        baselines = listOf(
            Baseline(
                id = "base1",
                name = "Baseline A",
                createdAt = Instant.now().minusSeconds(60L * 60L * 24L * 30L),
                metrics = randomMetrics()
            ),
            Baseline(
                id = "base2",
                name = "Baseline B",
                createdAt = Instant.now().minusSeconds(60L * 60L * 24L * 60L),
                metrics = randomMetrics()
            ),
            Baseline(
                id = "base3",
                name = "Baseline C",
                createdAt = Instant.now().minusSeconds(60L * 60L * 24L * 90L),
                metrics = randomMetrics()
            )
        )
    }

    private fun baseLocation(
        id: String,
        name: String,
        path: List<String>,
        type: LocationType,
        coords: LatLng
    ): Location {
        val status = when (Random.nextInt(0, 10)) {
            in 0..5 -> Status.Good
            in 6..7 -> Status.Warning
            8 -> Status.Critical
            else -> Status.Unknown
        }
        return Location(
            id = id,
            name = name,
            path = path,
            type = type,
            status = status,
            coords = coords,
            kpis = randomMetrics()
        )
    }

    private fun randomMetrics(): Map<String, Double> = kpiKeys.associateWith {
        when (it) {
            "availability" -> Random.nextDouble(90.0, 100.0)
            "health" -> Random.nextDouble(60.0, 100.0)
            "risk" -> Random.nextDouble(0.0, 40.0)
            else -> Random.nextDouble(0.0, 100.0)
        }
    }
}

class InMemoryLocationRepository(
    private val data: MockDataProvider = MockDataProvider()
) : LocationRepository {
    private val flow = MutableStateFlow(data.locations)
    override fun getTree(): StateFlow<List<Location>> = flow.asStateFlow()

    override suspend fun search(query: String): List<Location> {
        delay(120)
        val q = query.trim().lowercase()
        if (q.isBlank()) return flow.value
        return flow.value.filter { loc ->
            loc.name.lowercase().contains(q) ||
                loc.path.joinToString("/").lowercase().contains(q)
        }
    }

    override suspend fun getDetails(id: String): Location? {
        delay(80)
        return flow.value.find { it.id == id }
    }
}

class InMemoryIssueRepository(
    private val data: MockDataProvider = MockDataProvider()
) : IssueRepository {
    private val flow = MutableStateFlow(data.issues)
    override fun getIssues(): StateFlow<List<Issue>> = flow.asStateFlow()
}

class InMemoryBaselineRepository(
    private val data: MockDataProvider = MockDataProvider()
) : BaselineRepository {
    private val flow = MutableStateFlow(data.baselines)
    override fun getBaselines(): StateFlow<List<Baseline>> = flow.asStateFlow()
}
