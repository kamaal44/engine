package jadx.tests.integration.inline;

import jadx.core.dex.nodes.ClassNode;
import jadx.tests.api.IntegrationTest;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;

public class TestInline6 extends IntegrationTest {

    @Test
    public void test() {
        ClassNode cls = getClassNode(TestCls.class);
        String code = cls.getCode().toString();

        assertThat(code, containsString("System.out.println(System.nanoTime() - start);"));
        assertThat(code, not(containsString("System.out.println(System.nanoTime() - System.nanoTime());")));
    }

    public static class TestCls {
        public void f() {
        }

        public void test(int a, int b) {
            long start = System.nanoTime();
            f();
            System.out.println(System.nanoTime() - start);
        }
    }
}