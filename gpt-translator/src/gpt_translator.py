import openai
from fastapi import HTTPException
from fastapi.logger import logger

from src.config import OPENAI_KEY, MODEL_FINE_TUNE_ID, STOP_STRINGS, FORMAL_MODEL_TYPE, KIF_MODEL_TYPE

openai.api_key = OPENAI_KEY


def convert_text_to_kif(text: str):
    formal_text = convert(FORMAL_MODEL_TYPE, text)
    return convert(KIF_MODEL_TYPE, formal_text)


def convert(action_type, prompt):
    formal_model = get_translator(action_type)
    return formal_model.get_completion(prompt)


def get_translator(model_type: str):
    fine_tune_id = MODEL_FINE_TUNE_ID[model_type]
    return GptTranslator(fine_tune_id, STOP_STRINGS[model_type])


class GptTranslator:
    def __init__(self, fine_tune_id: str, delimiter: str):
        self.model = self._get_model(fine_tune_id)
        self.prompt_suffix = " ->"
        self.delimiter = delimiter

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
            temperature=0,
            stop=[self.delimiter]
        )
