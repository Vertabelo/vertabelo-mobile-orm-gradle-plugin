package com.mobiorm.gradle

import groovy.json.JsonSlurper
import org.apache.http.HttpEntity
import org.apache.http.HttpResponse
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpPost
import org.apache.http.entity.mime.MultipartEntityBuilder
import org.apache.http.impl.client.DefaultHttpClient
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import javax.xml.bind.DatatypeConverter

/**
 * 'orm-generate' task class. Tries to generate ORM from Vertabelo XML model file based on following parameters:
 * - modelFile : relative path to Vertabelo XML model file
 * - destDir : directory where ORM files should be unpacked
 * - package : java package for generated files
 * - apiUrl : url to API generating ORM by HTTP request
 *
 * @author Krzysztof Waraksa
 */
class GenerateORMTask extends DefaultTask {

    @TaskAction
    def void generate() {
        println 'Generating ORM...'
        def xmlFile = new File(project.mobiorm.modelFile.toString())

        String url = project.mobiorm.apiUrl + "/" + project.mobiorm.apiVersion + "/generate/android"
        HttpClient client = new DefaultHttpClient()

        HttpEntity entity = MultipartEntityBuilder.create()
                .addBinaryBody("vertabeloXML", xmlFile)
                .addTextBody("package", project.mobiorm.packageName.toString())
                .build()

        HttpPost post = new HttpPost(url)
        post.setEntity(entity)

        HttpResponse response = client.execute(post)

        if (response.getStatusLine().getStatusCode() == 200) {
            File outPutDir = new File(project.mobiorm.destDir.toString())
            outPutDir.mkdirs()

            def parser = new JsonSlurper()
            def json = parser.parse(response.getEntity().getContent(), "UTF-8")
            if (json.status == "OK") {
                new FileOutputStream(project.mobiorm.destDir + "/mobi-orm.zip").withStream {
                    DatatypeConverter converter = new DatatypeConverter()
                    it << converter.parseBase64Binary(json.result.content)
                }

                project.exec { ex ->
                    ex.workingDir outPutDir.absolutePath
                    ex.commandLine 'unzip', '-o', 'mobi-orm.zip'
                }

                project.exec { ex ->
                    ex.workingDir outPutDir.absolutePath
                    ex.commandLine 'rm', 'mobi-orm.zip'
                }

                println 'ORM generated successfully.'

            } else if (json.status == "ERROR") {
                throw new RuntimeException("Error when generating ORM: " + json.result.error)
            }
        } else if (response.getStatusLine().getStatusCode() == 500) {
            throw new RuntimeException("Internal server error.")
        } else {
            throw new RuntimeException("Cannot generate ORM: " + response.getStatusLine())
        }
    }
}
