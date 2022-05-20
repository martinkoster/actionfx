plugins {
    id "com.github.spotbugs" version "${r"${spotbugsVersion}"}"
    id "java-library"
    id("org.openjfx.javafxplugin") version "${r"${javafxPluginVersion}"}"
<#if useSpring>
    id 'org.springframework.boot' version "${r"${springBootVersion}"}"
    id 'io.spring.dependency-management' version "${r"${springDepManagementVersion}"}"
</#if>    
}

wrapper {
    gradleVersion = "7.4.2"
    distributionType = Wrapper.DistributionType.ALL
    distributionUrl = "https://services.gradle.org/distributions/gradle-7.4.2-bin.zip"
}

description = "ActionFX: A declarative, less-intrusive JavaFX MVC framework with dependency injection"

apply plugin: "java-library"
apply plugin: "eclipse"
apply plugin: "idea"
apply plugin: "jacoco"
apply plugin: "com.github.spotbugs"
<#if useSpring>
apply plugin: "org.springframework.boot"
apply plugin: "io.spring.dependency-management"
</#if>

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
		html.destination = file("${r"${reporting.baseDir}/${name}"}")
	}
	testLogging {
    	exceptionFormat = "full"
    }
}

tasks.withType(Test) {
	systemProperties = System.properties
    systemProperties["user.dir"] = workingDir
    reports.html.destination = file("${r"${reporting.baseDir}/${name}"}")
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
    	println "Executing test ${r"${test.name}"} [${r"${test.className}"}] with result: ${r"${result.resultType}"}"
   	}
}

////////////////////
// Infrastracture //
////////////////////
idea {
	module {
	}
}

eclipse {
	classpath {
	    downloadSources = true
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
    into "${r"${rootProject.projectDir}/build/docs/${version}/${project.name}"}"
}
copyJavaDocToDocs.dependsOn javadoc
test.dependsOn copyJavaDocToDocs

<#if useSpring>
bootJar {
	enabled = true
	mainClass = '${packageName}.${mainAppClassName}'
}
<#else>
jar {
    manifest {
        attributes 'Main-Class': '${packageName}.${mainAppClassName}'
    }
    from {
        configurations.runtimeClasspath.collect {
            duplicatesStrategy = 'exclude'
            it.isDirectory() ? it : zipTree(it)
        }
    }
}
</#if>
	
dependencies {

    // JAVAFX DEPENDENCIES    
	javafx {
    	version = "${r"${javafxVersion}"}"
        modules = [ 'javafx.base', 'javafx.controls', 'javafx.fxml', 'javafx.graphics', 'javafx.media', 'javafx.web', 'javafx.swing' ]
   	}
   	
   	// ACTIONFX DEPENDENCIES
	implementation group: "com.github.martinkoster", name: "actionfx-core", version: "${r"${actionFXVersion}"}"
	implementation group: "com.github.martinkoster", name: "actionfx-controlsfx", version: "${r"${actionFXVersion}"}"
<#if useSpring>
	implementation group: "com.github.martinkoster", name: "actionfx-spring-boot", version: "${r"${actionFXVersion}"}"

	// SPRING DEPENDENCIES
	implementation group: "org.springframework.boot", name: "spring-boot-starter"
</#if>

	testImplementation group: "com.github.martinkoster", name: "actionfx-testing", version: "${r"${actionFXVersion}"}"

    compileOnly group: "com.github.spotbugs", name: "spotbugs-annotations", version: "${r"${spotbugsAnnotationVersion}"}"
	testImplementation group: "org.junit.jupiter", name: "junit-jupiter-api", version: "${r"${junitJupiterVersion}"}"
   	testRuntimeOnly group: "org.junit.jupiter", name: "junit-jupiter-engine", version: "${r"${junitJupiterVersion}"}"
   	testImplementation group: 'org.hamcrest', name: 'hamcrest', version: "${r"${hamcrestVersion}"}"
}