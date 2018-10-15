package com.example.hehe;

import lombok.extern.slf4j.Slf4j;
import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.embedded.ConfigurableEmbeddedServletContainer;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatConnectorCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.ContextClosedEvent;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;


@SpringBootApplication
@RestController
@Slf4j
public class Application {

	public static void main(String[] args) {
		SpringApplication.run(Application.class, args);
	}

	@Autowired
	ThreadPoolTaskExecutor threadPoolExecutor;


	@RequestMapping("/pause")
	public String pause() throws InterruptedException {
		threadPoolExecutor.execute(() -> {
			while (true){
                try {
                    Thread.sleep(3000);
                    System.out.println("hello");
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
		});

		return "Pause complete";

	}

	@Bean
	public GracefulShutdown gracefulShutdown() {
		return new GracefulShutdown(threadPoolExecutor);
	}

	@Bean
	public EmbeddedServletContainerCustomizer tomcatCustomizer() {
		return new EmbeddedServletContainerCustomizer() {

			@Override
			public void customize(ConfigurableEmbeddedServletContainer container) {
				if (container instanceof TomcatEmbeddedServletContainerFactory) {
					((TomcatEmbeddedServletContainerFactory) container)
							.addConnectorCustomizers(gracefulShutdown());
				}

			}
		};
	}

	private static class GracefulShutdown implements TomcatConnectorCustomizer,
			ApplicationListener<ContextClosedEvent> {


		private volatile Connector connector;
        private ThreadPoolTaskExecutor threadPoolExecutor;

        public GracefulShutdown(ThreadPoolTaskExecutor threadPoolExecutor) {

            this.threadPoolExecutor = threadPoolExecutor;
        }

		@Override
		public void customize(Connector connector) {
			this.connector = connector;
		}

		@Override
		public void onApplicationEvent(ContextClosedEvent event) {
			this.connector.pause();
			log.info("hello tomact pause request");


			Executor executor = this.connector.getProtocolHandler().getExecutor();
			if (executor instanceof ThreadPoolExecutor) {
				try {
					ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executor;
					threadPoolExecutor.shutdown();
					if (!threadPoolExecutor.awaitTermination(90, TimeUnit.SECONDS)) {
						log.warn("Tomcat thread pool did not shut down gracefully within "
								+ "30 seconds. Proceeding with forceful shutdown");
					}
				}
				catch (InterruptedException ex) {
					Thread.currentThread().interrupt();
				}
			}


            log.info(String.valueOf(System.currentTimeMillis()));
            threadPoolExecutor.shutdown();
            log.info(String.valueOf(System.currentTimeMillis()));
            log.info("thread pool shutdown successful");


            //todolike awaitTermination
		}
	}

}
