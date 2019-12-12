package at.enfilo.def.node.routine.factory.impl;

import org.junit.Test;
import org.mockito.Mockito;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

public class TemplateTest {

    @Test
    public void template() {
        Map<String, Integer> testMap = new HashMap<>();
        testMap.put("mono test arg1 arg2", 1);
        testMap.put("python3 {rbs} {args} {pipes}", 6);
        testMap.put("{$path} {rbs} {pipes}", 5);
        testMap.put("java ({rbs}:-cp {}) {args} {pipes}", 6);
        testMap.put("java -cp ({rbs}:{}:)[{java(>1.8)}:{$test}:] {args} {pipes}", 7);
        testMap.put("java ({rbs}:-cp {}) [{java(>1.8)}:{$test}] {args} {pipes}", 8);
        testMap.put("java ({rbs}:-cp {}) [{java(>1.8)}:{$test}] test {args} {pipes}", 8);
        testMap.put("java ({rbs}:-cp {}) [{java(>1.8)}:{$test}] test (cp) [[(arg)]] {args} {pipes}", 12);
        testMap.put("java -args={in},{out},{ctrl}", 6);
        testMap.put("{rbs} {args} {pipes}", 5);

        for (Map.Entry<String, Integer> entry : testMap.entrySet()) {
            List<String> current = new Template(null, entry.getKey()).getTemplate();
            assertEquals(entry.getValue().intValue(), current.size());
            assertEquals(entry.getKey(), String.join("", current));
        }
    }

    @Test
    public void parse() {
        String string1 = "mono test arg1 arg2";
        String string2 = "python3 {rbs} {args} {pipes}";
        String string3 = "{$path} {rbs} {pipes}";
        String string4 = "java ({rbs}:-cp {} ){args} {pipes}";
        String string5 = "{rbs} {args} {pipes}";

        ITemplateDataProvider dataProvider = Mockito.mock(ITemplateDataProvider.class);
        when(dataProvider.resolvePlaceholder("rbs")).thenReturn("rb1 rb2 rb3");
        when(dataProvider.resolvePlaceholder("args")).thenReturn("arg1 arg2 arg3");
        when(dataProvider.resolvePlaceholder("pipes")).thenReturn("in ctrl out");

        ITemplateDataProvider dataProvider3 = Mockito.mock(ITemplateDataProvider.class);
        when(dataProvider3.resolvePlaceholder("rbs")).thenReturn("rb");
        when(dataProvider3.resolvePlaceholder("args")).thenReturn(null);
        when(dataProvider3.resolvePlaceholder("pipes")).thenReturn("in ctrl out");

        when(dataProvider.resolveVariable("path")).thenReturn("pathToRb");
        when(dataProvider.resolveVariable("loop")).thenReturn("({args}:{$path})");

        Template template1 = new Template(dataProvider, string1);
        Template template2 = new Template(dataProvider, string2);
        Template template3 = new Template(dataProvider, string3);
        Template template4 = new Template(dataProvider, string4);
        Template template5 = new Template(dataProvider3, string5);

        assertEquals("mono test arg1 arg2", template1.parse());
        assertEquals("python3 rb1 rb2 rb3 arg1 arg2 arg3 in ctrl out", template2.parse());
        assertEquals("pathToRb rb1 rb2 rb3 in ctrl out", template3.parse());
        assertEquals("java -cp rb1 -cp rb2 -cp rb3 arg1 arg2 arg3 in ctrl out", template4.parse());
        assertEquals("rb in ctrl out", template5.parse());

        String test = "java ({rbs}:-cp {} {$loopVar})[{python(3.7):numpy}:{$pythonVar} test {$numpyVar} test {$javaVar}] -test {pipes}";

        ITemplateDataProvider dataProvider2 = Mockito.mock(ITemplateDataProvider.class);
        when(dataProvider2.resolvePlaceholder("rbs")).thenReturn("rb1 rb2");
        when(dataProvider2.resolvePlaceholder("args")).thenReturn("arg1 arg2");
        when(dataProvider2.resolvePlaceholder("pipes")).thenReturn("in ctrl");

        when(dataProvider2.resolveVariable("path")).thenReturn("pathToRb");
        when(dataProvider2.resolveVariable("loopVar")).thenReturn("({args}:{$path} )");

        when(dataProvider2.resolveOptional("python(3.7):numpy", "{$pythonVar} test {$numpyVar} test {$javaVar}"))
                .thenReturn("py test npy test ja");

        assertEquals("java -cp rb1 pathToRb pathToRb -cp rb2 pathToRb pathToRb py test npy test ja -test in ctrl", new Template(dataProvider2, test).parse());

        String string6 = "java -cp ({rbs}:{}:)[{java(>1.8)}:{$test}:] {args} {pipes}";

        ITemplateDataProvider dataProvider4 = Mockito.mock(ITemplateDataProvider.class);
        when(dataProvider4.resolvePlaceholder("rbs")).thenReturn("r1 r2");
        when(dataProvider4.resolvePlaceholder("args")).thenReturn("a");
        when(dataProvider4.resolvePlaceholder("pipes")).thenReturn("p");

        when(dataProvider4.resolveVariable("test")).thenReturn("t");

        when(dataProvider4.resolveOptional("java(>1.8)", "{$test}:")).thenReturn("t:");

        assertEquals("java -cp r1:r2:t: a p", new Template(dataProvider4, string6).parse());
    }
}
