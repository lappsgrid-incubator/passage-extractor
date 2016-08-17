package org.anc.lapps.chunk

import org.junit.*
import org.lappsgrid.api.WebService
import org.lappsgrid.metadata.ServiceMetadata
import org.lappsgrid.serialization.Data
import org.lappsgrid.serialization.Serializer
import org.lappsgrid.serialization.lif.Annotation
import org.lappsgrid.serialization.lif.Container
import org.lappsgrid.serialization.lif.View

import static org.lappsgrid.discriminator.Discriminators.Uri
import static org.junit.Assert.*

/**
 * @author Keith Suderman
 */
class PassageExtractorTest {
    static final String S1 = "Barak Obama is the 44th president of the United States."
    static final String S2 = "London is the capital city of England."
    static final String S3 = "Queen Elizabeth is the monarch of England."
    static final String UTF8 = 'UTF-8'

    WebService service
    Container container

    @Before
    void setup() {
        service = new PassageExtractor()
    }

    @After
    void teardown() {
        service = null
    }

    @Test
    void testMetadata() {
        ServiceMetadata metadata = Serializer.parse(service.getMetadata(), ServiceMetadata)
        assertNotNull metadata
        assertEquals(Uri.ANY, metadata.allow)
        assertEquals(Uri.APACHE2, metadata.license)
        assertEquals(PassageExtractor.class.name, metadata.name)
        assertEquals(Version.getVersion(), metadata.version)
        assertEquals("http://www.anc.org", metadata.vendor)
        assertEquals(UTF8, metadata.requires.encoding)
        assertEquals(UTF8, metadata.produces.encoding)
        assertEquals(1, metadata.requires.format.size())
        assertEquals(1, metadata.produces.format.size())
        assertEquals(Uri.LIF, metadata.requires.format[0])
        assertEquals(Uri.LIF, metadata.produces.format[0])
    }

    @Test
    void testNoInput() {
        String json = service.execute()
        assertNotNull json
        Data data = Serializer.parse(json, Data)
        hasError(data)
        assertEquals(PassageExtractor.NO_INPUT, data.payload.toString())
    }

    @Test
    void testEmptyInput() {
        String json = service.execute('')
        assertNotNull json
        Data data = Serializer.parse(json, Data)
        hasError(data)
        assertTrue(data.payload.toString().startsWith('No content'))
    }

    @Test
    void testError() {
        String expected = 'error message'
        Data data = new Data(Uri.ERROR, expected)
        data = execute(data)
        hasError(data)
        assertEquals(expected, data.payload.toString())
    }

    @Test
    void testWrongFormat() {
        Data data = new Data(Uri.TEXT, 'hello world')
        data = execute(data)
        hasError(data)
        assertTrue(data.payload.toString().startsWith(PassageExtractor.INVALID_DISCRIMINATOR))
    }

    @Test
    void testNoParameters() {
        Data data = new Data(Uri.LIF, null)
        data = execute(data)
        hasError(data)
        assertEquals(PassageExtractor.NO_PARAMETERS, data.payload.toString())
    }

    @Test
    void testNoSegment() {
        Data data = new Data(Uri.LIF, null)
        data.parameters = [:]
        data = execute(data)
        hasError(data)
        assertEquals(PassageExtractor.NO_PASSAGE_ANNOTAION, data.payload.toString())
    }

    @Test
    void testNoKeyword() {
        Data data = new Data(Uri.LIF, null)
        data.parameters = [ annotation: Uri.TOKEN ]
        data = execute(data)
        hasError(data)
        assertEquals(PassageExtractor.NO_KEYWORD, data.payload.toString())
    }

    @Test
    void testNoAnnotations() {
        Data data = makeData()
        data.parameters = [
                keyword: 'foobar',
                annotation: 'token'
        ]
        data = execute(data)
        assertEquals(Uri.LIF, data.discriminator)
        container = new Container(data.payload)
        assertNotNull container.text
        println "'${container.text}'"
        assertEquals('', container.text)
        assertEquals(1, container.views.size())
        View view = container.views[0]
        assertEquals(0, view.annotations.size())
        assertTrue(view.contains('token'))
//        hasError(data)
//        assertEquals(PassageExtractor.NO_ANNOTATIONS, data.payload.toString())
    }

    @Test
    void testChunkExtractor() {
        File testFile = File.createTempFile('passage_extractor', '.txt')
        testFile.deleteOnExit()
        testFile.withWriter {
            println 'Barak Obama'
        }
        Data original = makeData()
        original.parameters = [
                keyword: testFile.path,
                annotation: Uri.SENTENCE
        ]
        Data data = execute(original)
//        assertEquals(Uri.LIF, data.discriminator)
        if (Uri.LIF != data.discriminator) {
            fail data.payload
        }
        container = new Container(data.payload)
        List<View> views = container.findViewsThatContain(Uri.SENTENCE)
        assertNotNull(views)
        assertEquals(1, views.size())
        List<Annotation> annotations = views[0].annotations
        assertNotNull(annotations)
        assertEquals(1, annotations.size())
        Annotation annotation = annotations[0]
        assertEquals(Uri.SENTENCE, annotation.atType)
        String text = container.text.substring((int)annotation.start, (int)annotation.end)
        assertEquals(S1, text)

        original.parameters.keyword = 'London'
        data = execute(original)
        container = new Container(data.payload)
        views = container.findViewsThatContain(Uri.SENTENCE)
        assertEquals(1, views.size())
        annotations = views[0].annotations
        assertEquals(1, annotations.size())
        Annotation a = annotations[0]
        assertEquals(S2, getText(a))

        original.parameters.keyword = 'England'
        data = execute(original)
        container = new Container(data.payload)
        views = container.findViewsThatContain(Uri.SENTENCE)
        assertEquals(1, views.size())
        annotations = views[0].annotations
        assertEquals(2, annotations.size())
        assertEquals(S2, getText((Annotation)annotations[0]))
        assertEquals(S3, getText((Annotation)annotations[1]))
    }

    String getText(Annotation annotation) {
        return container.text.substring((int)annotation.start, (int)annotation.end)
    }

    Data execute(Data data) {
        return Serializer.parse(service.execute(data.asJson()), Data)
    }
    void hasError(Data data) {
        assertEquals(Uri.ERROR, data.discriminator)
    }

    Data makeData() {
        Container container = new Container()
        View view = container.newView()
        view.addContains(Uri.SENTENCE, "PassageExtractor Test Case", "sentence")

        StringBuilder buffer = new StringBuilder()
        int offset = 0
        int id = 0
        [S1, S2, S3].each { String sentence ->
            view.newAnnotation("s=${++id}", Uri.SENTENCE, offset, offset + sentence.length())
            buffer.append(sentence)
            buffer.append('\n')
            offset = buffer.length()
        }
        container.text = buffer.toString()
        return new Data(Uri.LIF, container)
    }
}
