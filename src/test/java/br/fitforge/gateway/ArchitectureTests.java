package br.fitforge.gateway;

import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.Test;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureTests {

    @Test
    void featureRespeitaFronteiras() {
        var code = new ClassFileImporter().withImportOption(new ImportOption.DoNotIncludeTests())
                .importPackages("br.fitforge.gateway");
        classes().that().haveSimpleName("BffController")
                .should().resideInAPackage("..bff.controller").check(code);
        noClasses().that().resideInAPackage("..bff.controller")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "..repository..", "org.springframework.data..",
                        "org.springframework.web.reactive.function.client..",
                        "com.azure.messaging.servicebus..").check(code);
        noClasses().that().resideInAPackage("..bff.service")
                .should().dependOnClassesThat().resideInAPackage("..controller..").check(code);
    }
}
