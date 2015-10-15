import com.datamodel.datamodels.CheckIn;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import com.sun.deploy.net.HttpResponse;
//import javafx.scene.control.TextInputControl;
import sun.net.www.http.HttpClient;

import javax.swing.text.AbstractDocument;
import java.io.*;
import java.net.*;
import java.text.*;
//import java.nio.charset.StandardCharsets;
import java.util.*;
import com.google.android.gms.maps.model.LatLng;

public class ClientApp {



    public static void main(String args[]) {

        ClientApp Exp = new ClientApp();
        Exp.CreateTeam("test team",2);

    }

    public void CreateTeam(String teamName, int userID){

        String targetURL = "http://localhost:8080/Servlet";
        String urlParameters = "ActionType=CreateTeam&TeamName=Победа";


        //System.out.println(urlParameters);
        URL url;
        HttpURLConnection connection = null;
        try {
            //Create connection
            url = new URL(targetURL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");

            connection.setRequestProperty("Content-Length", "" +
                    Integer.toString(urlParameters.getBytes().length));
            connection.setRequestProperty("Content-Language", "en-US");

            connection.setUseCaches(false);
            connection.setDoInput(true);
            connection.setDoOutput(true);

            //Send request
            OutputStream wr = connection.getOutputStream();
            wr.write(urlParameters.getBytes());
            wr.flush();
            wr.close();

            /*
            //Get Response
            Gson gson = new Gson();
            InputStream is = connection.getInputStream();
            JsonReader rd = new JsonReader(new InputStreamReader(is,"UTF-8"));
            List<CheckIn> responseList = new ArrayList<CheckIn>();
            rd.beginArray();
            while (rd.hasNext()) {
                CheckIn checkin = gson.fromJson(rd, CheckIn.class);
                responseList.add(checkin);
            }
            rd.endArray();
            rd.close();
            System.out.println(responseList.get(1).getComment());
            */
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
}

