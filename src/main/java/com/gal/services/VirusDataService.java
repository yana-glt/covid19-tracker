package com.gal.services;

import com.gal.models.Location;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service
public class VirusDataService {
    private List<Location> allStats = new ArrayList<>();
    public List<Location> getAllStats() {
        return allStats;
    }

    private static String VIRUS_DATASOURCE_URL = "https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_deaths_global.csv";

    //fetch the data
    @PostConstruct
    //run on a schedule cron = "*sec *min *hour *day *month *year <command>"
    @Scheduled(cron = "* * 1 * * *")
    public void fetchVirusData() throws IOException, IOException, InterruptedException {
        List<Location> newStats = new ArrayList<>();
        HttpClient client = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(VIRUS_DATASOURCE_URL)).build();
        HttpResponse <String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        StringReader csvReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord>records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvReader);
        for(CSVRecord record: records){
            Location location = new Location();
            location.setState(record.get("Province/State"));
            location.setCountry(record.get("Country/Region"));
            int latestCases = Integer.parseInt(record.get(record.size()-1));
            int prevDayCases = Integer.parseInt(record.get(record.size()-2));
            location.setLatestTotalCases(latestCases);
            location.setDelta(latestCases-prevDayCases);
            newStats.add(location);
        }
        this.allStats = newStats;
    }
}
