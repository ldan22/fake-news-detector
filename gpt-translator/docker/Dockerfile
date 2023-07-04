FROM python:3.10 as python-base

RUN mkdir gpt-translator
WORKDIR  /gpt-translator

COPY /pyproject.toml /gpt-translator

RUN pip3 install poetry

RUN poetry config virtualenvs.create false

RUN poetry install

COPY . .

CMD ["uvicorn", "src.main:app", "--host", "0.0.0.0", "--port", "8000"]

