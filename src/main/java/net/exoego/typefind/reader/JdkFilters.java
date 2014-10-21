package net.exoego.typefind.reader;

import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.regex.Pattern;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import net.exoego.typefind.definition.TypeDef;

/**
 * Provides utility methods that returns {@code Pattern} instance to filter JDK public packages.
 */
public final class JdkFilters {
    private static final Pattern WHITE_PACKAGES;

    private JdkFilters() { }

    /**
     * Returns a {@code Pattern} instance that test if given {@code CharSequence} matches one of existing public JDK
     * packages, such as "java.util", "org.w3c.dom" and so on.
     *
     * @return a {@code Pattern} instance that test if given {@code CharSequence} matches one of existing public
     * JDK packages.
     */
    public static Pattern jdkAll() {
        return WHITE_PACKAGES;
    }

    /**
     * Test if the given {@code CharSequence} is public and belongs to one of public documented JDK packages.
     *
     * @return {@code true} if the given {@code CharSequence} is public and belongs to one of public documented JDK
     * packages.
     */
    public static boolean isPublicDocumentedJdkClass(Class<?> k) {
        if (TypeDef.isPublic(k)) {
            final String packageName = k.getPackage().getName();
            return packageName != null && WHITE_PACKAGES.matcher(packageName).matches();
        }
        return false;
    }

    static {
        final List<String> compact1 = Arrays.asList(new String[]{
                "java.io", "java.lang", "java.lang.annotation", "java.lang.invoke", "java.lang.ref",
                "java.lang.reflect", "java.math", "java.net", "java.nio", "java.nio.channels", "java.nio.charset",
                "java.nio.file", "java.nio.file.attribute", "java.text", "java.time", "java.time.chrono",
                "java.time.format", "java.time.temporal", "java.time.zone", "java.util", "java.util.concurrent",
                "java.util.concurrent.atomic", "java.util.concurrent.locks", "java.util.function", "java.util.jar",
                "java.util.logging", "java.util.regex", "java.util.stream", "java.util.zip", "javax.crypto",
                "javax.crypto.interfaces", "javax.crypto.spec", "javax.net", "javax.net.ssl", "javax.script",
                "java.nio.channels.spi", "java.nio.charset.spi", "java.nio.file.spi", "java.text.spi", "java.util.spi",
                "java.security", "java.security.cert", "java.security.interfaces", "java.security.spec",
                "javax.security.auth", "javax.security.auth.callback", "javax.security.auth.login",
                "javax.security.auth.spi", "javax.security.auth.x500", "javax.security.cert"
        });
        final List<String> compact2_addition = Arrays.asList(new String[]{
                "java.rmi", "java.rmi.activation", "java.rmi.dgc", "java.rmi.registry", "java.rmi.server", "java.sql",
                "javax.rmi.ssl", "javax.sql", "javax.transaction", "javax.transaction.xa", "javax.xml",
                "javax.xml.datatype", "javax.xml.namespace", "javax.xml.parsers", "javax.xml.stream",
                "javax.xml.stream.events", "javax.xml.stream.util", "javax.xml.transform", "javax.xml.transform.dom",
                "javax.xml.transform.sax", "javax.xml.transform.stax", "javax.xml.transform.stream",
                "javax.xml.validation", "javax.xml.xpath", "org.w3c.dom", "org.w3c.dom.bootstrap", "org.w3c.dom.events",
                "org.w3c.dom.ls", "org.xml.sax", "org.xml.sax.ext", "org.xml.sax.helpers"
        });
        final List<String> compact3_addition = Arrays.asList(new String[]{
                "java.lang.instrument", "java.lang.management", "java.security.acl", "java.util.prefs",
                "javax.annotation.processing", "javax.lang.model", "javax.lang.model.element", "javax.lang.model.type",
                "javax.lang.model.util", "javax.management", "javax.management.loading", "javax.management.modelmbean",
                "javax.management.monitor", "javax.management.openmbean", "javax.management.relation",
                "javax.management.remote", "javax.management.remote.rmi", "javax.management.timer", "javax.naming",
                "javax.naming.directory", "javax.naming.event", "javax.naming.ldap", "javax.naming.spi",
                "javax.security.auth.kerberos", "javax.security.sasl", "javax.sql.rowset", "javax.sql.rowset.serial",
                "javax.sql.rowset.spi", "javax.tools", "javax.xml.crypto", "javax.xml.crypto.dom",
                "javax.xml.crypto.dsig", "javax.xml.crypto.dsig.dom", "javax.xml.crypto.dsig.keyinfo",
                "javax.xml.crypto.dsig.spec", "org.ietf.jgss"
        });

        final List<String> fullSE8API = Arrays.asList(new String[]{
                "java.applet", "java.awt", "java.awt.color", "java.awt.datatransfer", "java.awt.dnd", "java.awt.event",
                "java.awt.font", "java.awt.geom", "java.awt.im", "java.awt.im.spi", "java.awt.image",
                "java.awt.image.renderable", "java.awt.print", "java.beans", "java.beans.beancontext", "java.io",
                "java.lang", "java.lang.annotation", "java.lang.instrument", "java.lang.invoke", "java.lang.management",
                "java.lang.ref", "java.lang.reflect", "java.math", "java.net", "java.nio", "java.nio.channels",
                "java.nio.channels.spi", "java.nio.charset", "java.nio.charset.spi", "java.nio.file",
                "java.nio.file.attribute", "java.nio.file.spi", "java.rmi", "java.rmi.activation", "java.rmi.dgc",
                "java.rmi.registry", "java.rmi.server", "java.security", "java.security.acl", "java.security.cert",
                "java.security.interfaces", "java.security.spec", "java.sql", "java.text", "java.text.spi", "java.time",
                "java.time.chrono", "java.time.format", "java.time.temporal", "java.time.zone", "java.util",
                "java.util.concurrent", "java.util.concurrent.atomic", "java.util.concurrent.locks",
                "java.util.function", "java.util.jar", "java.util.logging", "java.util.prefs", "java.util.regex",
                "java.util.spi", "java.util.stream", "java.util.zip", "javax.accessibility", "javax.activation",
                "javax.activity", "javax.annotation", "javax.annotation.processing", "javax.crypto",
                "javax.crypto.interfaces", "javax.crypto.spec", "javax.imageio", "javax.imageio.event",
                "javax.imageio.metadata", "javax.imageio.plugins.bmp", "javax.imageio.plugins.jpeg",
                "javax.imageio.spi", "javax.imageio.stream", "javax.jws", "javax.jws.soap", "javax.lang.model",
                "javax.lang.model.element", "javax.lang.model.type", "javax.lang.model.util", "javax.management",
                "javax.management.loading", "javax.management.modelmbean", "javax.management.monitor",
                "javax.management.openmbean", "javax.management.relation", "javax.management.remote",
                "javax.management.remote.rmi", "javax.management.timer", "javax.naming", "javax.naming.directory",
                "javax.naming.event", "javax.naming.ldap", "javax.naming.spi", "javax.net", "javax.net.ssl",
                "javax.print", "javax.print.attribute", "javax.print.attribute.standard", "javax.print.event",
                "javax.rmi", "javax.rmi.CORBA", "javax.rmi.ssl", "javax.script", "javax.security.auth",
                "javax.security.auth.callback", "javax.security.auth.kerberos", "javax.security.auth.login",
                "javax.security.auth.spi", "javax.security.auth.x500", "javax.security.cert", "javax.security.sasl",
                "javax.sound.midi", "javax.sound.midi.spi", "javax.sound.sampled", "javax.sound.sampled.spi",
                "javax.sql", "javax.sql.rowset", "javax.sql.rowset.serial", "javax.sql.rowset.spi", "javax.swing",
                "javax.swing.border", "javax.swing.colorchooser", "javax.swing.event", "javax.swing.filechooser",
                "javax.swing.plaf", "javax.swing.plaf.basic", "javax.swing.plaf.metal", "javax.swing.plaf.multi",
                "javax.swing.plaf.nimbus", "javax.swing.plaf.synth", "javax.swing.table", "javax.swing.text",
                "javax.swing.text.html", "javax.swing.text.html.parser", "javax.swing.text.rtf", "javax.swing.tree",
                "javax.swing.undo", "javax.tools", "javax.transaction", "javax.transaction.xa", "javax.xml",
                "javax.xml.bind", "javax.xml.bind.annotation", "javax.xml.bind.annotation.adapters",
                "javax.xml.bind.attachment", "javax.xml.bind.helpers", "javax.xml.bind.util", "javax.xml.crypto",
                "javax.xml.crypto.dom", "javax.xml.crypto.dsig", "javax.xml.crypto.dsig.dom",
                "javax.xml.crypto.dsig.keyinfo", "javax.xml.crypto.dsig.spec", "javax.xml.datatype",
                "javax.xml.namespace", "javax.xml.parsers", "javax.xml.soap", "javax.xml.stream",
                "javax.xml.stream.events", "javax.xml.stream.util", "javax.xml.transform", "javax.xml.transform.dom",
                "javax.xml.transform.sax", "javax.xml.transform.stax", "javax.xml.transform.stream",
                "javax.xml.validation", "javax.xml.ws", "javax.xml.ws.handler", "javax.xml.ws.handler.soap",
                "javax.xml.ws.http", "javax.xml.ws.soap", "javax.xml.ws.spi", "javax.xml.ws.spi.http",
                "javax.xml.ws.wsaddressing", "javax.xml.xpath", "org.ietf.jgss", "org.omg.CORBA", "org.omg.CORBA_2_3",
                "org.omg.CORBA_2_3.portable", "org.omg.CORBA.DynAnyPackage", "org.omg.CORBA.ORBPackage",
                "org.omg.CORBA.portable", "org.omg.CORBA.TypeCodePackage", "org.omg.CosNaming",
                "org.omg.CosNaming.NamingContextExtPackage", "org.omg.CosNaming.NamingContextPackage",
                "org.omg.Dynamic", "org.omg.DynamicAny", "org.omg.DynamicAny.DynAnyFactoryPackage",
                "org.omg.DynamicAny.DynAnyPackage", "org.omg.IOP", "org.omg.IOP.CodecFactoryPackage",
                "org.omg.IOP.CodecPackage", "org.omg.Messaging", "org.omg.PortableInterceptor",
                "org.omg.PortableInterceptor.ORBInitInfoPackage", "org.omg.PortableServer",
                "org.omg.PortableServer.CurrentPackage", "org.omg.PortableServer.POAManagerPackage",
                "org.omg.PortableServer.POAPackage", "org.omg.PortableServer.portable",
                "org.omg.PortableServer.ServantLocatorPackage", "org.omg.SendingContext", "org.omg.stub.java.rmi",
                "org.w3c.dom", "org.w3c.dom.bootstrap", "org.w3c.dom.events", "org.w3c.dom.ls", "org.w3c.dom.views",
                "org.xml.sax", "org.xml.sax.ext", "org.xml.sax.helpers"
        });

        final Stream<String> pkgs = fullSE8API.stream();
        // Java8u20 final Stream<String> pkgs = compact1.stream();
        final BinaryOperator<PackageNode> NO_COMBINER = (p1, p2) -> {
            throw new IllegalStateException();
        };
        final String ROOT_INDICATOR = "/";
        final PackageNode root = pkgs.map(r -> {
            final String[] split = r.split("\\.");
            // The sentinel value is to indicate that the "middle" package is also one to be included in Pattern.
            // Example is "java.awt" which has sub package "java.awt.color" and etc.
            // On the other hand, middle packages like "java" and "org" is not to be included in Pattern.
            return Stream.concat(IntStream.range(0, split.length)
                                          .mapToObj(depth -> PackageNode.newInstance(split[depth], depth)),
                                 Stream.of(PackageNode.sentinel(split.length)));
        }).reduce(PackageNode.newInstance(ROOT_INDICATOR, 0), (root_, stream) -> {
            stream.reduce(root_, (last, current) -> {
                if (last.containsKey(current)) {
                    return last.get(current);
                }
                last.add(current);
                return current;
            }, NO_COMBINER);
            return root_;
        }, NO_COMBINER);
        WHITE_PACKAGES = Pattern.compile(String.format("^%s$",
                                                       root.toPatternGroup().replaceFirst("^" + ROOT_INDICATOR, "")));
    }
}
