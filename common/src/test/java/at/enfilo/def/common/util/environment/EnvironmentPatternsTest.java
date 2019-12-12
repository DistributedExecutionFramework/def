package at.enfilo.def.common.util.environment;

import org.junit.Test;

import static at.enfilo.def.common.util.environment.EnvironmentPatterns.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class EnvironmentPatternsTest {

    @Test
    public void testPatterns() {
        assertTrue("1".matches(VERSION_PATTERN_EXACT));
        assertTrue("1.1".matches(VERSION_PATTERN_EXACT));
        assertTrue("1.123.14.1".matches(VERSION_PATTERN_EXACT));
        assertFalse("1.".matches(VERSION_PATTERN_EXACT));
        assertFalse("".matches(VERSION_PATTERN_EXACT));
        assertFalse("1.a.4".matches(VERSION_PATTERN_EXACT));
        assertFalse("1-5.4".matches(VERSION_PATTERN_EXACT));

        assertTrue(">1".matches(VERSION_PATTERN_HIGHER));
        assertTrue(">1.1".matches(VERSION_PATTERN_HIGHER));
        assertTrue(">1.423".matches(VERSION_PATTERN_HIGHER));
        assertFalse("<1.3".matches(VERSION_PATTERN_HIGHER));

        assertTrue("<1.3".matches(VERSION_PATTERN_LOWER));
        assertFalse(">1.3".matches(VERSION_PATTERN_LOWER));
        assertFalse("1.3".matches(VERSION_PATTERN_LOWER));

        assertTrue("1.24-1.54.3".matches(VERSION_PATTERN_INTERVAL));
        assertFalse("1.24".matches(VERSION_PATTERN_INTERVAL));
        assertFalse("1.543-".matches(VERSION_PATTERN_INTERVAL));

        assertTrue("1.123.14.1".matches(VERSION_PATTERN_SINGLE));
        assertTrue(">1.423".matches(VERSION_PATTERN_SINGLE));
        assertTrue("<1.3".matches(VERSION_PATTERN_SINGLE));
        assertTrue("1.24-1.54.3".matches(VERSION_PATTERN_SINGLE));
        assertFalse("(1.24-1.54.3)".matches(VERSION_PATTERN_SINGLE));
        assertFalse("(1.24-1.54.3|1.4)".matches(VERSION_PATTERN_SINGLE));

        assertTrue("1.1".matches(VERSION_PATTERN));
        assertTrue(">1.423".matches(VERSION_PATTERN));
        assertTrue("<1.3".matches(VERSION_PATTERN));
        assertTrue("1.24-1.54.3".matches(VERSION_PATTERN));
        assertTrue("1,1.1,<1.2,>1.3,1.4-1.5".matches(VERSION_PATTERN));

        assertTrue("java".matches(FEATURE_NAME_PATTERN));
        assertTrue("python3".matches(FEATURE_NAME_PATTERN));
        assertTrue("matlab-runtime".matches(FEATURE_NAME_PATTERN));

        assertTrue("python(3.7)".matches(FEATURE_PATTERN_SINGLE));
        assertTrue("python(3.7,>4)".matches(FEATURE_PATTERN_SINGLE));
        assertFalse("python3()".matches(FEATURE_PATTERN_SINGLE));

        assertTrue("python3(2.7,3.7):numpy".matches(FEATURE_PATTERN));
        assertTrue("python3:numpy(2.7,3.7)".matches(FEATURE_PATTERN));
        assertTrue("python3(2.7,3.7):numpy(2.7,3.7),matplotlib".matches(FEATURE_PATTERN));
        assertFalse("python3:".matches(FEATURE_PATTERN));
        assertFalse("python3:numpy(2)matplotlib".matches(FEATURE_PATTERN));

        assertTrue("arg".matches(COMMAND_TEMPLATE_PLACEHOLDER_VARIABLE_NAME_PATTERN));
        assertTrue("arg3".matches(COMMAND_TEMPLATE_PLACEHOLDER_VARIABLE_NAME_PATTERN));
        assertTrue("path_to_binary".matches(COMMAND_TEMPLATE_PLACEHOLDER_VARIABLE_NAME_PATTERN));

        assertTrue("{arg}".matches(COMMAND_TEMPLATE_PLACEHOLDER));
        assertTrue("{arg5}".matches(COMMAND_TEMPLATE_PLACEHOLDER));
        assertFalse("{}".matches(COMMAND_TEMPLATE_PLACEHOLDER));
        assertFalse("{arg".matches(COMMAND_TEMPLATE_PLACEHOLDER));
        assertFalse("arg".matches(COMMAND_TEMPLATE_PLACEHOLDER));
        assertFalse("{arg}s".matches(COMMAND_TEMPLATE_PLACEHOLDER));

        assertTrue("{$arg}".matches(COMMAND_TEMPLATE_VARIABLE));
        assertTrue("{$arg5}".matches(COMMAND_TEMPLATE_VARIABLE));
        assertFalse("{$}".matches(COMMAND_TEMPLATE_VARIABLE));
        assertFalse("{arg}".matches(COMMAND_TEMPLATE_VARIABLE));
        assertFalse("{$arg".matches(COMMAND_TEMPLATE_VARIABLE));
        assertFalse("$arg".matches(COMMAND_TEMPLATE_VARIABLE));
        assertFalse("{$arg}s".matches(COMMAND_TEMPLATE_VARIABLE));

        assertTrue("({args}:{})".matches(COMMAND_TEMPLATE_LOOP));
        assertTrue("({args}:{$path})".matches(COMMAND_TEMPLATE_LOOP));
        assertTrue("({rbs}:-cp {})".matches(COMMAND_TEMPLATE_LOOP));
        assertTrue("({args}:abc )".matches(COMMAND_TEMPLATE_LOOP));
        assertFalse("({$args}:abc )".matches(COMMAND_TEMPLATE_LOOP));
        assertFalse("(:abc )".matches(COMMAND_TEMPLATE_LOOP));
        assertFalse("(abc )".matches(COMMAND_TEMPLATE_LOOP));
        assertFalse("({args})".matches(COMMAND_TEMPLATE_LOOP));
        assertFalse("({args} abc )".matches(COMMAND_TEMPLATE_LOOP));
        assertFalse("{args}:abc".matches(COMMAND_TEMPLATE_LOOP));

        assertTrue("[{java(>1.8)}:abc]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertTrue("[{matlab-runtime}:{$path}]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertTrue("[{python3(2.7,3.7):numpy}:-cp {$test}]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertTrue("[{java}:abc ]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertFalse("[{$java}:abc ]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertFalse("[:abc ]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertFalse("[abc ]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertFalse("[{java}]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertFalse("[{java} abc ]".matches(COMMAND_TEMPLATE_OPTIONAL));
        assertFalse("{java}:abc]".matches(COMMAND_TEMPLATE_OPTIONAL));
    }
}
