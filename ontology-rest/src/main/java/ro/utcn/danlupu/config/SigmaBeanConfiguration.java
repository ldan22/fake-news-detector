package ro.utcn.danlupu.config;


import com.articulate.sigma.KBmanager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;


@Component
@Slf4j
public class SigmaBeanConfiguration implements ApplicationListener<ContextRefreshedEvent> {

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        initKB();
    }

    private static void initKB() {
        log.info("Initialize sigmakee KBmanager.");
        KBmanager kBmanager = KBmanager.getMgr();
        KBmanager.getMgr().prover = KBmanager.Prover.VAMPIRE;
        kBmanager.initializeOnce();
    }
}
