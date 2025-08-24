package org.bg3.forge;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.baldurs.forge.services.DescriptionParam;
import org.junit.jupiter.api.Test;

public class DescriptionParamTest {

    @Test
    public void testDescriptionParam() {
        DescriptionParam descriptionParam = DescriptionParam.fromString("DealDamage(2, Acid)");
        assertEquals("DealDamage", descriptionParam.function);
        assertEquals("2", descriptionParam.args[0]);
        assertEquals("Acid", descriptionParam.args[1]);

        System.out.println("function: " + descriptionParam.function);
        System.out.println("value: " + descriptionParam.value);
        System.out.println("args:");
        for (String arg : descriptionParam.args) {
            System.out.println(arg);
        }
        System.out.println(DescriptionParam.param("DealDamage(2, Acid)"));

        descriptionParam = DescriptionParam.fromString("DealDamage(max(1,StrengthModifier), Acid)");
        assertEquals("DealDamage", descriptionParam.function);
        assertEquals("Strength Modifier (min 1)", descriptionParam.args[0]);
        assertEquals("Acid", descriptionParam.args[1]);

    }

}
