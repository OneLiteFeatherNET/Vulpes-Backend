package net.onelitefeather.vulpes.backend.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

/**
 * Keeps the layers of the asset code apart: controller, then service, then persistence and storage.
 * The rules read the production classes only.
 */
@AnalyzeClasses(packages = "net.onelitefeather.vulpes.backend", importOptions = ImportOption.DoNotIncludeTests.class)
class AssetArchitectureTest {

    @ArchTest
    static final ArchRule asset_controllers_depend_only_on_the_service_interface = noClasses()
            .that()
            .resideInAPackage("..controller.asset..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..entity.asset..", "..storage..", "..service.impl..");

    @ArchTest
    static final ArchRule asset_domain_does_not_use_entities = noClasses()
            .that()
            .resideInAPackage("..domain.asset..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..entity.asset..");

    @ArchTest
    static final ArchRule asset_storage_is_only_used_by_the_service_implementation = noClasses()
            .that()
            .resideOutsideOfPackages("..service.impl..", "..storage..")
            .should()
            .dependOnClassesThat()
            .resideInAPackage("..storage..");
}
