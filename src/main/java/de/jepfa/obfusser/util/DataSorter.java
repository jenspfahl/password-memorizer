package de.jepfa.obfusser.util;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import de.jepfa.obfusser.model.Credential;
import de.jepfa.obfusser.model.Group;
import de.jepfa.obfusser.model.PatternHolder;

public class DataSorter {

    public static <T extends PatternHolder> List<T> sortPatternsByName(List<T> patterns) {
        if (patterns == null) {
            return null;
        }
        Collections.sort(patterns, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                String s1 = o1.getName().toUpperCase() + o1.getId();
                String s2 = o2.getName().toUpperCase() + o1.getId();
                return s1.compareTo(s2);
            }
        });
        return patterns;
    }

    public static List<Group> sortGroupsByName(List<Group> groups) {
        if (groups == null) {
            return null;
        }
        Collections.sort(groups, new Comparator<Group>() {
            @Override
            public int compare(Group o1, Group o2) {
                String s1 = o1.getName().toUpperCase() + o1.getId();
                String s2 = o2.getName().toUpperCase() + o1.getId();
                return s1.compareTo(s2);
            }
        });
        return groups;
    }

    public static <T extends PatternHolder> List<T> sortPatternsByGroupsAndName(final List<Group> groups, List<T> patterns) {
        if (patterns == null) {
            return null;
        }
        Collections.sort(patterns, new Comparator<T>() {
            @Override
            public int compare(T o1, T o2) {
                String gn1 = findGroupName(groups, o1.getGroupId());
                String gn2 = findGroupName(groups, o2.getGroupId());
                String s1 = gn1 + o1.getName().toUpperCase() + o1.getId();
                String s2 = gn2 + o2.getName().toUpperCase() + o1.getId();
                return s1.compareTo(s2);
            }
        });
        return patterns;
    }

    private static String findGroupName(List<Group> groups, Integer groupId) {
        if (groupId != null) {
            for (Group group : groups) {
                if (group.getId() == groupId) {
                    return group.getName().toUpperCase();
                }
            }
        }
        return "                         ";
    }
}
