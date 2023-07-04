package ro.utcn.danlupu.service.kb.impl;

import com.articulate.sigma.KB;
import com.articulate.sigma.KBmanager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ro.utcn.danlupu.service.kb.KBFactory;


@Component
@Slf4j
public class KBFactoryImpl implements KBFactory {

    private volatile KB kb;

    @Override
    public KB getKB() {
        if (kb == null) {
            synchronized (KBFactoryImpl.class) {
                if (kb == null) {
                    log.info("Set KB.");
                    kb = KBmanager.getMgr().getKB("SUMO");
                }
            }
        }
        return kb;
    }
}
