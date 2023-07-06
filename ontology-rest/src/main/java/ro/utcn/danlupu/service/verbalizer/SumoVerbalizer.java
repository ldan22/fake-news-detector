package ro.utcn.danlupu.service.verbalizer;

import com.articulate.sigma.Formula;

import java.util.List;

public interface SumoVerbalizer {
    String verbalize(Formula formula);
    List<String> verbalize(List<Formula> formula);
}
