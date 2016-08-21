package org.anc.lapps.chunk

import com.fasterxml.jackson.databind.JsonMappingException
import org.anc.lapps.chunk.window.Window
import org.anc.lapps.chunk.window.extraction.AbstractWindowExtraction
import org.anc.lapps.chunk.window.extraction.AnnTypeBasedWindow
import org.lappsgrid.api.WebService
import org.lappsgrid.discriminator.Discriminators
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.metadata.ServiceMetadataBuilder
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri

/**
 * @author Keith Suderman
 */
class WindowExtractorService implements WebService {

    static final String WINDOW = "Window"

    static final String NO_INPUT = "No input was provided."
    static final String INVALID_DISCRIMINATOR = "Invalid discriminator. Expected a LIF document but found "
    static final String NO_PARAMETERS = "No input parameter map found."
    static final String NO_PASSAGE_ANNOTAION = "No passage annotation type was provided."
    static final String NO_ANNOTATIONS = "No view contains the selected annotation type."
    static final String NO_KEYWORD_PARAMETER = "Missing Parameter: no keywords file specified."
    static final String NO_KEYWORD_FILE = "Keyword file not found."

    String metadata

    /**
     * Entry point for a Lappsgrid service.
     * <p>
     * Each service on the Lappsgrid will accept {@code org.lappsgrid.serialization.Data} object
     * and return a {@code Data} object with a {@code org.lappsgrid.serialization.lif.Container}
     * payload.
     * <p>
     * Errors and exceptions the occur during processing should be wrapped in a {@code Data}
     * object with the discriminator set to http://vocab.lappsgrid.org/ns/error
     * <p>
     * See <a href="https://lapp.github.io/org.lappsgrid.serialization/index.html?org/lappsgrid/serialization/Data.html>org.lappsgrid.serialization.Data</a><br />
     * See <a href="https://lapp.github.io/org.lappsgrid.serialization/index.html?org/lappsgrid/serialization/lif/Container.html>org.lappsgrid.serialization.lif.Container</a><br />
     *
     *
     * @param input A JSON string representing a Data object
     * @return A JSON string containing a Data object with a Container payload.
     */
    @Override
    String execute(String input) {

        Data data = validateInputJson(input)

        if (Uri.ERROR == data.discriminator) {
            return data.asPrettyJson()
        }

        Map params = data.parameters
        Data paramsValidation = validateParameters(params)
        if (paramsValidation != null && paramsValidation.discriminator == Uri.ERROR) {
            return paramsValidation.asPrettyJson()
        }

        File keytermFile = new File((String) params.keyword)
        if (!keytermFile.exists()) {
            return error(NO_KEYWORD_FILE).asPrettyJson()
        }

        List<String> keyterms = readKeyterms(keytermFile)
        Container container = new Container(data.payload)

        AbstractWindowExtraction extractor = new AnnTypeBasedWindow(params)
        List<Window> candidateWindows = extractor.extract(container, keyterms)

        if (candidateWindows == null || candidateWindows.size() == 0) {
            Container result = new Container()
            result.text = container.text
            result.metadata = container.metadata
            View view = result.newView()
            String annotationType = params.annotation
            view.addContains(annotationType, WindowExtractorService.class.name, annotationType)
            return new Data(Discriminators.Uri.LIF, result).asJson()
        }

        View resultView = container.newView()
        resultView.addContains(WINDOW, WindowExtractorService.class.name, WINDOW)
        resultView.metadata.keyterms = keyterms
        resultView.metadata.sizelimit = params.sizelimit ?: "unlimited"
        resultView.metadata.numlimit = params.numlimit ?: "unlimited"
        resultView.metadata.matchlimit = params.matchlimit ?: "unlimited"

        int id = 0
        candidateWindows.each {Window window ->
            resultView.addAnnotation(window.toAnnotation("window-${++id}"))
        }

        return new Data(Uri.LIF, container).asPrettyJson()
    }

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

    List<String> readKeyterms(File keytermFile) {
        return keytermFile.readLines()
    }

    boolean contains(String line, List<String> keyterms) {
        for (String keyterm : keyterms) {
            if (line.contains(keyterm)) {
                return true
            }
        }
        return false
    }
    /**
     * Returns a JSON string containing metadata describing the service. The
     * JSON <em>must</em> conform to the json-schema at
     * <a href="http://vocab.lappsgrid.org/schema/service-schema.json">http://vocab.lappsgrid.org/schema/service-schema.json</a>
     * (processing services) or
     * <a href="http://vocab.lappsgrid.org/schema/datasource-schema.json">http://vocab.lappsgrid.org/schema/datasource-schema.json</a>
     * (datasources).
     *
     */
    @Override
    String getMetadata() {
        if (!metadata) {
            ServiceMetadata md = new ServiceMetadataBuilder()
                .allow(Uri.ANY)
                .license(Uri.APACHE2)
                .vendor("http://www.anc.org")
                .name(WindowExtractorService.class.name)
                .version(Version.getVersion())
                .description('Extracts all segments (chunks) that contain the given string(s).')
                .requireEncoding('UTF-8')
                .requireFormat(Uri.LIF)
                .produceEncoding('UTF-8')
                .produceFormat(Uri.LIF)
                .build()
            metadata = Serializer.toPrettyJson(md)
        }
        return metadata
    }

    private Data error(String message) {
        return new Data(Uri.ERROR, message)
    }


    public static void main(String[] args) {
        CliBuilder cli = new CliBuilder()
        cli.usage = "pe -a <annotation> -k <keyword> -o <path>"
        cli.header = '''\nOptions:

'''
//        cli.header = '''
//NAME
//    passage extractor
//
//DESCRIPTION
//    Extracts all passages from the text that contain the given
//    keyword.  Passages are defined by the annotation type given.
//
//PARAMETERS
//
//'''
        cli.footer = "\nCopyright 2016 American National Corpus. All rights reserved."
        cli.a(longOpt:'annotation', args:1, argName: 'uri', 'annotation type that defines the passage to be extracted.')
        cli.k(longOpt:'keyword', args:1, argName: 'string', 'passages containing this keyword will be extracted')
        cli.o(longOpt:'output', args:1, argName: 'file', 'output file that will be created.')
        cli.v(longOpt:'version', 'current version of the program')
        cli.h(longOpt:'help', 'prints this help message')
        cli.i(longOpt:'input', args:1, argName: 'file', 'the input file to be loaded.' )

//        cli.usage()
        def params = cli.parse(args)
        if (!params) {
            println "Unable to parse the command line parameters"
            cli.usage()
            return
        }

        if (params.h) {
            cli.usage()
            return
        }

        if (params.v) {
//            char c = 0xA9
            println()
            println "Lappsgrid Passage Extractor v${Version.getVersion()}"
            println "Copyright 2016 American National Corpus. All rights reserved."
            println()
            return
        }

        String annotation = params.a
        String keyword = params.k
        String input = params.i
        String output = params.o

        List<String> errors = new ArrayList<>()
        if (!annotation) {
            errors << 'No annotation specified.'
        }
        if (!keyword) {
            errors << 'No keyword specified.'
        }
        File inputFile = new File(input)
        if (!inputFile.exists()) {
            errors << "Input file not found: ${inputFile.path}"
        }

        if (errors.size() > 0) {
            println "One or more errors were encountered: "
            errors.eachWithIndex{ String message, int i ->
                println "${i}: ${message}"
            }
            return
        }


        Data data = new Data()
        data.discriminator = Uri.LIF
        data.parameters = [
                annotation: annotation,
                keyword: keyword
        ]
        Container container = new Container()
        container.text = inputFile.text
        data.payload = container

        String json = new WindowExtractorService().execute(data.asJson())
        println json

    }
}
