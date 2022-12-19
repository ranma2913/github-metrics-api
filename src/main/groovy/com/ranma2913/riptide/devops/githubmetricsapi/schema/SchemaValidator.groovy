package com.ranma2913.riptide.devops.githubmetricsapi.schema

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory
import com.fasterxml.jackson.dataformat.yaml.YAMLGenerator
import com.networknt.schema.JsonSchema
import com.networknt.schema.JsonSchemaFactory
import com.networknt.schema.SpecVersion
import com.networknt.schema.SpecVersionDetector
import com.networknt.schema.ValidationMessage
import groovy.util.logging.Slf4j
import org.springframework.stereotype.Service

@Slf4j
@Service
class SchemaValidator {
  ObjectMapper yamlMapper
  ObjectMapper jsonMapper

  SchemaValidator() {
    YAMLFactory yamlFactory = new YAMLFactory()
    yamlFactory.configure(YAMLGenerator.Feature.MINIMIZE_QUOTES, true)
    yamlMapper = new ObjectMapper(yamlFactory)
    jsonMapper = new ObjectMapper()
  }

  Set<ValidationMessage> validateYamlText(String schema, String text, SpecVersion.VersionFlag version) {
    return validateText(yamlMapper, schema, text, version)

  }

  Set<ValidationMessage> validateJsonText(String schema, String text, SpecVersion.VersionFlag version) {
    return validateText(jsonMapper, schema, text, version)
  }

  private Set<ValidationMessage> validateText(ObjectMapper objectMapper, String schema, String text, SpecVersion.VersionFlag version) {
    SpecVersion.VersionFlag actualVersion = version ?: SpecVersionDetector.detect(jsonMapper.readTree(schema))

    JsonSchemaFactory validatorFactory = JsonSchemaFactory.builder(
        JsonSchemaFactory.getInstance(actualVersion))
        .objectMapper(objectMapper).build()

    JsonNode textObject = objectMapper.readTree(text)
    String cleanText = objectMapper.writeValueAsString(textObject)
    JsonNode cleanTextObject = objectMapper.readTree(cleanText)
    JsonSchema jsonSchema = validatorFactory.getSchema(schema)
    Set<ValidationMessage> validationMessages = jsonSchema.validate(cleanTextObject)

    return validationMessages
  }

  Set<String> validationMessagesStrings(Set<ValidationMessage> validationMessages) {
    Set response = []
    validationMessages.each { ValidationMessage validationMessage ->
      response.add(validationMessage.getMessage())
    }
    return response
  }
}