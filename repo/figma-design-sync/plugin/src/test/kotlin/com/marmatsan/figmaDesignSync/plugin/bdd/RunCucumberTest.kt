package com.marmatsan.figmaDesignSync.plugin.bdd

import io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME
import org.junit.platform.suite.api.ConfigurationParameter
import org.junit.platform.suite.api.IncludeEngines
import org.junit.platform.suite.api.SelectPackages
import org.junit.platform.suite.api.Suite

@Suite
@IncludeEngines("cucumber")
@SelectPackages("com.marmatsan.figmaDesignSync.plugin.bdd")
@ConfigurationParameter(
    key = GLUE_PROPERTY_NAME,
    value = "com.marmatsan.figmaDesignSync.plugin.bdd"
)
class RunCucumberTest
