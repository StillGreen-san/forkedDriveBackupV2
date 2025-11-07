package moe.sgs;

import org.json.JSONObject;
import ratismal.drivebackup.uploaders.onedrive.GraphApiError;
import ratismal.drivebackup.uploaders.onedrive.GraphApiErrorException;
import ratismal.drivebackup.uploaders.onedrive.GraphApiErrorExceptionOld;

public class Main {

    public static final String BODY_A = "{\n" + "  \"error\": {\n" + "    \"code\": \"badRequest\",\n"
        + "\t\"message\": \"Uploaded fragment overlaps with existing data.\",\n" + "\t\"innererror\": {\n"
        + "\t  \"code\": \"invalidRange\",\n" + "\t  \"request-id\": \"request-id\",\n"
        + "\t  \"date\": \"date-time\"\n" + "\t}\n" + "  }\n" + "}";
    public static final String BODY_B = "{\n" + "  \"error\": {\n" + "\t\"code\": \"badRequest\",\n"
        + "\t\"message\": \"Multiple errors in ContactInfo data\",\n" + "\t\"target\": \"contactInfo\",\n"
        + "\t\"details\": [\n" + "\t  {\"error\":{\n" + "\t\t\"code\": \"nullValue\",\n"
        + "\t\t\"target\": \"phoneNumber\",\n" + "\t\t\"message\": \"Phone number must not be null\"\n" + "\t  }},\n"
        + "\t  {\n" + "\t\t\"code\": \"nullValue\",\n" + "\t\t\"target\": \"lastName\",\n"
        + "\t\t\"message\": \"Last name must not be null\"\n" + "\t  },\n" + "\t  {\n"
        + "\t\t\"code\": \"malformedValue\",\n" + "\t\t\"target\": \"address\",\n"
        + "\t\t\"message\": \"Address is not valid\"\n" + "\t  }\n" + "\t]\n" + "  }\n" + "}";
    public static final String BODY_C = "{\n" + "  \"error\": {\n" + "    \"code\": \"unauthorized\",\n"
        + "    \"message\": \"Previous passwords may not be reused\",\n" + "    \"target\": \"password\",\n"
        + "    \"innerError\": {\n" + "      \"code\": \"passwordError\",\n" + "      \"innererror\": {\n"
        + "        \"code\": \"passwordDoesNotMeetPolicy\",\n" + "        \"minLength\": \"6\",\n"
        + "        \"maxLength\": \"64\",\n"
        + "        \"characterTypes\": [\"lowerCase\",\"upperCase\",\"number\",\"symbol\"],\n"
        + "        \"minDistinctCharacterTypes\": \"2\",\n" + "        \"InnerError\": {\n"
        + "          \"Code\": \"passwordReuseNotAllowed\"\n" + "        }\n" + "      }\n" + "    }\n" + "  }\n" + "}";

    public static void main(String[] args) {
        System.out.println("We Mainin Boys!");
        Try(101, BODY_A, false);
        Try(101, BODY_A, true);
        Try(102, BODY_B, false);
        Try(102, BODY_B, true);
        Try(103, BODY_C, false);
        Try(103, BODY_C, true);
        Try(123, "NOT FOUND", false);
        Try(123, "NOT FOUND", true);
    }

    public static void Try(int code, String respBody, boolean improved) {
        try {
            if (improved) {
                GraphApiErrorException ex = new GraphApiErrorException(code, respBody);
                JSONObject jobj = new JSONObject(ex);//, "statusCode", "error");
                System.out.println(jobj.toString(4));
                throw ex.verbose();
            }
            else
                throw new GraphApiErrorExceptionOld(code, respBody);
        } catch (GraphApiErrorException e) {
            if (e.error.innerError != null) {
                System.out.println(e.error.innerError.contents);
            }
            e.printStackTrace();
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
