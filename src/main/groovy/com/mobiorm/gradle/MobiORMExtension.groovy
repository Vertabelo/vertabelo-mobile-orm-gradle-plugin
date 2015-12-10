package com.mobiorm.gradle

class MobiORMExtension {
    String modelFile = "model.xml"
    String destDir = "src/main/java"
    String packageName = "com.mobiorm"
    String apiUrl = "https://api.mobiorm.com"
    String apiVersion = "1.0"
    VertabeloORMExtension vertabelo = new VertabeloORMExtension()
}

class VertabeloORMExtension {
    String destModelFile = "model.xml"
    String apiToken
    String modelId
    String modelTag
}
