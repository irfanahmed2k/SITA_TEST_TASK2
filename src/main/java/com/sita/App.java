package com.sita;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskExecutor;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.integration.core.MessageSource;
import org.springframework.integration.dsl.IntegrationFlow;
import org.springframework.integration.dsl.IntegrationFlows;
import org.springframework.integration.dsl.core.Pollers;
import org.springframework.integration.dsl.support.Transformers;
import org.springframework.integration.file.FileReadingMessageSource;
import org.springframework.integration.file.filters.AcceptOnceFileListFilter;
import org.springframework.integration.file.filters.CompositeFileListFilter;
import org.springframework.integration.file.filters.RegexPatternFileListFilter;
import org.springframework.integration.transaction.DefaultTransactionSynchronizationFactory;
import org.springframework.integration.transaction.ExpressionEvaluatingTransactionSynchronizationProcessor;
import org.springframework.integration.transaction.PseudoTransactionManager;
import org.springframework.integration.transaction.TransactionSynchronizationFactory;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * Use this class to Run as Spring boot application.
 * In this class the inbound file route is created. 
 *
 */

@SpringBootApplication
public class App {

    public static final String INBOUND_CHANNEL = "inbound-channel";

	@Autowired
    public File inboundReadDirectory;

    @Autowired
    private ApplicationContext applicationContext;

    public static void main(String[] args) throws IOException, InterruptedException {
        SpringApplication.run(App.class, args);
    }
    
    @Bean
    public IntegrationFlow inboundFileIntegration(@Value("${inbound.file.fixed.delay}") long fixedDelay,
                                                  @Value("${inbound.file.max.messages.per.poll}") int maxMessagesPerPoll,
                                                  TaskExecutor taskExecutor,
                                                  MessageSource<File> fileReadingMessageSource) {
        return IntegrationFlows.from(fileReadingMessageSource,
                c -> c.poller(Pollers.fixedDelay(fixedDelay)
                        .taskExecutor(taskExecutor)
                        .maxMessagesPerPoll(maxMessagesPerPoll)
                        .transactionSynchronizationFactory(transactionSynchronizationFactory())
                        .transactional(transactionManager())))
                .transform(Transformers.fileToString())
                .channel(INBOUND_CHANNEL).get();
    }

    @Bean
    public TaskExecutor taskExecutor(@Value("${inbound.file.thread.pool.size}") int threadPoolSize) {
        ThreadPoolTaskExecutor taskExecutor = new ThreadPoolTaskExecutor();
        taskExecutor.setCorePoolSize(threadPoolSize);
        return taskExecutor;
    }

    @Bean
    public PseudoTransactionManager transactionManager() {
        return new PseudoTransactionManager();
    }

    @Bean
    public TransactionSynchronizationFactory transactionSynchronizationFactory() {
        ExpressionParser parser = new SpelExpressionParser();
        ExpressionEvaluatingTransactionSynchronizationProcessor syncProcessor =
                new ExpressionEvaluatingTransactionSynchronizationProcessor();
        syncProcessor.setBeanFactory(applicationContext.getAutowireCapableBeanFactory());
        syncProcessor.setAfterCommitExpression(parser.parseExpression("payload.renameTo(new java.io.File(@inboundProcessedDirectory.path " +
                " + T(java.io.File).separator + payload.name))"));
        syncProcessor.setAfterRollbackExpression(parser.parseExpression("payload.renameTo(new java.io.File(@inboundErrorDirectory.path " +
                " + T(java.io.File).separator + payload.name))"));
        return new DefaultTransactionSynchronizationFactory(syncProcessor);
    }

    @Bean
    public FileReadingMessageSource fileReadingMessageSource(@Value("${inbound.filename.pattern}") String filenameRegex) {
        FileReadingMessageSource source = new FileReadingMessageSource();
        source.setDirectory(this.inboundReadDirectory);
        source.setAutoCreateDirectory(true);
        CompositeFileListFilter<File> filter = new CompositeFileListFilter<>(
                Arrays.asList(new AcceptOnceFileListFilter<File>(),
                        new RegexPatternFileListFilter(filenameRegex))
        );
        source.setFilter(filter);
        return source;
    }

}
