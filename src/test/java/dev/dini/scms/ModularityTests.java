package dev.dini.scms;

import com.tngtech.archunit.core.domain.JavaClass;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;

class ModularityTests {

    @Test
    void verifiesModularStructure() {
        ApplicationModules.of(
                ScmsApplication.class,
                JavaClass.Predicates.resideInAPackage("dev.dini.scms.seed..")
        ).verify();
    }
}
