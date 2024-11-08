import docker
from flask import Flask, jsonify

app = Flask(__name__)

def shutdown_containers():
    client = docker.from_env()
    containers = client.containers.list()

    for container in containers:
        if "python-app" not in container.name:
            container.stop()

    shutdown_service = client.containers.get("python-app")
    shutdown_service.stop()

@app.route('/shutdown', methods=['POST'])
def shutdown():
    try:
        shutdown_containers()
        return jsonify({"message": "Shutting down"}), 200
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == "__main__":
    app.run(host='0.0.0.0', port=5000)