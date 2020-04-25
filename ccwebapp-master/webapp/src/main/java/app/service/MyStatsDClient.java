package app.service;

import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;

public class MyStatsDClient {
  private static final StatsDClient statsd = new NonBlockingStatsDClient("csye6225-demo", "localhost", 8125);

  public static StatsDClient getStatsDClient(){
    return statsd;
  }
}