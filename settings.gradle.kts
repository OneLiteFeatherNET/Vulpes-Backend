rootProject.name = "vulpes-backend"

dependencyResolutionManagement {
    if (System.getenv("CI") != null) {
        repositoriesMode = RepositoriesMode.PREFER_SETTINGS
        repositories {
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            maven("https://repo.htl-md.schule/repository/Gitlab-Runner/")
            maven {
                val groupdId = 28 // Gitlab Group
                val ciApiv4Url = System.getenv("CI_API_V4_URL")
                url = uri("$ciApiv4Url/groups/$groupdId/-/packages/maven")
                name = "GitLab"
                credentials(HttpHeaderCredentials::class.java) {
                    name = "Job-Token"
                    value = System.getenv("CI_JOB_TOKEN")
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    } else {
        repositories {
            mavenCentral()
            maven("https://s01.oss.sonatype.org/content/repositories/snapshots/")
            maven {
                val groupdId = 28 // Gitlab Group
                url = uri("https://gitlab.onelitefeather.dev/api/v4/groups/$groupdId/-/packages/maven")
                name = "GitLab"
                credentials(HttpHeaderCredentials::class.java) {
                    name =  "Private-Token"
                    value = providers.gradleProperty("gitLabPrivateToken").get()
                }
                authentication {
                    create<HttpHeaderAuthentication>("header")
                }
            }
        }
    }
    versionCatalogs {
        create("libs") {
            version("kotlin", "2.0.20")
            version("annotation", "26.0.1")
            version("spring", "3.4.0")
            version("spring.management", "1.1.6")
            version("jackson", "2.18.2")
            version("embed.mongo", "4.11.0")

            library("jackson", "com.fasterxml.jackson.module", "jackson-module-kotlin").versionRef("jackson")
            library("annotation", "org.jetbrains", "annotations").versionRef("annotation")
            library("vulpes.api", "net.theevilreaper.vulpes.api", "vulpes-spring-api").version("0.1.0-SNAPSHOT+2597aca4")
            library("spring.starter.web", "org.springframework.boot", "spring-boot-starter-web").withoutVersion()
            library("spring.starter.data.mongodb", "org.springframework.boot", "spring-boot-starter-data-mongodb").withoutVersion()
            library("spring.starter.test", "org.springframework.boot", "spring-boot-starter-test").withoutVersion()
            library("embed.mongo", "de.flapdoodle.embed", "de.flapdoodle.embed.mongo.spring30x").versionRef("embed.mongo")

            plugin("spring", "org.springframework.boot").versionRef("spring")
            plugin("spring.dependency", "io.spring.dependency-management").versionRef("spring.management")
            plugin("kotlin.jvm", "org.jetbrains.kotlin.jvm").versionRef("kotlin")
            plugin("kotlin.spring", "org.jetbrains.kotlin.plugin.spring").versionRef("kotlin")
        }
    }
}