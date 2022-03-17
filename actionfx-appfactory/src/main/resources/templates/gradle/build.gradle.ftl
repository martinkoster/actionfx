plugins {
    id "com.github.spotbugs" version "${spotbugsVersion}"
    id "java-library"
    id "io.github.gradle-nexus.publish-plugin" version "${gradleNexusPublishPlugin}"
    id "org.sonarqube" version "${sonarQubePlugin}"
    id "org.ajoberstar.git-publish" version "${gradleGitPublishPlugin}"
    id("org.openjfx.javafxplugin") version "0.0.9"
    
}

wrapper {
    gradleVersion = "6.7.0"
    distributionType = Wrapper.DistributionType.ALL
    distributionUrl = "https://services.gradle.org/distributions/gradle-6.7-bin.zip"
}

description = "ActionFX: A declarative, less-intrusive JavaFX MVC framework with dependency injection"

apply plugin: "java-library"
apply plugin: "eclipse"
apply plugin: "idea"
apply plugin: 'jacoco'
apply plugin: "com.github.spotbugs"

sourceCompatibility = "11" 
targetCompatibility = "11"
    
configurations {
	published
	all*.exclude module: "slf4j-log4j12"
	all*.exclude module: "jsr305"
	integrationTestImplementation.extendsFrom testImplementation
	integrationTestRuntimeOnly.extendsFrom testRuntimeOnly
}
      
repositories {
	mavenCentral()
    jcenter()
    mavenLocal()
}
    
// In this section you declare additional sourceSets
sourceSets {
	integrationTest {
	    java {
	        compileClasspath += main.output + test.output
	        runtimeClasspath += main.output + test.output
	    	srcDir file("src/integration-test/java")
	    }
		resources.srcDir file("src/integration-test/resources")
	}
}

[compileJava, compileTestJava, compileIntegrationTestJava]*.options*.encoding = "UTF-8"

java {
	withSourcesJar()
	withJavadocJar() 
}
	
test {
	useJUnitPlatform()
	reports {
		html.enabled = true
		html.destination = file("${reporting.baseDir}/${name}")
	}
	testLogging {
    	exceptionFormat = "full"
    }
}

tasks.withType(Test) {
	systemProperties = System.properties
    systemProperties["user.dir"] = workingDir
    reports.html.destination = file("${reporting.baseDir}/${name}")
    testLogging {
    	events "passed", "skipped", "failed"
    }
}
    
task integrationTest(type: Test) {
	useJUnitPlatform()
	testClassesDirs = sourceSets.integrationTest.output.classesDirs
	classpath = sourceSets.integrationTest.runtimeClasspath
	outputs.upToDateWhen { false }
	
	// test includes
	if ( project.hasProperty("tests.include") ) {
		include project.property("tests.include")
	}

	testLogging {
    	exceptionFormat = org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
   	}
    afterTest { test, result ->
    	println "Executing test ${test.name} [${test.className}] with result: ${result.resultType}"
   	}
}

////////////////////
// Infrastracture //
////////////////////
idea {
	module {
		scopes.TEST.plus += [ configurations.integrationTestCompile ]
	}
}

eclipse {
	classpath {
	    downloadSources = true
		plusConfigurations += [ configurations.integrationTestCompile ]
	}
}

///////////////////
// Code analysis //
///////////////////
spotbugs {
    effort = "max"
    reportLevel = "high"  
	reportsDir = file("$buildDir/reports/spotbugs")
}
    
spotbugsMain {
    reports {
    	html {
        	enabled = true
            destination = file("$buildDir/reports/spotbugs/spotbugs-main.html")
            stylesheet = "fancy-hist.xsl"
       	}
    }
}
    
spotbugsTest {
	enabled = false
    ignoreFailures = true
}
    
spotbugsIntegrationTest {
	enabled = false
    ignoreFailures = true
}

javadoc {
	description = "Generates project-level javadoc for use in -javadoc jar"

    options.memberLevel = org.gradle.external.javadoc.JavadocMemberLevel.PROTECTED
    options.author = true
    options.header = project.name
    options.addStringOption("Xdoclint:none", "-quiet")
    options.addStringOption("charSet", "UTF-8")

    // suppress warnings due to cross-module @see and @link references;
    // note that global "api" task does display all warnings.
    logging.captureStandardError LogLevel.INFO
    logging.captureStandardOutput LogLevel.INFO // suppress "## warnings" message

	failOnError = false
}
	
// Copy Javadoc of the sub-project to the docs folder for versioning und linking in Github
task copyJavaDocToDocs(type: Copy) {
    from "$buildDir/docs/javadoc"
    into "${rootProject.projectDir}/build/docs/${version}/${project.name}"
}
copyJavaDocToDocs.dependsOn javadoc
test.dependsOn copyJavaDocToDocs

	
dependencies {

    // JAVAFX DEPENDENCIES    
	javafx {
    	version = "${javafxVersion}"
        modules = [ 'javafx.base', 'javafx.controls', 'javafx.fxml', 'javafx.graphics', 'javafx.media', 'javafx.web', 'javafx.swing' ]
   	}

    compileOnly group: "com.github.spotbugs", name: "spotbugs-annotations", version: "${spotbugsAnnotationVersion}"
	testImplementation group: "org.junit.jupiter", name: "junit-jupiter-api", version: "${junitJupiterVersion}"
   	testRuntimeOnly group: "org.junit.jupiter", name: "junit-jupiter-engine", version: "${junitJupiterVersion}"
   	testImplementation group: 'org.hamcrest', name: 'hamcrest', version: "${hamcrestVersion}"
}
