FROM python:3.12
WORKDIR /app
COPY pythonShutdown.py .
RUN pip install flask docker
EXPOSE 5000
CMD ["python", "pythonShutdown.py"]