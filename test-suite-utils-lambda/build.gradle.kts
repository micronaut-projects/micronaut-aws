plugins {
    id("java-library")
}
repositories {
    mavenCentral()
}
dependencies {
    api(libs.managed.aws.lambda.core)
}
