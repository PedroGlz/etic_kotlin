package com.example.etic.features.inspection.tree

import android.util.Log
import com.example.etic.data.local.entities.Ubicacion
import com.example.etic.data.local.views.VistaUbicacionArbol
import java.time.LocalDate
import java.util.ArrayDeque

data class TreeNode(
    val id: String,
    var title: String,
    var barcode: String? = null,
    var verified: Boolean = false,
    var textColorHex: String? = null,
    val children: MutableList<TreeNode> = mutableListOf(),
    val problems: MutableList<Problem> = mutableListOf(),
    val baselines: MutableList<Baseline> = mutableListOf(),
    var estatusInspeccionDet: String? = null,
    var idStatusInspeccionDet: String? = null
) {
    val isLeaf: Boolean get() = children.isEmpty()
}

data class Problem(
    val id: String,
    val no: Int,
    val fecha: LocalDate,
    val numInspeccion: String,
    val tipo: String,
    val tipoId: String?,
    val inspectionId: String?,
    val estatus: String,
    val cronico: Boolean,
    val tempC: Double,
    val deltaTC: Double,
    val severidad: String,
    val equipo: String,
    val comentarios: String
)

data class Baseline(
    val id: String,
    val numInspeccion: String,
    val equipo: String,
    val fecha: LocalDate,
    val mtaC: Double,
    val tempC: Double,
    val ambC: Double,
    val imgR: String? = null,
    val imgD: String? = null,
    val notas: String
)

fun findById(id: String?, list: List<TreeNode>): TreeNode? {
    if (id == null) return null
    for (n in list) {
        if (n.id == id) return n
        val found = findById(id, n.children)
        if (found != null) return found
    }
    return null
}

fun removeById(id: String, list: MutableList<TreeNode>): Boolean {
    val it = list.listIterator()
    while (it.hasNext()) {
        val idx = it.nextIndex()
        val n = it.next()
        if (n.id == id) {
            list.removeAt(idx)
            return true
        }
        if (removeById(id, n.children)) return true
    }
    return false
}

fun findPathByBarcode(list: List<TreeNode>, barcode: String): List<String>? {
    fun dfs(n: TreeNode, path: List<String>): List<String>? {
        if ((n.barcode ?: "") == barcode) return path + n.id
        for (c in n.children) {
            val found = dfs(c, path + n.id)
            if (found != null) return found
        }
        return null
    }
    for (root in list) {
        val res = dfs(root, emptyList())
        if (res != null) return res
    }
    return null
}

fun depthOfId(list: List<TreeNode>, targetId: String): Int {
    fun dfs(n: TreeNode, depth: Int): Int? {
        if (n.id == targetId) return depth
        for (c in n.children) {
            val d = dfs(c, depth + 1)
            if (d != null) return d
        }
        return null
    }
    for (root in list) {
        val d = dfs(root, 0)
        if (d != null) return d
    }
    return 0
}

fun titlePathForId(list: List<TreeNode>, targetId: String): List<String> {
    fun dfs(n: TreeNode, path: List<String>): List<String>? {
        val newPath = path + n.title
        if (n.id == targetId) return newPath
        for (c in n.children) {
            val res = dfs(c, newPath)
            if (res != null) return res
        }
        return null
    }
    for (root in list) {
        val res = dfs(root, emptyList())
        if (res != null) return res
    }
    return emptyList()
}

fun descendantIds(all: List<Ubicacion>, rootId: String): Set<String> {
    val childrenMap: Map<String?, List<Ubicacion>> = all.groupBy { it.idUbicacionPadre }
    val out = mutableSetOf<String>()
    val stack = ArrayDeque<String>()
    stack.add(rootId)
    while (stack.isNotEmpty()) {
        val id = stack.removeLast()
        if (out.add(id)) {
            val children = childrenMap[id].orEmpty()
            children.forEach { stack.add(it.idUbicacion) }
        }
    }
    return out
}

fun collectProblems(root: TreeNode?): List<Problem> {
    if (root == null) return emptyList()
    val out = mutableListOf<Problem>()
    fun dfs(n: TreeNode) { out += n.problems; n.children.forEach { dfs(it) } }
    dfs(root)
    return out
}

fun collectBaselines(root: TreeNode?): List<Baseline> {
    if (root == null) return emptyList()
    val out = mutableListOf<Baseline>()
    fun dfs(n: TreeNode) { out += n.baselines; n.children.forEach { dfs(it) } }
    dfs(root)
    return out
}

fun buildTreeFromVista(rows: List<VistaUbicacionArbol>): MutableList<TreeNode> {
    Log.d("VistaUbicacionArbol", "Filas obtenidas en buildTreeFromVista: ${rows.size}")
    rows.forEach { r ->
        Log.d(
            "VistaUbicacionArbol",
            "insp=${r.idInspeccion} det=${r.idInspeccionDet} id=${r.idUbicacion} padre=${r.idUbicacionPadre} nombre=${r.nombreUbicacion}"
        )
    }

    val byId = mutableMapOf<String, TreeNode>()
    val roots = mutableListOf<TreeNode>()

    rows.forEach { r ->
        val node = TreeNode(
            id = r.idUbicacion,
            title = r.nombreUbicacion ?: "(Sin nombre)",
            barcode = r.codigoBarras,
            verified = (r.esEquipo ?: "").equals("SI", ignoreCase = true),
            textColorHex = r.color,
            estatusInspeccionDet = r.estatusInspeccionDet,
            idStatusInspeccionDet = r.idStatusInspeccionDet
        )
        byId[r.idUbicacion] = node
    }

    rows.forEach { r ->
        val node = byId[r.idUbicacion] ?: return@forEach
        val parentId = r.idUbicacionPadre?.takeIf { it.isNotBlank() && it != "0" }
        if (parentId != null) {
            val parent = byId[parentId]
            if (parent != null) parent.children.add(node) else roots.add(node)
        } else {
            roots.add(node)
        }
    }

    return roots
}

