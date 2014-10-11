package net.exoego.typefind.definition;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.List;

import net.exoego.typefind.definition.PackageDef;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class PackageDefTest {
    @Test
    public void Create_from_usual_class() {
        assertThat(PackageDef.of(Object.class).getName(), is("java.lang"));
    }

    @Test
    public void Create_from_raw_tyoe() {
        assertThat(PackageDef.of(List.class).getName(), is("java.util"));
    }

    @Test
    public void Create_from_void() throws NoSuchMethodException {
        // void clear();
        final Method clear = List.class.getDeclaredMethod("clear");
        final Type voidReturnType = clear.getReturnType();
        assertThat(PackageDef.of(voidReturnType).getName(), is(""));
    }

    @Test
    public void Create_from_parameterized_type() throws NoSuchMethodException {
        // java.util.Collection<? extends E>
        final Type[] params = List.class.getDeclaredMethod("addAll", Collection.class).getGenericParameterTypes();
        final Type parametrizedType = params[0];
        assertThat(PackageDef.of(parametrizedType).getName(), is("java.util"));
    }

    @Test
    public void Create_from_type_variable() throws NoSuchMethodException {
        // typeVariable E of List<E>
        final Type E = List.class.getDeclaredMethod("get", int.class).getGenericReturnType();
        assertThat(PackageDef.of(E).getName(), is(""));
    }

    @Test
    public void Create_from_primitive_type() throws NoSuchMethodException {
        assertThat(PackageDef.of(int.class).getName(), is(""));
    }

    @Test
    public void Create_from_primitive_array() throws NoSuchMethodException {
        assertThat(PackageDef.of(int[].class).getName(), is(""));
    }

    @Test
    public void Create_from_reference_array() throws NoSuchMethodException {
        assertThat(PackageDef.of(String[].class).getName(), is("java.lang"));
    }
}
