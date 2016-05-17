package net.wasdev.gameon.room;

import java.util.Properties;
import javax.enterprise.context.ApplicationScoped;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import org.apache.kafka.clients.producer.*;

@ApplicationScoped
public class Kafka {

  protected String kafkaUrl;

   private Producer<String,String> producer=null;

   public Kafka(){
     getConfig();
     initProducer();
   }

   private void getConfig() {
       try {
           kafkaUrl = (String) new InitialContext().lookup("kafkaUrl");
       } catch (NamingException e) {
       }
       if (kafkaUrl == null ) {
           throw new IllegalStateException("kafkaUrl("+String.valueOf(kafkaUrl)+") was not found, check server.xml/server.env");
       }
   }

   private void initProducer(){
       //Kafka client expects this property to be set and pointing at the
       //jaas config file.. except when running in liberty, we don't need
       //one of those.. thankfully, neither does kafka client, it just doesn't
       //know that.. so we'll set this to an empty string to bypass the check.
       if(System.getProperty("java.security.auth.login.config")==null){
         System.setProperty("java.security.auth.login.config", "");
       }

       System.out.println("Initializing kafka producer for url "+kafkaUrl);
       Properties producerProps = new Properties();
       producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaUrl);
       producerProps.put("acks","-1");
       producerProps.put("client.id","gameon-room");
       producerProps.put("retries",0);
       producerProps.put("batch.size",16384);
       producerProps.put("zookeeper.session.timeout.ms",1000);
       producerProps.put("linger.ms",1);
       producerProps.put("buffer.memory",33554432);
       producerProps.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
       producerProps.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");

       //this is a cheat, we need to enable ssl when talking to message hub, and not to kafka locally
       //the easiest way to know which we are running on, is to check how many hosts are in kafkaUrl
       //locally for kafka there'll only ever be one, and messagehub gives us a whole bunch..
       boolean multipleHosts = kafkaUrl.indexOf(",") != -1;
       if(multipleHosts){
         producerProps.put("security.protocol","SASL_SSL");
         producerProps.put("ssl.protocol","TLSv1.2");
         producerProps.put("ssl.enabled.protocols","TLSv1.2");
         Path p = Paths.get(System.getProperty("java.home"), "lib", "security", "cacerts");
   			 producerProps.put("ssl.truststore.location", p.toString());
         producerProps.put("ssl.truststore.password","changeit");
         producerProps.put("ssl.truststore.type","JKS");
         producerProps.put("ssl.endpoint.identification.algorithm","HTTPS");
       }


       producer = new KafkaProducer<String, String>(producerProps);
   }

   public void publishMessage(String topic, String key, String message){
     System.out.println("Publishing to kafka, creating record");
     ProducerRecord<String,String> pr = new ProducerRecord<String,String>(topic, key, message);
     System.out.println("Publishing to kafka, sending record");
     producer.send(pr);
     System.out.println("Publishing to kafka, sent record");
   }
}
