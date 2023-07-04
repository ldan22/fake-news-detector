import openai
from fastapi import FastAPI
from pydantic import BaseModel
from fastapi.logger import logger

from src.gpt_translator import convert_text_to_kif

app = FastAPI()


class TranslatorRequest(BaseModel):
    text: str


class TranslatorResponse(BaseModel):
    kif_formula: str


@app.post("/translator")
async def translate_text(translator_request: TranslatorRequest) -> TranslatorResponse:
    logger.info("Received request for translating text: {}".format(translator_request.text))
    kif_formula = convert_text_to_kif(translator_request.text)
    return TranslatorResponse(kif_formula=kif_formula)


@app.get("/fine_tuned_models")
async def list_all_models():
    return openai.FineTune.list()


@app.get("/fine_tuned_models/{fine_tune_id}")
async def list_all_models(fine_tune_id: str):
    return openai.FineTune.retrieve(id=fine_tune_id)
