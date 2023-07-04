package ro.utcn.danlupu.fakenewsdetectorapi.dto;

public record FakeNewsDetectorRequest(String text, String nlpProcessor) {
    public FakeNewsDetectorRequest(String text) {
        this(text, "gpt");
    }
}
