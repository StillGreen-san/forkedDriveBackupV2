package ratismal.drivebackup.uploaders.onedrive;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GraphApiErrorExceptionTest {

    @Test
    void singleResource() {
        String singleResource =
            "{\n"
            + "  \"error\": {\n"
            + "    \"code\": \"stringcode\",\n"
            + "    \"message\": \"stringmessage\",\n"
            + "    \"innererror\": { \n"
            + "      \"code\": \"stringcodeinner\"\n"
            + "    },\n"
            + "    \"details\": []\n"
            + "  }\n"
            + "}";

        GraphApiErrorException exception = new GraphApiErrorException(123, singleResource);
        assertEquals(123, exception.statusCode);
        assertEquals("stringcode", exception.errorCode);
        assertEquals("stringmessage", exception.errorMessage);
        assertTrue(exception.details.isEmpty());
        assertEquals(1, exception.innerErrors.size());
        assertEquals("stringcodeinner", exception.innerErrors.get(0));
    }

    @Test
    void simpleError() {
        String simpleError =
            " {\n"
            + "   \"error\": {\n"
            + "     \"code\": \"badRequest\",\n"
            + "     \"message\": \"Cannot process the request because it is malformed or incorrect.\",\n"
            + "     \"target\": \"resource\"\n"
            + "   }\n"
            + " }";

        GraphApiErrorException exception = new GraphApiErrorException(400, simpleError);
        assertEquals(400, exception.statusCode);
        assertEquals("badRequest", exception.errorCode);
        assertEquals("Cannot process the request because it is malformed or incorrect.", exception.errorMessage);
        assertTrue(exception.details.isEmpty());
        assertTrue(exception.innerErrors.isEmpty());
    }

    @Test
    void detailedError() {
        String detailedError =
            " {\n"
            + "   \"error\": {\n"
            + "     \"code\": \"badRequest\",\n"
            + "     \"message\": \"Cannot process the request because a required field is missing.\",\n"
            + "     \"innererror\": {\n"
            + "       \"code\": \"requiredFieldOrParameterMissing\",\n"
            + "       \"innererror\": {\n"
            + "         \"code\": \"innerer\"\n"
            + "       }\n"
            + "     },\n"
            + "    \"details\": [\n"
            + "      {\n"
            + "       \"error\": {\n"
            + "          \"code\": \"codeinner\",\n"
            + "          \"message\": \"messageinner\",\n"
            + "          \"target\": \"resource\"\n"
            + "         }\n"
            + "       },\n"
            + "      {\n"
            + "       \"error\": {\n"
            + "          \"code\": \"codeinner2\",\n"
            + "          \"message\": \"messageinner2\"\n"
            + "         }\n"
            + "       }\n"
            + "     ]\n"
            + "   }\n"
            + " }";

        GraphApiErrorException exception = new GraphApiErrorException(400, detailedError);
        assertEquals(400, exception.statusCode);
        assertEquals("badRequest", exception.errorCode);
        assertEquals("Cannot process the request because a required field is missing.", exception.errorMessage);
        assertEquals(2, exception.innerErrors.size());
        assertEquals("requiredFieldOrParameterMissing", exception.innerErrors.get(0));
        assertEquals("innerer", exception.innerErrors.get(1));
        assertEquals(2, exception.details.size());
        assertEquals(-1, exception.details.get(0).statusCode);
        assertEquals("codeinner", exception.details.get(0).errorCode);
        assertEquals("messageinner", exception.details.get(0).errorMessage);
        assertTrue(exception.details.get(0).details.isEmpty());
        assertTrue(exception.details.get(0).innerErrors.isEmpty());
        assertEquals(-1, exception.details.get(1).statusCode);
        assertEquals("codeinner2", exception.details.get(1).errorCode);
        assertEquals("messageinner2", exception.details.get(1).errorMessage);
        assertTrue(exception.details.get(1).details.isEmpty());
        assertTrue(exception.details.get(1).innerErrors.isEmpty());
    }
}