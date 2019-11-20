package de.jepfa.obfusser.util

import java.util.Collections
import java.util.Comparator

import de.jepfa.obfusser.model.Credential
import de.jepfa.obfusser.model.Group
import de.jepfa.obfusser.model.PatternHolder

object DataSorter {

    fun <T : PatternHolder> sortPatternsByName(patterns: List<T>?): List<T>? {
        if (patterns == null) {
            return null
        }
        Collections.sort(patterns) { o1, o2 ->
            val s1 = o1.name.toUpperCase() + o1.id
            val s2 = o2.name.toUpperCase() + o1.id
            s1.compareTo(s2)
        }
        return patterns
    }

    fun sortGroupsByName(groups: List<Group>?): List<Group>? {
        if (groups == null) {
            return null
        }
        Collections.sort(groups) { o1, o2 ->
            val s1 = o1.name.toUpperCase() + o1.id
            val s2 = o2.name.toUpperCase() + o1.id
            s1.compareTo(s2)
        }
        return groups
    }

    fun <T : PatternHolder> sortPatternsByGroupsAndName(groups: List<Group>, patterns: List<T>?): List<T>? {
        if (patterns == null) {
            return null
        }
        Collections.sort(patterns) { o1, o2 ->
            val gn1 = findGroupName(groups, o1.groupId)
            val gn2 = findGroupName(groups, o2.groupId)
            val s1 = gn1 + o1.name.toUpperCase() + o1.id
            val s2 = gn2 + o2.name.toUpperCase() + o1.id
            s1.compareTo(s2)
        }
        return patterns
    }

    private fun findGroupName(groups: List<Group>, groupId: Int?): String {
        if (groupId != null) {
            for (group in groups) {
                if (group.id == groupId) {
                    return group.name.toUpperCase()
                }
            }
        }
        return "                         "
    }
}
