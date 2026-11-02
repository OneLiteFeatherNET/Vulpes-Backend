package net.onelitefeather.vulpes.backend.domain.expression;

import com.ezylang.evalex.config.ExpressionConfiguration;
import net.onelitefeather.vulpes.api.model.expression.ExpressionFunctionKind;
import net.onelitefeather.vulpes.api.model.expression.ExpressionVariableType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Unit tests for finding the functions a formula calls")
class FunctionReferencesTest {

    private static final ExpressionConfiguration CONFIGURATION = ProjectExpressionConfiguration.of(List.of(
            new ExpressionFunctionDTO(null, "UI scale", "scale", ExpressionFunctionKind.FORMULA,
                    ExpressionVariableType.NUMBER,
                    List.of(new ExpressionVariableDTO("value", ExpressionVariableType.NUMBER, null)),
                    "value * 2", null)
    ));

    @Test
    void testCalledFunctions_listsProjectAndBuiltInFunctionsInLowerCase() {
        assertEquals(Set.of("scale", "max"), FunctionReferences.calledFunctions("MAX(SCALE(a), 3)", CONFIGURATION),
                "every called function must be listed in lower case");
    }

    @Test
    void testCalls_matchesIgnoringCase() {
        assertTrue(FunctionReferences.calls("Scale(4) + 1", "SCALE", CONFIGURATION),
                "a call must be found whatever case the formula and the name use");
    }

    @Test
    void testCalls_variableWithFunctionName_isNoCall() {
        assertFalse(FunctionReferences.calls("scale + 1", "scale", CONFIGURATION),
                "a variable named like the function is not a call");
    }

    @Test
    void testCalledFunctions_unparsableOrBlankFormula_callsNothing() {
        assertEquals(Set.of(), FunctionReferences.calledFunctions("scale(", CONFIGURATION),
                "a formula that does not parse must not report calls");
        assertEquals(Set.of(), FunctionReferences.calledFunctions(" ", CONFIGURATION),
                "a blank formula calls nothing");
        assertEquals(Set.of(), FunctionReferences.calledFunctions(null, CONFIGURATION),
                "a missing formula calls nothing");
    }
}
