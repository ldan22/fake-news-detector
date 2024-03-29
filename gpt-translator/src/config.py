from decouple import config

OPENAI_KEY = config("OPENAI_KEY")

FORMAL_MODEL_TYPE = "formal"
KIF_MODEL_TYPE = "kif"

MODEL_FINE_TUNE_ID = {
    FORMAL_MODEL_TYPE: config("FORMAL_MODEL_ID"),
    KIF_MODEL_TYPE: config("KIF_MODEL_ID")
}

STOP_STRINGS = {
    FORMAL_MODEL_TYPE: '\n',
    KIF_MODEL_TYPE: '\n'
}
