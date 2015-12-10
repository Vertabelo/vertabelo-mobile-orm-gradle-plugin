package com.mobiorm.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Plugin class of MobiORM Gradle plugin.
 *
 * @author Krzysztof Waraksa
 */
class MobiORMPlugin implements Plugin<Project> {

    def void apply(Project project) {
        project.task('orm-generate', type: GenerateORMTask)
        project.task('vertabelo-orm-generate', type: VertabeloORMTask)
        project.extensions.create('mobiorm', MobiORMExtension)
        project.mobiorm.extensions.create('vertabelo', VertabeloORMExtension)
    }
    
}
