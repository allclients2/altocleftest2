plugins {
    id("fabric-loom") version "1.6-SNAPSHOT" apply false
    id("com.replaymod.preprocess") version "b09f534"
}

subprojects {
    repositories {
        mavenLocal()
        mavenCentral()
        maven("https://libraries.minecraft.net/")
        maven("https://repo.spongepowered.org/repository/maven-public/")
        maven("https://github.com/jitsi/jitsi-maven-repository/raw/master/releases/")
        maven("https://maven.fabricmc.net/")
        maven("https://jitpack.io")
    }
}

preprocess {
    val mc12006 = createNode("1.20.6", 12006, "yarn")
    val mc12005 = createNode("1.20.5", 12005, "yarn")
    val mc12004 = createNode("1.20.4", 12004, "yarn")
    val mc12002 = createNode("1.20.2", 12002, "yarn")
    val mc12001 = createNode("1.20.1", 12001, "yarn")

    mc12006.link(mc12005)
    mc12005.link(mc12004)
    mc12004.link(mc12002)
    mc12002.link(mc12001)
}