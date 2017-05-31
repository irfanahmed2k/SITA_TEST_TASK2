package com.sita.writer;


import java.io.File;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.file.FileNameGenerator;
import org.springframework.integration.file.FileWritingMessageHandler;
import org.springframework.integration.handler.LoggingHandler;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageHandler;
import org.springframework.stereotype.Component;

import com.sita.App;

/**
 * Calculate the Sum and write the output file
 *
 */
@Component
public class FileProcessor {
    
    private static final String HEADER_FILE_NAME = "file_name";

    @Autowired
    public File inboundOutDirectory;

    @Bean
    public IntegrationFlow writeToFile(@Qualifier("fileWritingMessageHandler") MessageHandler fileWritingMessageHandler) {
        return IntegrationFlows.from(App.INBOUND_CHANNEL)
                .transform(m -> calculateSum(((String)m).toString()))
                .handle(fileWritingMessageHandler)
                .handle(loggingHandler())
                .get();
    }

    @Bean (name = "fileWritingMessageHandler")
    public MessageHandler fileWritingMessageHandler() {
        FileWritingMessageHandler handler = new FileWritingMessageHandler(inboundOutDirectory);
        handler.setFileNameGenerator(new FileNameGenerator() {
			@Override
			public String generateFileName(Message<?> message) {
				String originalFileName = (String) message.getHeaders().get(HEADER_FILE_NAME);
				return originalFileName+ ".PROCESSED";
			}
		});
        handler.setAutoCreateDirectory(true);
        return handler;
    }
    
    @Bean
    public MessageHandler loggingHandler() {
        LoggingHandler logger = new LoggingHandler("INFO");
        logger.setShouldLogFullMessage(true);
        return logger;
    }
    
    private String calculateSum(String content) {
    	//split on new line and remove empty lines
    	String[] numbers = content.split("[\\r\\n]+");
        long sum = 0;
        for (String number : numbers) {
        	//throws NumberFormatException for string and message will be moved to error folder
            int num = Integer.parseInt(number); 
            sum += num;
        }
        return String.valueOf(sum);
    }
}
