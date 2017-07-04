package org.anc.lapps.chunk

import com.fasterxml.jackson.databind.JsonMappingException
import org.lappsgrid.api.WebService
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer

import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 * Created by krim on 9/8/16.
 */
abstract class AbstractWindowService implements WebService {

    static final String WINDOW = "Window"
    static final String SCORES = "Scores"
    static final String BEST = "SelectedOutput"

    static final String NO_INPUT = "No input was provided."
    static final String INVALID_DISCRIMINATOR = "Invalid discriminator. Expected a LIF document but found "
    static final String NO_PARAMETERS = "No input parameter map found."
    static final String NO_PASSAGE_ANNOTAION = "No passage annotation type was provided."
    static final String NO_ANNOTATIONS = "No view contains the selected annotation type."
    static final String NO_KEYWORD_PARAMETER = "Missing Parameter: no keywords file specified."
    static final String NO_KEYWORD_FILE = "Keyword file not found."
    static final String NO_SCORER_CONFIG = "Missing configurations for scorer, using default weights."

    Data validateInputJson(String input) {

        if (input == null) {
            return error(NO_INPUT)
        }
        Data data
        try {
            data = Serializer.parse(input, Data)
        }
        catch (JsonMappingException e) {
            return error(e.message)
        }

        if (Uri.ERROR == data.discriminator) {
            return data
        }
        if (Uri.LIF != data.discriminator && Uri.LAPPS != data.discriminator) {
            return error(INVALID_DISCRIMINATOR + data.discriminator)
        }

        return data
    }

    Data validateParameters(Map params) {

        if (params == null) {
            return error(NO_PARAMETERS)
        }
        String annotationType = params.annotation
        if (annotationType == null) {
            return error(NO_PASSAGE_ANNOTAION)
        }

        String keywordFilename = params.keyword
        if (keywordFilename == null) {
            return error(NO_KEYWORD_PARAMETER)
        }
    }

    protected Data error(String message) {
        return new Data(Uri.ERROR, message)
    }

    @Override
    abstract String execute(String input)

    @Override
    abstract String getMetadata()

}
