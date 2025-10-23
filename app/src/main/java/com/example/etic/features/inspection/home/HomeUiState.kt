package com.example.etic.features.inspection.home

import androidx.compose.runtime.Immutable
import com.example.etic.features.inspection.domain.model.*

@Immutable
data class HomeUiState(
    val isLoading: Boolean = false,
    val error: String? = null,

    val tree: TreeState = TreeState(),
    val selectedLocation: Location? = null,

    val issues: IssuesState = IssuesState(),
    val baselines: BaselinesState = BaselinesState(),

    val ui: UiPrefs = UiPrefs()
)

@Immutable
data class UiPrefs(
    val leftPanelFraction: Float = 0.25f,
    val rightPanelFraction: Float = 0.30f,
    val rightTab: RightTab = RightTab.Issues,
    val centerTab: CenterTab = CenterTab.Overview
)

enum class RightTab { Issues, Baseline }
enum class CenterTab { Overview, Assets, Timeline }

@Immutable
data class TreeState(
    val query: String = "",
    val expanded: Set<String> = emptySet(),
    val selectedId: String? = null,
    val visible: List<TreeRow> = emptyList()
)

@Immutable
data class TreeRow(
    val id: String,
    val name: String,
    val type: LocationType,
    val level: Int,
    val hasChildren: Boolean,
    val isExpanded: Boolean,
    val status: Status,
)

@Immutable
data class IssuesState(
    val all: List<Issue> = emptyList(),
    val filtered: List<Issue> = emptyList(),
    val severities: Set<Severity> = setOf(Severity.High, Severity.Critical, Severity.Medium, Severity.Low),
    val text: String = "",
    val status: IssueStatus? = null,
    val sortBy: IssueSort = IssueSort.ByDateDesc
)

enum class IssueSort { ByDateDesc, BySeverityDesc }

@Immutable
data class BaselinesState(
    val all: List<Baseline> = emptyList(),
    val selectedId: String? = null,
    val diffs: List<BaselineDiff> = emptyList()
)
