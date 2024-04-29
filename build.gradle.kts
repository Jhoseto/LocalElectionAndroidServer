plugins {
    id("java")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation ("mysql:mysql-connector-java:8.0.33")
    implementation ("org.springframework.boot:spring-boot-starter-web:2.6.3")
    implementation ("com.google.code.gson:gson:2.8.9")
    implementation ("org.json:json:20210307")
    implementation ("org.java-websocket:Java-WebSocket:1.5.3")
    implementation ("com.sun.mail:javax.mail:1.6.2")
    implementation ("org.apache.commons:commons-email:1.5")

    testImplementation(platform("org.junit:junit-bom:5.9.1"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}


tasks.test {
    useJUnitPlatform()
}