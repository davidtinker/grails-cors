grails.project.work.dir = "target"

grails.project.dependency.resolution = {
    inherits "global"
    log "warn"

    repositories {
        grailsCentral()
    }

    dependencies {
        test ":grails-test-suite-base:$grailsVersion"
        compile('org.springframework.security:spring-security-core:3.0.7.RELEASE') {
            transitive = false
        }
        compile('org.springframework.security:spring-security-web:3.0.7.RELEASE') {
			transitive = false
        }
    }

    plugins {
        compile ":webxml:1.4.1"
        build(":release:2.0.4", ":rest-client-builder:1.0.2") {
            export = false
        }
    }
}
