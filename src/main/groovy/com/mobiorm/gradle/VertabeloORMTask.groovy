package com.mobiorm.gradle

import org.apache.http.HttpResponse
import org.apache.http.auth.UsernamePasswordCredentials
import org.apache.http.client.HttpClient
import org.apache.http.client.methods.HttpGet
import org.apache.http.impl.auth.BasicScheme
import org.apache.http.impl.client.DefaultHttpClient
import org.gradle.api.InvalidUserDataException
import org.gradle.api.tasks.TaskAction

/**
 * 'vertabelo-orm-generate' task class. Tries to download XML model file from Vertabelo and generate ORM
 * based on following parameters:
 * - vertabelo.apiToken : API token to Vertabelo user account
 * - vertabelo.modelId : vertabelo model id
 * - vertabelo.modelTag : vertabelo model version tag (optional)
 * - vertabelo.destModelFile : file where XML model will be saved to
 * - destDir : directory where ORM files should be unpacked
 * - package : java package for generated files
 * - apiUrl : url to API generating ORM by HTTP request
 *
 * @author Krzysztof Waraksa
 */
class VertabeloORMTask extends GenerateORMTask {

    final String GET_MODEL_ADDR = "https://my.vertabelo.com/api/xml"

    @TaskAction
    @Override
    def void generate() {
        HttpClient client = new DefaultHttpClient()
        String modelId = project.mobiorm.vertabelo.modelId
        String tag = project.mobiorm.vertabelo.modelTag

        if (modelId == null) {
            throw new InvalidUserDataException("Model id cannot be empty.")
        }
        HttpGet request;
        if (tag != null) {
            request = new HttpGet(GET_MODEL_ADDR + "/" + modelId + "/" + tag)
        } else {
            request = new HttpGet(GET_MODEL_ADDR + "/" + modelId)
        }
        request.setHeader(BasicScheme.authenticate
                (new UsernamePasswordCredentials(project.mobiorm.vertabelo.apiToken.toString(), ''), "UTF-8", false))

        HttpResponse response = client.execute(request)

        if (response.getStatusLine().getStatusCode() == 200) {
            String xmlPath = project.mobiorm.vertabelo.destModelFile

            new FileOutputStream(xmlPath).withStream {
                response.getEntity().writeTo(it)
            }

            project.mobiorm.modelFile = xmlPath
            super.generate()
        } else if (response.getStatusLine().getStatusCode() == 401) {
            throw new InvalidUserDataException("Wrong API token.")
        }
    }

}
