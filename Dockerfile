FROM python:3.12-slim

RUN groupadd --system simulator && useradd --system --gid simulator --home-dir /app --create-home simulator

WORKDIR /app
COPY requirements.txt .
RUN pip install --no-cache-dir --only-binary=:all: -r requirements.txt
COPY --chown=simulator:simulator simulator ./simulator
COPY --chown=simulator:simulator schemas ./schemas

USER simulator
CMD ["python", "-m", "simulator"]
