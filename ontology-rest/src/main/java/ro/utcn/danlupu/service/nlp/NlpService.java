package ro.utcn.danlupu.service.nlp;

import ro.utcn.danlupu.model.TextInterpreterRequest;
import ro.utcn.danlupu.model.TextInterpreterResponse;

public interface NlpService {
    TextInterpreterResponse interpretText(TextInterpreterRequest textInterpreterRequest);
}
