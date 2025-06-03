FROM python:3.9-slim

RUN useradd -m user
USER user

WORKDIR /app

COPY --chown=user requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

COPY --chown=user . .

CMD ["python", "app.py"]
