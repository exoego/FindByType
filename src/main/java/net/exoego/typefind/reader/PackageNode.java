package net.exoego.typefind.reader;

import java.util.AbstractMap;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

final class PackageNode extends AbstractMap<PackageNode, PackageNode> {
    private static final String PACKAGE_NAME_SENTINEL = "<>";
    private final String name;
    private final Map<PackageNode, PackageNode> sub = new HashMap<PackageNode, PackageNode>();
    private final int depth;

    private PackageNode(final String name, int depth) {
        this.name = name;
        this.depth = depth;
    }

    static PackageNode newInstance(final String name, int depth) {
        return new PackageNode(name, depth);
    }

    static PackageNode sentinel(int depth) {
        return new PackageNode(PACKAGE_NAME_SENTINEL, depth);
    }

    public String getName() {
        return name;
    }

    @Override
    public PackageNode put(PackageNode key, PackageNode value) {
        return sub.put(key, value);
    }

    @Override
    public PackageNode get(Object key) {
        return sub.get(key);
    }

    public PackageNode add(PackageNode key) {
        return put(key, key);
    }

    @Override
    public Set<Entry<PackageNode, PackageNode>> entrySet() {
        return sub.entrySet();
    }

    @Override
    public boolean containsKey(Object key) {
        return sub.containsKey(key);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof PackageNode)) {
            return false;
        }
        final PackageNode that = (PackageNode) o;
        return name.equals(that.name);
    }

    public String toPatternGroup() {
        final String dot = (depth != 0) ? "\\." : "";
        final StringJoiner joiner = new StringJoiner("|", dot + name + "(?:", ")");
        sub.keySet().stream()
           .sorted(Comparator.comparing(PackageNode::getName))
           .forEach(p -> joiner.add(p.toPatternGroup()));
        return joiner.toString()
                     .replace("\\." + PACKAGE_NAME_SENTINEL, "")
                     .replace("(?:)", "");
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
