package com.example.etic.features.inspection.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.etic.features.inspection.data.repo.*
import com.example.etic.features.inspection.domain.model.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    private val locationRepo: LocationRepository = InMemoryLocationRepository(),
    private val issueRepo: IssueRepository = InMemoryIssueRepository(),
    private val baselineRepo: BaselineRepository = InMemoryBaselineRepository()
) : ViewModel() {

    private val _state = MutableStateFlow(HomeUiState(isLoading = true))
    val state: StateFlow<HomeUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch(Dispatchers.Default) {
            combine(
                locationRepo.getTree(),
                issueRepo.getIssues(),
                baselineRepo.getBaselines(),
            ) { locations, issues, baselines -> Triple(locations, issues, baselines) }
                .collect { (locations, issues, baselines) ->
                    _state.update {
                        val treeState = buildTreeState(
                            locations = locations,
                            expanded = it.tree.expanded,
                            selectedId = it.tree.selectedId,
                            query = it.tree.query
                        )

                        val issuesState = applyIssueFilters(
                            all = issues,
                            severities = it.issues.severities,
                            status = it.issues.status,
                            text = it.issues.text,
                            sort = it.issues.sortBy,
                            selectedLocationId = treeState.selectedId
                        )

                        val baselinesState = computeBaselineDiffs(
                            all = baselines,
                            selectedId = it.baselines.selectedId,
                            currentKpis = it.selectedLocation?.kpis.orEmpty()
                        )

                        it.copy(
                            isLoading = false,
                            error = null,
                            tree = treeState,
                            issues = issuesState,
                            baselines = baselinesState,
                            selectedLocation = it.selectedLocation
                        )
                    }
                }
        }
    }

    private fun buildTreeState(
        locations: List<Location>,
        expanded: Set<String>,
        selectedId: String?,
        query: String,
    ): TreeState {
        val filtered = if (query.isBlank()) locations else locations.filter { loc ->
            loc.name.contains(query, ignoreCase = true) ||
                loc.path.joinToString("/").contains(query, ignoreCase = true)
        }
        // Flatten by path depth order, then compute visible rows from expanded set
        val byDepth = filtered.sortedBy { it.path.size }
        val childrenByParent = byDepth.groupBy { it.path.dropLast(1).joinToString("/") }
        val roots = byDepth.filter { it.path.size == 1 }

        val rows = mutableListOf<TreeRow>()

        fun addNode(node: Location) {
            val level = node.path.size - 1
            val hasChildren = childrenByParent[node.path.joinToString("/")].orEmpty().isNotEmpty()
            val isExpanded = node.id in expanded
            rows += TreeRow(
                id = node.id,
                name = node.name,
                type = node.type,
                level = level,
                hasChildren = hasChildren,
                isExpanded = isExpanded,
                status = node.status
            )
            if (hasChildren && isExpanded) {
                childrenByParent[node.path.joinToString("/")].orEmpty()
                    .sortedBy { it.name }
                    .forEach { addNode(it) }
            }
        }
        roots.sortedBy { it.name }.forEach { addNode(it) }

        return TreeState(query = query, expanded = expanded, selectedId = selectedId, visible = rows)
    }

    private fun applyIssueFilters(
        all: List<Issue>,
        severities: Set<Severity>,
        status: IssueStatus?,
        text: String,
        sort: IssueSort,
        selectedLocationId: String?
    ): IssuesState {
        var list = all.asSequence()
            .filter { it.severity in severities }
        if (status != null) list = list.filter { it.status == status }
        if (text.isNotBlank()) {
            val q = text.trim().lowercase()
            list = list.filter { it.title.lowercase().contains(q) || it.category.lowercase().contains(q) }
        }
        if (selectedLocationId != null) {
            list = list.filter { it.locationId == selectedLocationId }
        }
        val final = when (sort) {
            IssueSort.ByDateDesc -> list.sortedByDescending { it.createdAt }
            IssueSort.BySeverityDesc -> list.sortedByDescending { it.severity.ordinal }
        }.toList()
        return IssuesState(
            all = all,
            filtered = final,
            severities = severities,
            text = text,
            status = status,
            sortBy = sort
        )
    }

    private fun computeBaselineDiffs(
        all: List<Baseline>,
        selectedId: String?,
        currentKpis: Map<String, Double>
    ): BaselinesState {
        val selected = selectedId?.let { id -> all.find { it.id == id } } ?: all.firstOrNull()
        val diffs = if (selected != null) {
            (currentKpis.keys + selected.metrics.keys).distinct().map { key ->
                val cur = currentKpis[key]
                val base = selected.metrics[key]
                val delta = if (cur != null && base != null) cur - base else null
                BaselineDiff(metric = key, current = cur, baseline = base, delta = delta)
            }
        } else emptyList()

        return BaselinesState(
            all = all,
            selectedId = selected?.id,
            diffs = diffs
        )
    }

    fun onToggleExpand(id: String) {
        _state.update { s ->
            val new = if (id in s.tree.expanded) s.tree.expanded - id else s.tree.expanded + id
            s.copy(tree = s.tree.copy(expanded = new))
        }
    }

    fun onSearchTree(query: String) {
        _state.update { it.copy(tree = it.tree.copy(query = query)) }
    }

    fun onSelectNode(id: String) {
        viewModelScope.launch(Dispatchers.Default) {
            val details = locationRepo.getDetails(id)
            _state.update { s ->
                s.copy(
                    tree = s.tree.copy(selectedId = id),
                    selectedLocation = details
                )
            }
        }
    }

    fun onIssueSeverityToggle(severity: Severity) {
        _state.update { s ->
            val set = s.issues.severities.toMutableSet()
            if (!set.add(severity)) set.remove(severity)
            s.copy(issues = s.issues.copy(severities = set))
        }
    }

    fun onIssueText(text: String) {
        _state.update { it.copy(issues = it.issues.copy(text = text)) }
    }

    fun onIssueStatus(status: IssueStatus?) {
        _state.update { it.copy(issues = it.issues.copy(status = status)) }
    }

    fun onIssueSort(sort: IssueSort) {
        _state.update { it.copy(issues = it.issues.copy(sortBy = sort)) }
    }

    fun onIssueClick(issue: Issue) {
        onSelectNode(issue.locationId)
    }

    fun onRightTab(tab: RightTab) {
        _state.update { it.copy(ui = it.ui.copy(rightTab = tab)) }
    }

    fun onCenterTab(tab: CenterTab) {
        _state.update { it.copy(ui = it.ui.copy(centerTab = tab)) }
    }

    fun onLeftFraction(f: Float) {
        _state.update { it.copy(ui = it.ui.copy(leftPanelFraction = f.coerceIn(0.15f, 0.4f))) }
    }

    fun onRightFraction(f: Float) {
        _state.update { it.copy(ui = it.ui.copy(rightPanelFraction = f.coerceIn(0.2f, 0.5f))) }
    }

    fun onSelectBaseline(id: String?) {
        _state.update { it.copy(baselines = it.baselines.copy(selectedId = id)) }
    }
}
