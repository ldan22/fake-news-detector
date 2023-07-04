import openai
from fastapi import FastAPI, HTTPException
from pydantic import BaseModel
from fastapi.logger import logger

app = FastAPI()

OPENAI_KEY = "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"
FORMAL_MODEL_TYPE = "formal"
KIF_MODEL_TYPE = "kif"

MODEL_FINE_TUNE_ID = {
    FORMAL_MODEL_TYPE: "ft-QeWASBRovqLjlE62WAJ3w0E0",
    KIF_MODEL_TYPE: "ft-Lmb3doR9bnnJNTQZsmMccK3w"
}

openai.api_key = OPENAI_KEY


class TranslatorRequest(BaseModel):
    text: str


class TranslatorResponse(BaseModel):
    kif_formula: str


@app.post("/translator")
async def translate_text(translator_request: TranslatorRequest) -> TranslatorResponse:
    logger.info("Received request for translating text: {}".format(translator_request.text))
    kif_formula = translate_with_gpt(translator_request.text)
    return TranslatorResponse(kif_formula=kif_formula)


@app.get("/fine_tuned_models")
async def list_all_models():
    return openai.FineTune.list()


@app.get("/fine_tuned_models/{fine_tune_id}")
async def list_all_models(fine_tune_id: str):
    return openai.FineTune.retrieve(id=fine_tune_id)


def translate_with_gpt(text: str) -> str:
    formal_model = get_translator(FORMAL_MODEL_TYPE)
    formal_text = formal_model.get_completion(text)
    logger.info("Formal text obtained: {}", formal_text)
    kif_model = get_translator(KIF_MODEL_TYPE)
    kif_formula = kif_model.get_completion(formal_text)
    logger.info("Kif formula obtained: {}", kif_formula)
    return kif_formula


def get_translator(model_type: str):
    fine_tune_id = MODEL_FINE_TUNE_ID[model_type]
    return Translator(fine_tune_id)


class Translator:
    def __init__(self, fine_tune_id: str):
        self.model = self._get_model(fine_tune_id)
        self.prompt_suffix = " ->"

    @staticmethod
    def _get_model(fine_tune_id):
        fine_tune = openai.FineTune.retrieve(id=fine_tune_id)
        if fine_tune is None or fine_tune.fine_tuned_model is None:
            logger.warn("No model found with id: {}".format(fine_tune_id))
            raise HTTPException(status_code=500, detail="No model found with id: {}".format(fine_tune_id))
        return fine_tune.fine_tuned_model

    def get_completion(self, prompt):
        normalized_prompt = prompt + self.prompt_suffix
        completion = self._create_completion(normalized_prompt)
        logger.info("Completion response for text: {} is: {}", normalized_prompt, completion)
        return completion['choices'][0]['text']

    def _create_completion(self, prompt: str):
        return openai.Completion.create(
            model=self.model,
            prompt=prompt,
            max_tokens=10,
            temperature=0
        )
