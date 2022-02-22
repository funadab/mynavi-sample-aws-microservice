package org.debugroom.mynavi.sample.aws.microservice.frontend.webapp.config;

import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBAsyncClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
//import com.amazonaws.xray.AWSXRay;
//import com.amazonaws.xray.entities.Segment;
//import com.amazonaws.xray.entities.Subsegment;
//import com.amazonaws.xray.entities.TraceHeader;
//
//import com.amazonaws.xray.handlers.TracingHandler;
import org.debugroom.mynavi.sample.aws.microservice.common.apinfra.cloud.aws.CloudFormationStackResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;

import org.debugroom.mynavi.sample.aws.microservice.frontend.webapp.domain.ServiceProperties;

@Profile("dev")
@Configuration
public class DevConfig {

    @Autowired
    ServiceProperties serviceProperties;

    @Autowired
    CloudFormationStackResolver cloudFormationStackResolver;

    @Bean
    public WebClient userWebClient(OAuth2AuthorizedClientManager oAuth2AuthorizedClientManager){
        ServletOAuth2AuthorizedClientExchangeFilterFunction function =
                new ServletOAuth2AuthorizedClientExchangeFilterFunction(oAuth2AuthorizedClientManager);
        function.setDefaultClientRegistrationId("cognito");
        return WebClient.builder()
                .baseUrl(serviceProperties.getApplicationLoadBalancer().getDns())
//                .filter(exchangeFilterFunction())
                .filter(function)
                .build();
    }

//    private ExchangeFilterFunction exchangeFilterFunction(){
//        return (clientRequest, nextFilter) -> {
//            Segment segment = AWSXRay.getCurrentSegment();
//            Subsegment subsegment = AWSXRay.getCurrentSubsegment();
//            TraceHeader traceHeader = new TraceHeader(segment.getTraceId(),
//                    segment.isSampled() ? subsegment.getId() : null,
//                    segment.isSampled() ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED);
//            ClientRequest newClientRequest = ClientRequest.from(clientRequest)
//                    .header(TraceHeader.HEADER_KEY, traceHeader.toString())
//                    .build();
//            return nextFilter.exchange(newClientRequest);
//        };
//    }

    @Bean
    DynamoDBMapperConfig dynamoDBMapperConfig(){
        return DynamoDBMapperConfig.builder()
                .withTableNameOverride(
                        DynamoDBMapperConfig.TableNameOverride
                                .withTableNamePrefix("dev_"))
                .build();
    }
    @Bean
    AmazonDynamoDB amazonDynamoDB(){
        String endpoint = cloudFormationStackResolver.getExportValue(
                serviceProperties.getCloudFormation().getDynamodb().getEndpoint());
        String region = cloudFormationStackResolver.getExportValue(
                serviceProperties.getCloudFormation().getDynamodb().getRegion());
        return AmazonDynamoDBAsyncClientBuilder.standard()
                .withEndpointConfiguration(
                        new AwsClientBuilder.EndpointConfiguration(endpoint,region))
//                .withRequestHandlers(new TracingHandler(AWSXRay.getGlobalRecorder()))
                .build();
    }
}
