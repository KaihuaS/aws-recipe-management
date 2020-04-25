package app.service;

import com.amazonaws.services.sns.AmazonSNS;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import app.model.SNSMessageAttributes;

@Service
public class SNSService {
    @Autowired
    AmazonSNS amazonSNSClient;

    @Value("${cloud.aws.topic.arn}")
    private String topicArn;

    public void publishMessage(String message, String urls, String username, String token){
        final String msg = "If you receive this message, publishing a message to an Amazon SNS topic works.";
        SNSMessageAttributes snsMessageAttributes = new SNSMessageAttributes(msg);
        snsMessageAttributes.addAttribute("Urls", urls);
        snsMessageAttributes.addAttribute("Username", username);
        snsMessageAttributes.addAttribute("Token", token);
        // Print the MessageId of the message.
        System.out.println("MessageId: " + snsMessageAttributes.publish(amazonSNSClient, topicArn));
    }
}