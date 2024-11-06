FROM nginx:latest

COPY index.html /usr/share/nginx/html

RUN apt-get update && apt-get install -y fcgiwrap && rm -rf /var/lib/apt/lists/*

COPY nginx_shutdown.sh /usr/local/bin/nginx_shutdown.sh

RUN chmod +x /usr/local/bin/nginx_shutdown.sh

COPY nginxconf.conf /etc/nginx/conf.d/default.conf
